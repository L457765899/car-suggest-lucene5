package com.sxb.web.app.handler.base.suggest;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.library.StopLibrary;
import org.ansj.lucene5.AnsjAnalyzer;
import org.ansj.lucene5.AnsjAnalyzer.TYPE;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sxb.web.commons.util.RetUtil;
import com.sxb.web.db.sxb.model.Geographic;

@Service("bSSuggestService")
public class BSSuggestServiceImpl extends AbstractSuggestServiceImpl implements SuggestService{
	
	public static Map<Integer,List<Integer>> PRICE_MAP = null;
    
    public static final String SUGGEST_INDEX_PATH_BS = "/mnt/soft/tomcat/app_lucence/lucene/suggest/BS";
    
    public static final String SUGGEST_INDEX_PATH_A = "/mnt/soft/tomcat/app_lucence/lucene/suggest/A";
    
    private static final String[] citys = {"重庆市","天津市","上海市","北京市"};
    
    protected Directory adirectory;
    
    protected DirectoryReader adirectoryReader;
    
    protected IndexSearcher aindexSearcher;
    
    protected Analyzer aanalyzer;

    @Resource(name="bFSSuggestService")
    private SuggestService suggestService;

    @Override
    protected String getPath() {
        return SUGGEST_INDEX_PATH_BS;
    }

    @Override
    public SuggestService getSuggestService() {
        return suggestService;
    }
    
    private void setIndexSearcher() throws IOException{
        if(aindexSearcher == null){
            adirectoryReader = DirectoryReader.open(adirectory);
            aindexSearcher = new IndexSearcher(adirectoryReader);
        }
    }
    
    protected Query getQuery(String key){
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        TokenStream stream = null;  
        try {  
            stream = aanalyzer.tokenStream("", new StringReader(key));  
            CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);  
            stream.reset();  
            boolean b = false;
            while(stream.incrementToken()){
                String at = attr.toString();
                if(at.length() >= 2){
                    b = true;
                    //System.out.println("词元:"+at);
                    builder.add(new PrefixQuery(new Term("name",at)), Occur.SHOULD);
                }
            }  
            if(!b){
                return null;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {  
                if(stream != null) stream.close();  
            } catch (IOException e) {  
                logger.error(e.getMessage(), e);
            }
        }  
        return builder.build();
    }
    
    protected Area handlArea(String key) throws IOException{
        Query query = this.getQuery(key);
        if(query == null){
            return null;
        }
        this.setIndexSearcher();
        SortField sortField = new SortField("weight",SortField.Type.INT,true);
        Sort sort = new Sort(sortField);
        TopDocs topDocs = aindexSearcher.search(query, 1, sort);
        if(topDocs.totalHits > 0){
            Document document = aindexSearcher.doc(topDocs.scoreDocs[0].doc);
            Area area = new Area();
            area.setArea(document.get("name"));
            area.setAreaIdPath(document.get("idPath"));
            return area;
        }
        return null;
    }
    
    @Override
    public Map<String, Object> searchSuggest(String key,Area area) throws Exception {
        
        if(area == null){
            area = this.handlArea(key);
        }
        
        return super.searchSuggest(key, area);
        
    }

    @Override
    protected Map<String, Object> handleResult(List<LookupResult> results,String key,Area area) throws Exception {
        
        List<Map<String,Object>> datas = new ArrayList<Map<String, Object>>();
        if(results.size() == 1){
            LookupResult lookupResult = results.get(0);
            Car car = this.convertBytesRef(lookupResult.payload);
            
            Map<String,Object> firstData = new HashMap<String, Object>();
            firstData.put("brand", car.getBrand());
            firstData.put("series", car.getSeries());
            firstData.put("modeType", car.getShowModeType());
            firstData.put("show", car.getBrand() + " " + car.getSeries() + this.converModeType(car.getShowModeType()));
            datas.add(firstData);
            
            List<Integer> officialQuotes = PRICE_MAP.get(car.getSeriesId());
            if(officialQuotes != null && officialQuotes.size() > 0){
                List<Integer> exist = new ArrayList<Integer>();
                for(Integer officialQuote : officialQuotes){
                    if(officialQuote != null && officialQuote.intValue() > 0){
                        if(!exist.contains(officialQuote)){
                            exist.add(officialQuote);
                            Map<String,Object> data = new HashMap<String, Object>();
                            data.put("brand", car.getBrand());
                            data.put("series", car.getSeries());
                            data.put("officialQuote", officialQuote);
                            data.put("modeType", car.getShowModeType());
                            data.put("show", car.getBrand() + " " + car.getSeries() 
                                    + this.converModeType(car.getShowModeType()) + " " + (officialQuote/100));
                            datas.add(data);
                        }
                    }
                }
            }
        }else{
            Set<String> brands = new LinkedHashSet<String>();
            for(LookupResult lookupResult : results){
                Car car = this.convertBytesRef(lookupResult.payload);
                if(car.getBrand().contains(key)){
                    brands.add(car.getBrand());
                }else if(key.contains(car.getBrand())){
                    brands.add(car.getBrand());
                }
                Map<String,Object> data = new HashMap<String, Object>();
                data.put("brand", car.getBrand());
                data.put("series", car.getSeries());
                data.put("modeType", car.getShowModeType());
                data.put("show", car.getBrand() + " "+car.getSeries() + this.converModeType(car.getShowModeType()));
                datas.add(data);
            }
            if(brands.size() > 0){
                List<Map<String,Object>> brandDatas = new ArrayList<Map<String, Object>>();
                for(String brand : brands){
                    Map<String,Object> brandData = new HashMap<String, Object>();
                    brandData.put("brand", brand);
                    brandData.put("show", brand);
                    brandDatas.add(brandData);
                }
                brandDatas.addAll(datas);
                datas = brandDatas;
            }
        }
        
        return RetUtil.getRetValue(this.subDatas(datas,key,area));
    }

