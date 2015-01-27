package com.bagri.xquery.saxon;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

public class ExceptionIterator implements Iterator {
	
	private Throwable failure;
	
	public ExceptionIterator(Throwable failure) {
		this.failure = failure;
	}

	@Override
	public boolean hasNext() {
		return failure != null; 
	}

	@Override
	public Object next() {
		//Object result = failure;
		//if (failure != null) {
		//	failure = failure.getCause();
		//}
		//return result;
		if (failure != null) {
			StringWriter sw = new StringWriter();
			failure.printStackTrace(new PrintWriter(sw));
			sw.flush();
			failure = null;
			return sw.toString();
		}
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove not supported");
	}
	
	

}
