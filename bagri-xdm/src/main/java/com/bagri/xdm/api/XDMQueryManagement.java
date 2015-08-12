package com.bagri.xdm.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.xdm.domain.XDMQuery;

/**
 * XDMQueryManagement - client interface
 * 
 * @author Denis Sukhoroslov
 */
public interface XDMQueryManagement {
	
	Iterator executeXCommand(String command, Map bindings, Properties props) throws XDMException;
	Iterator executeXQuery(String query, Map bindings, Properties props) throws XDMException;
	
	Collection<String> getXML(ExpressionContainer query, String template, Map params) throws XDMException;

	Collection<Long> getDocumentIDs(ExpressionContainer query) throws XDMException;
	Collection<String> getDocumentURIs(ExpressionContainer query) throws XDMException;
	
	int getQueryKey(String query); 
	
}
