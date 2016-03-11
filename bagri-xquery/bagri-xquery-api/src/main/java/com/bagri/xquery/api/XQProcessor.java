package com.bagri.xquery.api;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQStaticContext;

public interface XQProcessor extends QueryProcessor {

	Iterator<?> executeXCommand(String command, Map<QName, Object> params, XQStaticContext ctx) throws XQException;
	
    Iterator<?> executeXCommand(String command, Map<QName, Object> params, Properties props) throws XQException;

    Iterator<?> getResults();
    void setResults(Iterator<?> itr);
    
    void cancelExecution() throws XQException;
    // Saxon specific conversion
    // TODO: move this out of the interface!
	String convertToString(Object item, Properties props) throws XQException;
	
}
