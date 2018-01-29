package com.bagri.core.api.impl;

import static com.bagri.support.util.XQUtils.mapFromSequence;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.BagriException;

/**
 * Base implementation for ResultCursor. Accessor methods are implemented  
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class ResultCursorBase<T> implements ResultCursor<T> {

    protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final int ONE = 1;
	public static final int EMPTY = 0;
	public static final int ONE_OR_MORE = -1;
	public static final int UNKNOWN = -2;
	
	protected int position;

	public abstract List<T> getList() throws BagriException;

	public abstract boolean add(T result);
	
	public abstract void finish();
	
}
