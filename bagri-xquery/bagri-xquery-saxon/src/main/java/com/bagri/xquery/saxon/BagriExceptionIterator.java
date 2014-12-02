package com.bagri.xquery.saxon;

import java.util.Iterator;

public class BagriExceptionIterator implements Iterator {
	
	private Throwable failure;
	
	public BagriExceptionIterator(Throwable failure) {
		this.failure = failure;
	}

	@Override
	public boolean hasNext() {
		return failure != null; 
	}

	@Override
	public Object next() {
		Object result = failure;
		if (failure != null) {
			failure = failure.getCause();
		}
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove not supported");
	}
	
	

}
