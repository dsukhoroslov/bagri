package com.bagri.xdm.process.hazelcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.xdm.access.api.XDMQueryManagement;
import com.bagri.xdm.access.hazelcast.data.QueryParamsKey;
import com.bagri.xdm.common.XDMResultsKey;
import com.bagri.xdm.domain.XDMQuery;
import com.bagri.xdm.domain.XDMResults;
import com.hazelcast.core.IMap;

public class QueryManagementServer implements XDMQueryManagement {
	
	private static final transient Logger logger = LoggerFactory.getLogger(QueryManagementServer.class);
	
    private IMap<Integer, XDMQuery> xqCache;
    private IMap<XDMResultsKey, XDMResults> xrCache;
    private Map<Integer, XDMQuery> xQueries = new HashMap<Integer, XDMQuery>();
    
    public QueryManagementServer() {
    	logger.info("<init>; query cache initialized");
    }

    public void setQueryCache(IMap<Integer, XDMQuery> cache) {
    	this.xqCache = cache;
    }
    
    public void setResultCache(IMap<XDMResultsKey, XDMResults> cache) {
    	this.xrCache = cache;
    }
    
    private int getQueryKey(String query) {
    	// will use cifer hash later..
    	return query.hashCode();
    }
    
	private long getParamsKey(Map<String, Object> params) {
		final int prime = 31;
		int result = params.size();
		for (Map.Entry param: params.entrySet()) {
			result = prime * result	+ param.getKey().hashCode();
			result = prime * result + param.getValue().hashCode();
		}
		return result;
	}

	@Override
	public XDMQuery getQuery(String query) {
		Integer qCode = getQueryKey(query);
		logger.trace("getQuery.enter; got code: {}; query cache size: {}", qCode, xQueries.size());
		XDMQuery result = xQueries.get(qCode);
		//if (result != null) {
		//	Object xqExpression = xqObjects.get(qCode);
		//	if (xqExpression != null) {
		//		result = new XDMQuery(query, xqExpression, result.getXdmExpression());
		//	}
		//}
		logger.trace("getQuery.exit; returning {}", result);
		return result;
	}

	@Override
	public boolean addQuery(String query, Object xqExpression, ExpressionBuilder xdmExpression) {
		Integer qCode = getQueryKey(query);
		logger.trace("addQuery.enter; got code: {}; query cache size: {}", qCode, xQueries.size());
		boolean result = false;
		//if (xqCache.tryLock(qCode)) {
		//	try {
		result = xQueries.put(qCode, new XDMQuery(query, xqExpression, xdmExpression)) == null;
		//		xqObjects.put(qCode, xqExpression);
		//	} finally {
		//		xqCache.unlock(qCode);
		//	}
		//}
		logger.trace("addQuery.exit; returning: {}", result);
		return result;
	}

	@Override
	public void addExpression(String query, Object xqExpression) {
		Integer qCode = getQueryKey(query);
		logger.trace("addExpression.enter; got code: {}; query cache size: {}", qCode, xQueries.size());
		ExpressionBuilder xdmExpression = null;
		XDMQuery xQuery = xQueries.get(qCode);
		if (xQuery != null) {
			xdmExpression = xQuery.getXdmExpression(); 
		}
		xQuery = new XDMQuery(query, xqExpression, xdmExpression);
		//if (xqCache.tryLock(qCode)) {
		//	try {
		xQueries.put(qCode, xQuery);
		//		xqObjects.put(qCode, xqExpression);
		//	} finally {
		//		xqCache.unlock(qCode);
		//	}
		//} else {
		//	xqObjects.put(qCode, xqExpression);
		//}
	}

	@Override
	public void addExpression(String query, ExpressionBuilder xdmExpression) {
		Integer qCode = getQueryKey(query);
		logger.trace("addExpression.enter; got code: {}; query cache size: {}", qCode, xQueries.size());
		Object xqExpression = null;
		XDMQuery xQuery = xQueries.get(qCode);
		if (xQuery != null) {
			xqExpression = xQuery.getXqExpression(); 
		}
		xQuery = new XDMQuery(query, xqExpression, xdmExpression);
		//if (xqCache.tryLock(qCode)) {
		//	try {
		xQueries.put(qCode, xQuery);
		//		xqObjects.put(qCode, xqExpression);
		//	} finally {
		//		xqCache.unlock(qCode);
		//	}
		//}
	}
	
	private QueryParamsKey getResultsKey(String query, Map<String, Object> params) {
		// should we check query ache first ??
		return new QueryParamsKey(getQueryKey(query), getParamsKey(params));
	}

	@Override
	public Iterator getQueryResults(String query, Map<String, Object> params, Properties props) {
		QueryParamsKey qpKey = getResultsKey(query, params);
		XDMResults xqr = xrCache.get(qpKey); 
		if (xqr != null) {
			return xqr.getResults().iterator();
		}
		return null;
	}
	
	@Override
	public Iterator addQueryResults(String query, Map<String, Object> params, Properties props, Iterator results) {
		QueryParamsKey qpKey = getResultsKey(query, params);
		// TODO: think about lazy solution..
		List resList = new ArrayList();
		while (results.hasNext()) {
			resList.add(results.next());
		}
		XDMResults xqr = new XDMResults(params, Collections.EMPTY_LIST, resList);
		//XDMResults oldRes = 
		xrCache.putAsync(qpKey, xqr);
		return xqr.getResults().iterator();
	}

	
}