    @Override
    protected Iterator<Car> handleIndexData() {
        
    	Reader reader = RetUtil.getReader("series.json");
    	List<Car> datas = new Gson().fromJson(reader, new TypeToken<List<Car>>(){}.getType());
        
        return datas.iterator();
    }

    @Override
    protected InputIterator getInputIterator(Iterator<Car> iterator) {
        return new BSInputIterator(iterator);
    }

    @Override
    public void indexSuggest() throws IOException {
        
        super.indexSuggest();
        
        List<Geographic> geographics = new Gson().fromJson(RetUtil.getReader("geographics.json"),
        		new TypeToken<List<Geographic>>(){}.getType());
        
        IndexWriter indexWriter = null;
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(aanalyzer);
            indexWriter = new IndexWriter(adirectory, indexWriterConfig);
            indexWriter.deleteAll();
            for(Geographic g : geographics){
                
                if(ArrayUtils.contains(citys, g.getName())){
                    String[] ids = g.getPathunid().split(",");
                    if(ids.length >= 5){
                        g.setPathunid(ids[0]+","+ids[1]+","+ids[2]+","+ids[3]+","+ids[4]);
                    }
                }
                
                Document document = new Document();
                document.add(new StringField("name", g.getName(), Store.YES));
                document.add(new StringField("idPath", g.getPathunid(), Store.YES));
                
                int indexOf = ArrayUtils.indexOf(SPECIAL_ALL_AREA, g.getName());
                if(indexOf > -1){
                    document.add(new IntField("weight", indexOf*100, Store.YES));  
                    document.add(new NumericDocValuesField("weight",indexOf*100));  
                }else{
                    document.add(new IntField("weight", 1000, Store.YES));  
                    document.add(new NumericDocValuesField("weight",1000));  
                }
                
                indexWriter.addDocument(document);
            }
            indexWriter.commit();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if(indexWriter != null) indexWriter.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void initAfter() throws IOException {
        adirectory = FSDirectory.open(FileSystems.getDefault().getPath(SUGGEST_INDEX_PATH_A));
        Map<String, String> args = new HashMap<String, String>();
        args.put("type", TYPE.query_ansj.name());
        args.put(DicLibrary.DEFAULT, "dic,dic1");
        args.put(StopLibrary.DEFAULT, "stop_dic1");
        args.put(AmbiguityLibrary.DEFAULT, "ambiguity");//无用，只是兼容
        aanalyzer = new AnsjAnalyzer(args);
        
        Reader reader = RetUtil.getReader("price.json");
        List<Map<String,Integer>> datas = new Gson().fromJson(reader, 
        		new TypeToken<List<Map<String,Integer>>>(){}.getType());
        
        PRICE_MAP = new HashMap<Integer, List<Integer>>();
        for(Map<String,Integer> data : datas){
        	int id = data.get("carSries_id");
        	int price = data.get("officialQuote");
        	if(PRICE_MAP.containsKey(id)){
        		List<Integer> list = PRICE_MAP.get(id);
        		list.add(price);
        	}else{
        		List<Integer> list = new ArrayList<Integer>();
        		list.add(price);
        		PRICE_MAP.put(id, list);
        	}
        }
    }

    @Override
    protected void destroyAfter() {
        try {
            if(adirectory != null) adirectory.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            if(adirectoryReader != null) adirectoryReader.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        if(aanalyzer != null) aanalyzer.close();
    }
    
}
