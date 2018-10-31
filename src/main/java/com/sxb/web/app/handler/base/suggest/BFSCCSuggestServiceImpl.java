package com.sxb.web.app.handler.base.suggest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sxb.web.commons.util.RetUtil;

@Service("bFSCCSuggestService")
public class BFSCCSuggestServiceImpl extends AbstractSuggestServiceImpl implements SuggestService{
    
    public static final String SUGGEST_INDEX_PATH_BFSCC = "/mnt/soft/tomcat/app_lucence/lucene/suggest/BFSCC";
    
    @Override
    protected String getPath() {
        return SUGGEST_INDEX_PATH_BFSCC;
    }

    @Override
    protected Map<String, Object> handleResult(List<LookupResult> results,String key,Area area) throws Exception {
        
        List<Map<String,Object>> datas = new ArrayList<>();
        for(int i=0,len=results.size();i<len;i++){
            LookupResult lookupResult = results.get(i);
            Car car = this.convertBytesRef(lookupResult.payload);
            String subCategory = this.subCategory(car.getCategory(),i);
            //subCategory = appCarcategoryService.reduceCategory(subCategory);
            
            Map<String,Object> data = new HashMap<>();
            data.put("series", car.getSeries());
            data.put("category", car.getCategory());
            data.put("modeType", car.getShowModeType());
            if(car.getOfficialQuote() != null && car.getOfficialQuote().intValue() > 0){
                data.put("officialQuote", car.getOfficialQuote());
                if(StringUtils.hasLength(car.getOutColor())){
                    data.put("color", car.getOutColor());
                    data.put("show", car.getSeries() + this.converModeType(car.getShowModeType()) 
                            + " " + subCategory + "/" + (car.getOfficialQuote()/100) + "/" + car.getOutColor());
                }else{
                    data.put("show", car.getSeries() + this.converModeType(car.getShowModeType()) 
                            + " " + subCategory + "/" + (car.getOfficialQuote()/100));
                }
            }else{
                if(StringUtils.hasLength(car.getOutColor())){
                    data.put("color", car.getOutColor());
                    data.put("show", car.getSeries() + this.converModeType(car.getShowModeType()) 
                            + " " + subCategory + "/" + car.getOutColor());
                }else{
                    data.put("show", car.getSeries() + this.converModeType(car.getShowModeType()) 
                            + " " + subCategory);
                }
            }
            datas.add(data);
        }
        
        return RetUtil.getRetValue(this.subDatas(datas,key,area));
    }

    @Override
    protected Iterator<Car> handleIndexData() {
        
        List<Map<String, Object>> list = appCarcategoryService.getAllForIndex();
        List<Car> datas = new ArrayList<>();
        for(Map<String, Object> seriesMap : list){
            Integer categoryId = (Integer) seriesMap.get("id");
            String brand = (String) seriesMap.get("brand");
            String factoryName = (String) seriesMap.get("factoryName");
            String series = (String) seriesMap.get("series");
            String category = (String) seriesMap.get("category");
            Integer officialQuote = (Integer) seriesMap.get("officialQuote");
            String outColor = (String) seriesMap.get("outColor");
            Integer modeType = (Integer) seriesMap.get("modeType");
            
            if(StringUtils.hasLength(outColor)){
                String[] colors = outColor.split(",");
                for(String color : colors){
                    Car car = new Car();
                    car.setBrand(brand);
                    car.setFactoryName(this.convertNull(factoryName));
                    car.setSeries(series);
                    car.setCategoryId(categoryId);
                    car.setCategory(category);
                    car.setOfficialQuote(officialQuote);
                    car.setOutColor(color);
                    car.setModeType(modeType);
                    datas.add(car);
                }
            }else{
                Car car = new Car();
                car.setBrand(brand);
                car.setFactoryName(this.convertNull(factoryName));
                car.setSeries(series);
                car.setCategoryId(categoryId);
                car.setCategory(category);
                car.setOfficialQuote(officialQuote);
                car.setModeType(modeType);
                datas.add(car);
            }
        }
        
        return datas.iterator();
    }

    @Override
    protected InputIterator getInputIterator(Iterator<Car> iterator) {
        return new BFSCCInputIterator(iterator);
    }

    @Override
    public SuggestService getSuggestService() {
        return null;
    }

}
