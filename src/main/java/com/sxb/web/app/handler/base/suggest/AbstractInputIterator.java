package com.sxb.web.app.handler.base.suggest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;

public abstract class AbstractInputIterator implements InputIterator{

	protected Iterator<Car> carIterator;
	
	protected Car car;
	
	public AbstractInputIterator(Iterator<Car> carIterator) {
		this.carIterator = carIterator;
	}
	
	@Override
	public abstract BytesRef next() throws IOException;
	
	@Override
	public long weight() {
		return car.getWeight();
	}
	
	@Override
	public BytesRef payload() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        ObjectOutputStream out = new ObjectOutputStream(bos);
	        out.writeObject(car);
	        out.close();
	        return new BytesRef(bos.toByteArray());
		} catch (IOException e) {
            throw new RuntimeException("BrandAndSeriesInputIterator car Serializ error");
        }
	}
	
	@Override
	public boolean hasPayloads() {
		return true;
	}

	@Override
	public Set<BytesRef> contexts() {
		return null;
	}

	@Override
	public boolean hasContexts() {
		return false;
	}
	
	protected StringBuilder appendFilterString(StringBuilder builder,String str){
		this.appendString(builder, str);
		if(str.contains("-")){
			builder.append(" ");
			builder.append(str.replace("-", ""));
		}
		return builder;
	}
	
	protected StringBuilder appendString(StringBuilder builder,String str) {
		builder.append(" ");
		builder.append(str);
		return builder;
	}
	
	/**
	 * 获取车系数字
	 * @param series
	 * @return
	 */
	protected String getNumber(String series){
	    String regEx="[^0-9]";  
        Pattern p = Pattern.compile(regEx);  
        Matcher m = p.matcher(series);
        return m.replaceAll("").trim();
	}
	
	/**
	 * 获取车型年份
	 * @param category
	 * @return
	 */
	protected String getYear(String category){
	    //boolean m = Pattern.matches("^2\\d\\d\\d.*", category);
	    //if(m){
	    //    return category.substring(0, 4);
	    //}
	    Pattern p = Pattern.compile(".*(\\d\\d)款.*");
        Matcher m = p.matcher(category);
        if(m.matches()){
            String group = m.group(1);
            return group == null ? "" : group;
        }
	    return "";
	}
	
	/**
	 * 获取车型排量
	 * @param category
	 * @return
	 */
	protected String getDisplacement(String category){
	    String p =  ".*(\\d\\.\\d).*";
        Pattern pa = Pattern.compile(p);
        Matcher m = pa.matcher(category);
        if(m.matches()){
            String value = m.group(1);
            return value == null ? "" : value;
        }
        return "";
	}
	
	protected String converExpandModeType(int modeType){
        String text = "";
        switch (modeType) {
        case 1:
            text = "国产";
            break;
        case 2:
            text = "中规";
            break;
        case 3:
            text = "中规";
            break;
        case 4:
            text = "美版" + " " + "美规";
            break;
        case 6:
            text = "中东";
            break;
        case 8:
            text = "加版";
            break;
        case 10:
            text = "欧版" + " " + "欧规";
            break;
        case 12:
            text = "墨西哥版";
            break;
        default:
            break;
        }
        return text;
    }
	
	/**
     * 有些平行进口车，厂家字段上没有“平行进口”
     * 给车系加上规格
     * @param builder
     * @param car
     */
    protected void handleImportCar(StringBuilder builder,Car car) {
        Integer modeType = car.getModeType();
        if(modeType != null && modeType != 1){
            if(modeType > 3 && car.getFactoryName() != null && !car.getFactoryName().contains("平行进口")){
                this.appendString(builder, "平行进口");
            } else if(car.getFactoryName() != null && !car.getFactoryName().contains("进口")){
                this.appendString(builder, "进口");
            }
            this.appendString(builder, this.converExpandModeType(modeType));
        }
    }
}
