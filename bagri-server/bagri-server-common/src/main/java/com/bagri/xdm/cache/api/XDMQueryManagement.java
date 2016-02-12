package com.bagri.xdm.cache.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.QueryBuilder;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.XDMQuery;

public interface XDMQueryManagement extends com.bagri.xdm.api.XDMQueryManagement {
	
	// the below methods are for server implementation,
	// we don't need them in the client API! but, we use them from XQProcesserImpl!

	Collection<Long> getDocumentIds(ExpressionContainer query) throws XDMException;
	// TODO: move params into query
	Collection<String> getContent(ExpressionContainer query, String template, Map params) throws XDMException;
	
	boolean isReadOnlyQuery(String query);
	
	XDMQuery getQuery(String query); //, Map bindings);
	boolean addQuery(String query, boolean readOnly, QueryBuilder xdmQuery);

	Iterator<?> getQueryResults(String query, Map<QName, Object> params, Properties props);
	Iterator<?> addQueryResults(String query, Map<QName, Object> params, Properties props, Iterator<?> results);
	
	void clearCache();

}
