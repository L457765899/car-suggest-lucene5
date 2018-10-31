package com.sxb.web.app.handler.base.suggest;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sxb.web.commons.util.RetUtil;

@Service("bFSCSuggestService")
public class BFSCSuggestServiceImpl extends AbstractSuggestServiceImpl implements SuggestService{
    
    public static final String SUGGEST_INDEX_PATH_BFSC = "/mnt/soft/tomcat/app_lucence/lucene/suggest/BFSC";
    
    @Resource(name="bFSCCSuggestService")
    private SuggestService suggestService;

    @Override
    protected String getPath() {
        return SUGGEST_INDEX_PATH_BFSC;
    }

    @Override
    public SuggestService getSuggestService() {
        return suggestService;
    }

    @Override
    protected Map<String, Object> handleResult(List<LookupResult> results,String key,Area area) throws Exception {
        
        List<Map<String,Object>> datas = new ArrayList<Map<String, Object>>();
        if(results.size() == 1){
            LookupResult lookupResult = results.get(0);
            Car car = this.convertBytesRef(lookupResult.payload);
            String subCategory = this.subCategory(car.getCategory(),0);
            
            Map<String,Object> firstData = new HashMap<String, Object>();
            firstData.put("series", car.getSeries());
            firstData.put("category", car.getCategory());
            firstData.put("modeType", car.getShowModeType());
            if(car.getOfficialQuote() != null && car.getOfficialQuote().intValue() > 0){
                firstData.put("officialQuote", car.getOfficialQuote());
                firstData.put("show", car.getSeries() + this.converModeType(car.getShowModeType()) 
                        + " " + subCategory + "/" + (car.getOfficialQuote()/100));
            }else{
                firstData.put("show", car.getSeries() + this.converModeType(car.getShowModeType()) 
                        + " " + subCategory);
            }
            datas.add(firstData);
            
            String colors = appCarcategoryService.SelectColorById(car.getCategoryId());
            if(StringUtils.hasLength(colors)){
                String[] colorArray = colors.split(",");
                for(String color : colorArray){
                    Map<String,Object> data = new HashMap<String, Object>();
                    data.put("series", car.getSeries());
                    data.put("category", car.getCategory());
                    data.put("color", color);
                    data.put("modeType", car.getShowModeType());
                    if(car.getOfficialQuote() != null && car.getOfficialQuote().intValue() > 0){
                        data.put("officialQuote", car.getOfficialQuote());
                        data.put("show", car.getSeries() + this.converModeType(car.getShowModeType()) 
                                + " " + subCategory + "/" + (car.getOfficialQuote()/100) + "/" + color);
                    }else{
                        data.put("show", car.getSeries() + this.converModeType(car.getShowModeType()) 
                                + " " + subCategory + "/" + color);
                    }
                    datas.add(data);
                }
            }
        }else{
            for(int i=0,len=results.size();i<len;i++){
                LookupResult lookupResult = results.get(i);
                Car car = this.convertBytesRef(lookupResult.payload);
                String subCategory = this.subCategory(car.getCategory(),i);
                
                Map<String,Object> data = new HashMap<String, Object>();
                data.put("series", car.getSeries());
                data.put("category", car.getCategory());
                data.put("modeType", car.getShowModeType());
                if(car.getOfficialQuote() != null && car.getOfficialQuote().intValue() > 0){
                    data.put("officialQuote", car.getOfficialQuote());
                    data.put("show", car.getSeries() + this.converModeType(car.getShowModeType()) 
                            + " " + subCategory + "/" + (car.getOfficialQuote()/100));
                }else{
                    data.put("show", car.getSeries() + this.converModeType(car.getShowModeType()) 
                            + " " + subCategory);
                }
                datas.add(data);
            }
        }
        
        return RetUtil.getRetValue(this.subDatas(datas,key,area));
    }

    @Override
    protected Iterator<Car> handleIndexData() {
        
    	Reader reader = RetUtil.getReader("categorys.json");
    	List<Car> datas = new Gson().fromJson(reader, new TypeToken<List<Car>>(){}.getType());
        
        return datas.iterator();
    }

    @Override
    protected InputIterator getInputIterator(Iterator<Car> iterator) {
        return new BFSCInputIterator(iterator);
    }

}
