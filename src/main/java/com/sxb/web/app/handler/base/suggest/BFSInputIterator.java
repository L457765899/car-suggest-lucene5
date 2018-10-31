package com.sxb.web.app.handler.base.suggest;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;


public class BFSInputIterator extends AbstractInputIterator implements InputIterator{
	
	public BFSInputIterator(Iterator<Car> carIterator) {
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
			return new BytesRef(builder.toString().getBytes("UTF8"));
		}else{
			return null;
		}
	}
}
