package com.bagri.core.api.impl;

import java.util.Properties;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;

import org.w3c.dom.Node;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.BagriException;

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
	
	private XQItemAccessor checkCurrent() throws BagriException {
		Object current = getCurrent();
		if (current == null) {
			throw new BagriException("no current item", BagriException.ecQuery);
		}
		return (XQItemAccessor) current; 
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getBoolean() throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getBoolean();
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte getByte() throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getByte();
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDouble() throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getDouble();
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getFloat() throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getFloat();
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInt() throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getInt();
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLong() throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getLong();
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getNode() throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getNode();
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getObject() throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getObject();
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public short getShort() throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getShort();
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getString() throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getAtomicValue();
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public XQItemAccessor getXQItem() throws BagriException {
		return checkCurrent();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getItemAsString(Properties props) throws BagriException {
		XQItemAccessor ci = checkCurrent();
		try {
			return ci.getItemAsString(props);
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}

	
}
