package com.sxb.web.app.handler.base.suggest;

import java.io.Serializable;

public class Car implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public Car() {
        
    }
    
    public Car(Car car) {
        this.brand = car.getBrand();
        this.factoryName = car.getFactoryName();
        this.series = car.getSeries();
        this.category = car.getCategory();
        this.officialQuote = car.getOfficialQuote();
        this.outColor = car.getOutColor();
        this.modeType = car.getModeType();
    }

	private String brand;
	
	private String factoryName;
	
	private Integer seriesId;
	
	private String series;
	
	private Integer categoryId;
	
	private String category;
	
	private Integer officialQuote;
	
	private String outColor;
	
	private Integer modeType;

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getOfficialQuote() {
		return officialQuote;
	}

	public void setOfficialQuote(Integer officialQuote) {
		this.officialQuote = officialQuote;
	}

	public String getOutColor() {
		return outColor;
	}

	public void setOutColor(String outColor) {
		this.outColor = outColor;
	}
	
	public long getWeight(){
		if(modeType == null){
		    return 50;
		}else if(modeType == 2 || modeType == 3){
			return 10;
		}else if(modeType > 3){
            return 0;
        }
		boolean fn = false;
		if(this.factoryName.contains(this.brand)){
			fn = true;
			return 100;
		}
		if(this.series.contains(this.brand)){
			if(fn){
				return 150;
			}else{
				return 100;
			}
		}
		return 50;
	}

    public Integer getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getModeType() {
        return modeType;
    }

    public void setModeType(Integer modeType) {
        this.modeType = modeType;
    }
    
    public Integer getShowModeType(){
        if(modeType != null && modeType > 3){
            return modeType;
        }
        return null;
    }
}
