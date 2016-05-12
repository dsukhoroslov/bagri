package com.bagri.xdm.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

/**
 * XDMQueryManagement - client interface
 * 
 * @author Denis Sukhoroslov
 */
public interface XDMQueryManagement {
	
	Iterator<?> executeQuery(String query, Map<QName, Object> params, Properties props) throws XDMException;
	Collection<String> getDocumentUris(String query, Map<QName, Object> params, Properties props) throws XDMException;
	
	void cancelExecution() throws XDMException;
	
	Collection<String> prepareQuery(String query); //throws XDMException;
	int getQueryKey(String query); 
	
}
