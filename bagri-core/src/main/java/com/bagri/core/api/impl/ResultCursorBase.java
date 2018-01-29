package com.bagri.core.api.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCursor;
//import com.bagri.core.api.BagriException;

/**
 * Base implementation for ResultCursor. Accessor methods are implemented  
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class ResultCursorBase<T> implements ResultCursor<T> {

    //protected Logger logger = LoggerFactory.getLogger(getClass());
	
	//protected int position;

	public abstract List<T> getList(); // throws BagriException;

	public abstract boolean add(T result);
	
	public abstract void finish();
	
}
