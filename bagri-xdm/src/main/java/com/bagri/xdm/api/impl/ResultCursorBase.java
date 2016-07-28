package com.bagri.xdm.api.impl;

import java.util.Properties;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;

import org.w3c.dom.Node;

import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.XDMException;

/**
 * Base implementation for ResultCursor. Accessor methods are implemented  
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class ResultCursorBase implements ResultCursor {

	public static final int ONE = 1;
	public static final int EMPTY = 0;
	public static final int ONE_OR_MORE = -1;
	public static final int UNKNOWN = -2;
	
	protected int position;

	protected abstract Object getCurrent();
	
	private XQItemAccessor checkCurrent() throws XDMException {
		Object current = getCurrent();
		if (current == null) {
			throw new XDMException("no current item", XDMException.ecQuery);
		}
		return (XQItemAccessor) current; 
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getBoolean() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getBoolean();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte getByte() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getByte();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDouble() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getDouble();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getFloat() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getFloat();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInt() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getInt();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLong() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getLong();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getNode() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getNode();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getObject() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getObject();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public short getShort() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getShort();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getString() throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getAtomicValue();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public XQItemAccessor getXQItem() throws XDMException {
		return checkCurrent();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getItemAsString(Properties props) throws XDMException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getItemAsString(props);
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}

	
}
