package com.bagri.xdm.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.bagri.xdm.common.XDMDocumentId;

/**
 * XDMQueryManagement - client interface
 * 
 * @author Denis Sukhoroslov
 */
public interface XDMQueryManagement {
	
	Iterator<?> executeQuery(String query, Map params, Properties props) throws XDMException;
	Collection<XDMDocumentId> getDocumentIds(String query, Map params, Properties props) throws XDMException;
	
	void cancelExecution() throws XDMException;
	
	int getQueryKey(String query); 
	
}
