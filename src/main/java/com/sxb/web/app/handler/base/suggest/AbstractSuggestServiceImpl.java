package com.sxb.web.app.handler.base.suggest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.library.StopLibrary;
import org.ansj.lucene5.AnsjAnalyzer;
import org.ansj.lucene5.AnsjAnalyzer.TYPE;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.sxb.web.commons.util.RetUtil;

public abstract class AbstractSuggestServiceImpl implements SuggestService{
    
    protected static final Logger logger = LoggerFactory.getLogger(AbstractSuggestServiceImpl.class);
    
    public static final String[] SPECIAL_ALL_AREA = {"北京市","天津市","郑州市","吉林省","南京市","四川省","苏州市","江西省","陕西省"};
    
    public static final String[] SPECIAL_PART_AREA = {"北京","天津","郑州","吉林","南京","四川","苏州","江西","陕西"};
    
    public static final int DEFAULT_DATAS_SIZE = 100;
    
    protected AnalyzingInfixSuggester suggester;
    
    protected Directory directory;
    
    protected Analyzer indexAnalyzer;
    
    protected Analyzer queryAnalyzer;
    
    protected Analysis analysis;
    
    protected StopRecognition stopRecognition1;
    protected StopRecognition stopRecognition2;
    
    protected abstract String getPath();
    
    protected abstract Map<String, Object> handleResult(List<LookupResult> results,String key,Area area) throws Exception;
    
    protected abstract Iterator<Car> handleIndexData();
    
    protected abstract InputIterator getInputIterator(Iterator<Car> iterator);
    
    public abstract SuggestService getSuggestService();
    
    @Override
    public void indexSuggest() throws IOException {
        
        this.deleteOldIndex();
        
        Iterator<Car> iterator = this.handleIndexData();
        InputIterator inputIterator = this.getInputIterator(iterator);
        suggester.build(inputIterator);
        suggester.commit();
        
        SuggestService suggestService = this.getSuggestService();
        if(suggestService != null){
            suggestService.indexSuggest();
        }
        
    }
    
    @Override
    public Map<String, Object> searchSuggest(String key,Area area) throws Exception {
        
        List<LookupResult> results = suggester.lookup(key, 1000, true, false, area != null ? area.getArea() : null);
        if(results.size() > 0){
            return this.signature(this.handleResult(results,key,area));
        }
        
        SuggestService suggestService = this.getSuggestService();
        if(suggestService != null){
            return suggestService.searchSuggest(key,area);
        }else{
            List<Map<String,Object>> datas = new ArrayList<Map<String, Object>>();
            if(area != null){
                Map<String,Object> data = new HashMap<String, Object>();
                data.put("areaIdPath", area.getAreaIdPath());
                data.put("show", area.getArea());
                datas.add(data);
            }
            return this.signature(RetUtil.getRetValue(true,datas));
        }
        
    }
    
