package com.bagri.xdm.api.impl;

import java.util.Iterator;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;

import org.w3c.dom.Node;

import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.XDMException;

public abstract class ResultCursorBase implements ResultCursor {

	public static final int ONE = 1;
	public static final int EMPTY = 0;
	public static final int ONE_OR_MORE = -1;
	public static final int UNKNOWN = -2;
	
	protected int position;

	//protected Iterator<Object> iter;
	
	//protected ResultCursorBase(Iterator<Object> iter) {
	//	this.iter = iter;
	//}
	
	//@Override
	//public void close() {
		//logger.trace("close.enter; position: {}", position);
	//	iter = null;
		//current = null;
	//}
	
	protected abstract Object getCurrent();
	
	private XQItemAccessor checkCurrent() throws XDMException {
		Object current = getCurrent();
		if (current == null) {
			throw new XDMException("no current item", XDMException.ecQuery);
		}
		return (XQItemAccessor) current; 
	}
	
	public boolean getBoolean() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getBoolean();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	public byte getByte() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getByte();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	public double getDouble() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getDouble();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	public float getFloat() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getFloat();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	public int getInt() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getInt();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	public long getLong() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getLong();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	public Node getNode() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getNode();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	public Object getObject() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getObject();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	public short getShort() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getShort();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	public String getString() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getAtomicValue();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	public XQItemAccessor getXQItem() throws XDMException {
		return checkCurrent();
	}

	//public boolean getNext() throws XDMException {
	//	boolean result = hasNext();
	//	if (result) {
	//		next();
		//} else {
		//	current = null;
	//	}
	//	return result;
	//}
	
	//@Override
	//public void remove() {
	//	throw new UnsupportedOperationException("remove not supported");
	//}
	
}
