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
	
	Iterator executeXCommand(String command, Map bindings, Properties props);
	Iterator executeXQuery(String query, Map bindings, Properties props);
	
	Collection<String> getXML(ExpressionContainer query, String template, Map params);

	Collection<Long> getDocumentIDs(ExpressionContainer query);
	Collection<String> getDocumentURIs(ExpressionContainer query);
	
	
	// the below methods are for server implementation,
	// we don't need them in the client API! but, we need to use them from XQProcesserImpl!
	
	XDMQuery getQuery(String query); //, Map bindings);
	boolean addQuery(String query, Object xqExpression, ExpressionBuilder xdmExpression);
	void addExpression(String query, Object xqExpression);
	void addExpression(String query, ExpressionBuilder xdmExpression);

	Iterator getQueryResults(String query, Map<String, Object> params, Properties props);
	Iterator addQueryResults(String query, Map<String, Object> params, Properties props, Iterator results);
	
}