    protected void deleteOldIndex(){
        Field writerField = ReflectionUtils.findField(AnalyzingInfixSuggester.class, "writer");
        ReflectionUtils.makeAccessible(writerField);
        IndexWriter indexWriter = (IndexWriter) ReflectionUtils.getField(writerField, suggester);
        if(indexWriter != null){
            try {
                indexWriter.deleteAll();
                indexWriter.commit();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    protected String convertNull(String convert){
        return convert == null ? "" : convert;
    }
    
    protected Car convertBytesRef(BytesRef bytesRef) throws IOException, ClassNotFoundException{
        InputStream is = new ByteArrayInputStream(bytesRef.bytes);
        ObjectInputStream oin = new ObjectInputStream(is);
        Car car = (Car) oin.readObject();
        return car;
    }
    
    protected Map<String, Object> signature(Map<String, Object> resMap) {
        resMap.put("signature", this.getClass().getName());
        return resMap;
    }
    
    protected List<Map<String,Object>> subDatas(List<Map<String,Object>> datas,String key,Area area){
        int size = datas.size() > DEFAULT_DATAS_SIZE ? DEFAULT_DATAS_SIZE : datas.size();
        List<Map<String,Object>> subDatas = new ArrayList<Map<String, Object>>();
        for(int i=0;i<size;i++){
            Map<String,Object> first = null;
            if(i == 0){
                first = new HashMap<String, Object>();
            }
            Map<String,Object> data = datas.get(i);
            this.appendArea(data, key, area, first, subDatas);
            subDatas.add(data);
        }
        return subDatas;
    }
    
    protected String subCategory(String category,int index){
        if(index > DEFAULT_DATAS_SIZE){
            return category;
        }
        try {
            String seg = category.replace("款", "");
            String p = "(\\d|\\.|-|!|[a-zA-Z]|\\s)*";
            boolean m = Pattern.matches(p, seg);
            if(!m && category.length() >= 18){
                Result result = this.analysis.parseStr(category).recognition(stopRecognition1).recognition(stopRecognition2);
                StringBuilder builder = new StringBuilder();
                Set<String> set = new LinkedHashSet<String>();
                for(Term t : result.getTerms()){
                    set.add(t.getName());
                }
                for(String value : set){
                    if(value.equals("t")){
                        value = "T";
                    }else if(value.equals("l")){
                        value = "L";
                    }else if(value.equals("tsi")){
                        value = "TSI";
                    }else if(value.equals("cvt")){
                        value = "CVT";
                    }
                    builder.append(value);
                }
                return builder.toString();
            }else if(!m && category.length() < 18){
                category = category.replaceAll("\\s", "");
            }else{
                category = category.replaceAll("款\\s", "款");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return category;
    }
    
    private boolean appendArea(Map<String,Object> data,String key,Area area,Map<String,Object> first,List<Map<String,Object>> subDatas){
        if(area != null){
            if(area.getArea().equals("九龙")){
                return false;
            }
            //特殊处理（品牌+厂家+车系）包含地名的情况
            String show = data.get("show").toString();
            String p1 = ".*(北京).*|.*(天津).*|.*(郑州).*|.*(吉林).*|.*(南京).*|.*(四川).*|.*(苏州).*|.*(江西).*|.*(陕西).*";
            Pattern pattern = Pattern.compile(p1);
            Matcher m = pattern.matcher(show);
            if(m.find()){
                String p2 = ".*北京市.*|.*天津市.*|.*郑州市.*|.*吉林省.*|.*南京市.*|.*四川省.*|.*苏州市.*|.*江西省.*|.*陕西省.*";
                if(Pattern.matches(p2, key)){
                    if(key.length() != 3){
                        data.put("areaIdPath", area.getAreaIdPath());
                        data.put("show", data.get("show")+" "+area.getArea());
                        return true;
                    }
                    if(first != null){
                        first.put("areaIdPath", area.getAreaIdPath());
                        first.put("show", area.getArea());
                        subDatas.add(first);
                    }
                }else{
                    boolean isShow = false;
                    for(int i=1;i<=m.groupCount();i++){
                        String group = m.group(i);
                        if(group != null && !area.getArea().contains(group)){
                            isShow = true;
                            break;
                        }
                    }
                    if(isShow){
                        data.put("areaIdPath", area.getAreaIdPath());
                        data.put("show", data.get("show")+" "+area.getArea());
                        return true;
                    }else{
                        if(first != null){
                            first.put("areaIdPath", area.getAreaIdPath());
                            first.put("show", area.getArea());
                            subDatas.add(first);
                        }
                    }
                }
            }else{
                data.put("areaIdPath", area.getAreaIdPath());
                data.put("show", data.get("show")+" "+area.getArea());
                return true;
            }
        }
        return false;
    }

    @PostConstruct
    protected void init() throws IOException {
        
        directory = FSDirectory.open(FileSystems.getDefault().getPath(this.getPath()));
        
        Map<String, String> indexArgs = new HashMap<String, String>();
        indexArgs.put("type", TYPE.index_ansj.name());
        indexArgs.put(DicLibrary.DEFAULT, "dic,dic1");
        indexArgs.put(StopLibrary.DEFAULT, "stop_dic1");
        indexArgs.put(AmbiguityLibrary.DEFAULT, "ambiguity");//无用，只是防止报错
        indexAnalyzer = new AnsjAnalyzer(indexArgs);
        
        
        Map<String, String> queryArgs = new HashMap<String, String>();
        queryArgs.put("type", TYPE.query_ansj.name());
        queryArgs.put(DicLibrary.DEFAULT, "dic,dic1");
        queryArgs.put(StopLibrary.DEFAULT, "stop_dic1");
        queryArgs.put(AmbiguityLibrary.DEFAULT, "ambiguity");
        queryAnalyzer = new AnsjAnalyzer(queryArgs);
        
        //AnalyzingInfixSuggester 该类是从源码拷贝，并修改后的
        suggester = new AnalyzingInfixSuggester(directory, indexAnalyzer, queryAnalyzer, 
                AnalyzingInfixSuggester.DEFAULT_MIN_PREFIX_CHARS, false);
        
        this.initAnalysis();
        this.initAfter();
    }
    
    protected void initAnalysis(){
        Forest forest1 = DicLibrary.get("dic");
        Forest forest2 = DicLibrary.get("dic1");
        this.analysis = new ToAnalysis().setForests(forest1,forest2);
        this.stopRecognition1 = StopLibrary.get("stop_dic1");
        this.stopRecognition2 = StopLibrary.get("stop_dic2");
    }
    
    protected void initAfter() throws IOException {
        
    }
    
    @PreDestroy
    protected void destroy(){
        
        try {
            if(suggester != null) suggester.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        
        try {
            if(directory != null) directory.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        
        if(indexAnalyzer != null) indexAnalyzer.close();
        
        if(queryAnalyzer != null) queryAnalyzer.close();
        
        this.destroyAfter();
        
    }
    
    protected void destroyAfter(){
        
    }
    
    protected String converModeType(Integer modeType){
        String text = "";
        if(modeType == null){
            return text;
        }
        switch (modeType) {
        case 1:
            text = "(国产)";
            break;
        case 2:
            text = "(中规)";
            break;
        case 3:
            text = "(中规)";
            break;
        case 4:
            text = "(美版)";
            break;
        case 6:
            text = "(中东)";
            break;
        case 8:
            text = "(加版)";
            break;
        case 10:
            text = "(欧版)";
            break;
        case 12:
            text = "(墨西哥版)";
            break;
        default:
            break;
        }
        return text;
    }
    
    class Area {
        private String area;
        
        private String areaIdPath;

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public String getAreaIdPath() {
            return areaIdPath;
        }

        public void setAreaIdPath(String areaIdPath) {
            this.areaIdPath = areaIdPath;
        }
    }
}
