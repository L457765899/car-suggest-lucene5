package com.sxb.web.app.handler.base.suggest;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;
import org.springframework.util.StringUtils;

public class BFSCCInputIterator extends AbstractInputIterator implements InputIterator{

    public BFSCCInputIterator(Iterator<Car> carIterator) {
        super(carIterator);
    }

    @Override
    public BytesRef next() throws IOException {
        if(carIterator.hasNext()){
            car = carIterator.next();
            StringBuilder builder = new StringBuilder();
            builder.append(car.getBrand());
            this.appendFilterString(builder, car.getFactoryName());
            this.appendFilterString(builder, car.getSeries());
            this.handleImportCar(builder, car);
            this.appendString(builder, car.getCategory());
            
            if(car.getOfficialQuote() != null && car.getOfficialQuote().intValue() > 0){
                Integer OfficialQuote = car.getOfficialQuote()/100;
                this.appendString(builder, OfficialQuote.toString());
                
                String number = this.getNumber(car.getSeries());
                String year = this.getYear(car.getCategory());
                String displacement = this.getDisplacement(car.getCategory());
                
                if(StringUtils.hasLength(number)){
                    this.appendString(builder, number + OfficialQuote);
                    this.appendString(builder, number + year);
                    this.appendString(builder, number + OfficialQuote + year);
                    this.appendString(builder, number + year + OfficialQuote);
                    
                    //额外加的
                    if(StringUtils.hasLength(displacement)){
                        this.appendString(builder, number + displacement);
                        
                        this.appendString(builder, number + OfficialQuote + displacement);
                        this.appendString(builder, number + displacement + OfficialQuote);
                        
                        this.appendString(builder, number + year + displacement);
                        this.appendString(builder, number + displacement + year);
                    }
                }
                
                this.appendString(builder, OfficialQuote + year);
                this.appendString(builder, year + OfficialQuote);
                
                //额外加的
                if(StringUtils.hasLength(displacement)){
                    this.appendString(builder, displacement + OfficialQuote);
                    this.appendString(builder, OfficialQuote + displacement);
                    
                    this.appendString(builder, displacement + year);
                    this.appendString(builder, year + displacement);
                    
                    this.appendString(builder, OfficialQuote + displacement+year);
                    this.appendString(builder, OfficialQuote + year + displacement);
                    
                    this.appendString(builder, year + displacement + OfficialQuote);
                    this.appendString(builder, year + OfficialQuote + displacement);
                    
                    this.appendString(builder, displacement + OfficialQuote + year);
                    this.appendString(builder, displacement + year + OfficialQuote);
                }
                
            }
            
            if(StringUtils.hasLength(car.getOutColor())){
                this.appendString(builder, car.getOutColor());
            }
            
            return new BytesRef(builder.toString().getBytes("UTF8"));
        }else{
            return null;
        }
    }

}
