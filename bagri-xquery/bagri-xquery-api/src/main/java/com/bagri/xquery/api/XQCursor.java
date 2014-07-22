package com.bagri.xquery.api;

import java.util.Properties;

//import javax.xml.xquery.XQException;

public interface XQCursor<T> {

	T current();
	boolean hasNext();
	T next();
	//int position();
	void close(); //  throws XQException; ?

	Properties getProperties();
	void setProperties(Properties props);
	
}
