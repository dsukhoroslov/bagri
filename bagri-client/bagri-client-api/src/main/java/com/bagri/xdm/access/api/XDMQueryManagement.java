package com.bagri.xdm.access.api;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.xdm.domain.XDMQuery;

/**
 * XDMQueryManagement - client interface
 * 
 * @author Denis Sukhoroslov
 */
public interface XDMQueryManagement {
	
	XDMQuery getQuery(String query); //, Map bindings);
	boolean addQuery(String query, Object xqExpression, ExpressionBuilder xdmExpression);
	void addExpression(String query, Object xqExpression);
	void addExpression(String query, ExpressionBuilder xdmExpression);

	Iterator getQueryResults(String query, Map<String, Object> params, Properties props);
	Iterator addQueryResults(String query, Map<String, Object> params, Properties props, Iterator results);
	
}
