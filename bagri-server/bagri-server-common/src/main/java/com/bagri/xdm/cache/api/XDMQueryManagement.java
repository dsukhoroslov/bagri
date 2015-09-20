package com.bagri.xdm.cache.api;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.QueryBuilder;
import com.bagri.xdm.domain.XDMQuery;

public interface XDMQueryManagement extends com.bagri.xdm.api.XDMQueryManagement {
	
	// the below methods are for server implementation,
	// we don't need them in the client API! but, we use them from XQProcesserImpl!

	boolean isReadOnlyQuery(String query);
	
	XDMQuery getQuery(String query); //, Map bindings);
	boolean addQuery(String query, boolean readOnly, QueryBuilder xdmQuery);
	//void addExpression(String query, boolean readOnly, Object xqExpression);
	//void addExpression(String query, boolean readOnly, QueryBuilder xdmQuery);

	Iterator getQueryResults(String query, Map<String, Object> params, Properties props);
	Iterator addQueryResults(String query, Map<String, Object> params, Properties props, Iterator results);
	
	void clearCache();

}
