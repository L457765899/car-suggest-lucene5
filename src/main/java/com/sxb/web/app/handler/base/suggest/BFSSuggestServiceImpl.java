package com.sxb.web.app.handler.base.suggest;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sxb.web.commons.util.RetUtil;

@Service("bFSSuggestService")
public class BFSSuggestServiceImpl extends AbstractSuggestServiceImpl implements SuggestService{
    
    public static final String SUGGEST_INDEX_PATH_BFS = "/mnt/soft/tomcat/app_lucence/lucene/suggest/BFS";
    
    @Resource(name="bFSCSuggestService")
    private SuggestService suggestService;

    @Override
    protected String getPath() {
        return SUGGEST_INDEX_PATH_BFS;
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
            
            Map<String,Object> firstData = new HashMap<String, Object>();
            firstData.put("factoryName", car.getFactoryName());
            firstData.put("series", car.getSeries());
            firstData.put("modeType", car.getShowModeType());
            firstData.put("show", car.getFactoryName() + " " + car.getSeries() + this.converModeType(car.getShowModeType()));
            datas.add(firstData);
            
            List<Integer> officialQuotes = BSSuggestServiceImpl.PRICE_MAP.get(car.getSeriesId());
            if(officialQuotes != null && officialQuotes.size() > 0){
                List<Integer> exist = new ArrayList<Integer>();
                for(Integer officialQuote : officialQuotes){
                    if(officialQuote != null && officialQuote.intValue() > 0){
                        if(!exist.contains(officialQuote)){
                            exist.add(officialQuote);
                            Map<String,Object> data = new HashMap<String, Object>();
                            data.put("factoryName", car.getFactoryName());
                            data.put("series", car.getSeries());
                            data.put("officialQuote", officialQuote);
                            data.put("modeType", car.getShowModeType());
                            data.put("show", car.getFactoryName() + " " + car.getSeries() 
                                    + this.converModeType(car.getShowModeType()) + " " + (officialQuote/100));
                            datas.add(data);
                        }
                    }
                }
            }
        }else{
            Set<String> factoryNames = new LinkedHashSet<String>();
            for(LookupResult lookupResult : results){
                Car car = this.convertBytesRef(lookupResult.payload);
                if( car.getFactoryName().replace("-", "").contains(key.replace("-", "")) ){
                    factoryNames.add(car.getFactoryName());
                }else if( key.replace("-", "").contains(car.getFactoryName().replace("-", "")) ){
                    factoryNames.add(car.getFactoryName());
                }
                Map<String,Object> data = new HashMap<String, Object>();
                data.put("factoryName", car.getFactoryName());
                data.put("series", car.getSeries());
                data.put("modeType", car.getShowModeType());
                data.put("show", car.getFactoryName() + " " + car.getSeries() + this.converModeType(car.getShowModeType()));
                datas.add(data);
            }
            if(factoryNames.size() > 0){
                List<Map<String,Object>> factoryNameDatas = new ArrayList<Map<String, Object>>();
                for(String factoryName : factoryNames){
                    Map<String,Object> brandData = new HashMap<String, Object>();
                    brandData.put("factoryName", factoryName);
                    brandData.put("show", factoryName);
                    factoryNameDatas.add(brandData);
                }
                factoryNameDatas.addAll(datas);
                datas = factoryNameDatas;
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
        return new BFSInputIterator(iterator);
    }

}
