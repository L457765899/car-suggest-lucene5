package com.sxb.web.app.handler.base.suggest;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;
import org.springframework.util.StringUtils;


public class BSInputIterator extends AbstractInputIterator implements InputIterator{
	
	public BSInputIterator(Iterator<Car> carIterator) {
		super(carIterator);
	}

	@Override
	public BytesRef next() throws IOException {
	    if(carIterator.hasNext()){
            car = carIterator.next();
            Car copyCar = new Car(car);
            this.seriesFilterfactory(copyCar);
            StringBuilder builder = new StringBuilder();
            builder.append(copyCar.getBrand());
            this.appendFilterString(builder, copyCar.getSeries());
            this.handleImportCar(builder, copyCar);
            return new BytesRef(builder.toString().getBytes("UTF8"));
        }else{
            return null;
        }
	}
	
	/**
	 * 从车系中过滤厂家，防止想收厂家，反而搜出了车系
	 * @param car
	 * @return
	 */
	protected Car seriesFilterfactory(Car car) {
	    String factoryName = car.getFactoryName();
		if(StringUtils.hasLength(factoryName)
		        && car.getSeries().contains(factoryName)
		        && !car.getBrand().equals(factoryName)){
		    String p = "^[\u4e00-\u9fa5a-zA-Z]+$";
		    boolean m = Pattern.matches(p, factoryName);
		    if(m){//字母和中文混合的厂家，单独处理
		        factoryName = factoryName.replaceAll("[a-zA-Z]+", "");
		    }
		    car.setSeries(car.getSeries().replace(factoryName, ""));
		}
		
		return car;
	}
	
	/**
	 * 给进口车添加关键字，方便检索，因为“进口厂家”没有办法搜索
	 * @param builder
	 * @param copyCar
	 */
	protected void handleImportCar(StringBuilder builder,Car car) {
	    int modeType = car.getModeType();
	    if(modeType == 1){
	        return;
	    }
        
        this.appendString(builder, this.converExpandModeType(modeType));
        this.appendString(builder, "进口");
    }
}
