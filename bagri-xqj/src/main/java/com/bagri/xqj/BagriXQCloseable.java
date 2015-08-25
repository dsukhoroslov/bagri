package com.bagri.xqj;

import javax.xml.xquery.XQException;

public abstract class BagriXQCloseable {
	
	protected boolean closed = false;
	
	public boolean isClosed() {
		return closed;
	}
	
	public void close() throws XQException {
		closed = true;
	}
	
	void checkState(String error) throws XQException {
		if (isClosed()) {
			throw new XQException(error);
		}
	}

}
