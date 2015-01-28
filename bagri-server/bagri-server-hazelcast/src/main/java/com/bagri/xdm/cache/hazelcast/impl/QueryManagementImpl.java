package com.bagri.xdm.cache.hazelcast.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.BinaryExpression;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.Expression;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathExpression;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.api.XQQueryManagement;
import com.bagri.xdm.cache.hazelcast.predicate.QueryPredicate;
import com.bagri.xdm.client.hazelcast.data.QueryParamsKey;
import com.bagri.xdm.client.hazelcast.impl.ResultsIterator;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.common.XDMResultsKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMIndexedValue;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.domain.XDMQuery;
import com.bagri.xdm.domain.XDMResults;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.ExceptionIterator;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class QueryManagementImpl implements XQQueryManagement {
	
	private static final transient Logger logger = LoggerFactory.getLogger(QueryManagementImpl.class);
	
	private RepositoryImpl repo;
	private XDMModelManagement model;
	private DocumentManagementImpl docMgr;
	
    private IMap<Integer, XDMQuery> xqCache;
    private IMap<XDMResultsKey, XDMResults> xrCache;
    private Map<Integer, XDMQuery> xQueries = new HashMap<Integer, XDMQuery>();
    
	private IMap<Long, XDMDocument> xddCache;
    private IMap<XDMDataKey, XDMElements> xdmCache;
    private IMap<XDMIndexKey, XDMIndexedValue> idxCache;
    
    public QueryManagementImpl() {
    	logger.info("<init>; query cache initialized");
    }
    
    public void setRepository(RepositoryImpl repo) {
    	this.repo = repo;
    	this.model = repo.getModelManagement();
    	this.docMgr = (DocumentManagementImpl) repo.getDocumentManagement();
    	this.xddCache = docMgr.getDocumentCache();
    	this.xdmCache = docMgr.getElementCache();
    	this.idxCache = docMgr.getIndexCache();
    }

    public void setQueryCache(IMap<Integer, XDMQuery> cache) {
    	this.xqCache = cache;
    }
    
    public void setResultCache(IMap<XDMResultsKey, XDMResults> cache) {
    	this.xrCache = cache;
    }
    
    // can inherit from some top ..
    @Override
    public int getQueryKey(String query) {
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

	//public abstract ExpressionBuilder getQuery(String query, Map bindings);
	
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

	
	public Set<Long> queryKeys(Set<Long> found, ExpressionContainer ec, Expression ex) {
		if (ex instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) ex;
			Set<Long> leftKeys = queryKeys(found, ec, be.getLeft());
			if (Comparison.AND.equals(be.getCompType())) {
				if (leftKeys.isEmpty()) {
					return leftKeys;
				}
				Set<Long> rightKeys = queryKeys(leftKeys, ec, be.getRight());
				return rightKeys;
			} else if (Comparison.OR.equals(be.getCompType())) {
				Set<Long> rightKeys = queryKeys(found, ec, be.getRight());
				leftKeys.addAll(rightKeys);
				return leftKeys;
			} else {
				throw new IllegalArgumentException("Wrong BinaryExpression type: " + be.getCompType());
			}
		}
		
		PathExpression pex = (PathExpression) ex;
		return queryPathKeys(found, pex, ec.getParam(pex));
	}

	protected Set<Long> queryPathKeys(Set<Long> found, PathExpression pex, Object value) {

		logger.trace("queryPathKeys.enter; found: {}", found == null ? "null" : found.size()); 
		Predicate pp = null;
		int pathId = 0;
		if (pex.isRegex()) {
			Set<Integer> pathIds = model.translatePathFromRegex(pex.getDocType(), pex.getRegex());
			logger.trace("queryPathKeys; regex: {}; pathIds: {}", pex.getRegex(), pathIds);
			if (pathIds.size() > 0) {
				// ?? use all of them !
				pp = Predicates.in("pathId", pathIds.toArray(new Integer[pathIds.size()]));
			}
		} else {
			String path = pex.getFullPath();
			logger.trace("queryPathKeys; path: {}; comparison: {}", path, pex.getCompType());
			XDMPath xPath = model.translatePath(pex.getDocType(), path, XDMNodeKind.fromPath(path));
			pp = Predicates.equal("pathId", xPath.getPathId());
			pathId = xPath.getPathId();
		}
		
		if (pp == null) {
			throw new IllegalArgumentException("Path not found for expression: " + pex);
		}
		
   		//try {
		//	long stamp = System.currentTimeMillis(); 
	   	//	Supplier<XDMDataKey, XDMElements, Object> supplier = Supplier.fromKeyPredicate(new DataKeyPredicate(pathId));
	   	//	Aggregation<XDMDataKey, Object, Long> aggregation = Aggregations.count();
	   	//	Long count = xdmCache.aggregate(supplier, aggregation);
	   	//	stamp = System.currentTimeMillis() - stamp;
		//	logger.info("queryPathKeys; got {} aggregation results; time taken: {}", count, stamp); 
   		//} catch (Throwable ex) {
   		//	logger.error("queryPathKeys", ex);
   		//}
		
		if (pathId > 0 && Comparison.EQ.equals(pex.getCompType()) && 
				model.isPathIndexed(pathId)) {
			XDMIndexKey idx = docMgr.getXdmFactory().newXDMIndexKey(pathId, value.toString());
			XDMIndexedValue xidx = idxCache.get(idx);
			logger.trace("queryPathKeys; seach for index {} get {}", idx, xidx); 
			if (xidx != null) {
				Set<Long> result;
				if (found == null) {
					result = xidx.getDocumentIds();
				} else {
					found.retainAll(xidx.getDocumentIds());
					result = found;
				}
				logger.trace("queryPathKeys.exit; returning {} indexed keys", result.size()); 
				return result;
			}
		}
   		
   		Predicate<XDMDataKey, XDMElements> f = Predicates.and(pp, new QueryPredicate(pex, value));
   		Set<XDMDataKey> xdmKeys = xdmCache.keySet(f);
		logger.trace("queryPathKeys; got {} query results", xdmKeys.size()); 
		Set<Long> result = new HashSet<Long>(xdmKeys.size());
		if (found == null) {
			for (XDMDataKey key: xdmKeys) {
				result.add(key.getDocumentId());
			}
		} else {
			for (XDMDataKey key: xdmKeys) {
				long docId = key.getDocumentId();
				if (found.contains(docId)) {
					result.add(docId);
				}
			}
		}
		logger.trace("queryPathKeys.exit; returning {} keys", result.size()); 
		return result;
	}

	@Override
	public Collection<Long> getDocumentIDs(ExpressionContainer query) {
		ExpressionBuilder exp = query.getExpression();
		if (exp.getRoot() != null) {
			return queryKeys(null, query, exp.getRoot());
		}
		logger.info("getDocumentIDs; got rootless path: {}", query); 
		// can we use local keySet only !?
		return xddCache.keySet();
	}
	
	@Override
	public Collection<String> getDocumentURIs(ExpressionContainer query) {
		// TODO: remove this method completely, or
		// make reverse cache..? or, make URI from docId somehow..
		Collection<Long> ids = getDocumentIDs(query);
		Set<String> result = new HashSet<String>(ids.size());
		// TODO: better to do this via EP or aggregator?
		Map<Long, XDMDocument> docs = xddCache.getAll(new HashSet<Long>(ids));
		for (XDMDocument doc: docs.values()) {
			result.add(doc.getUri());
		}
		return result;
	}

	@Override
	public Collection<String> getXML(ExpressionContainer query, String template, Map params) {
		Collection<Long> docIds = getDocumentIDs(query);
		if (docIds.size() > 0) {
			return docMgr.buildDocument(new HashSet<Long>(docIds), template, params);
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public Iterator executeXCommand(String command, Map bindings, Properties props) {
		
		return execXQCommand(false, command, bindings, props);
	}

	@Override
	public Iterator executeXQuery(String query, Map bindings, Properties props) {

		return execXQCommand(true, query, bindings, props);
	}

	private Iterator execXQCommand(boolean isQuery, String xqCmd, Map bindings, Properties props) {

		logger.trace("execXQCommand.enter; query: {}, command: {}; bindings: {}", isQuery, xqCmd, bindings);
		ResultsIterator result = null;
		Iterator iter = null;
		String clientId = props.getProperty("clientId");
		int batchSize = Integer.parseInt(props.getProperty("batchSize", "0"));
		try {
			//if (isQuery) {
			//	iter = queryManager.getQueryResults(xqCmd, bindings, props);
			//}
			
			if (iter == null) {
				XQProcessor xqp = repo.getXQProcessor(clientId);
				for (Object o: bindings.entrySet()) {
					Map.Entry<QName, Object> var = (Map.Entry<QName, Object>) o; 
					xqp.bindVariable(var.getKey(), var.getValue());
				}
				
				if (isQuery) {
					iter = xqp.executeXQuery(xqCmd, props);
				} else {
					iter = xqp.executeXCommand(xqCmd, bindings, props);
				}
				
				for (Object o: bindings.entrySet()) {
					Map.Entry<QName, Object> var = (Map.Entry<QName, Object>) o; 
					xqp.unbindVariable(var.getKey());
				}
				
			//	iter = queryManager.addQueryResults(xqCmd, bindings, props, iter);
			}
			result = createCursor(clientId, batchSize, iter, false);
		} catch (Throwable ex) {
			logger.error("execXQCommand.error;", ex);
			result = createCursor(clientId, batchSize, new ExceptionIterator(ex), true);
		}
		logger.trace("execXQCommand.exit; returning: {}, for client: {}", result, clientId);
		return result;
	}

	private ResultsIterator createCursor(String clientId, int batchSize, Iterator iter, boolean failure) {
		final ResultsIterator xqCursor = new ResultsIterator(clientId, batchSize, iter, failure);
		
		// TODO: put everything to the queue - can be too slow!
		// think of async solution..
		// profile: it takes 3.19 ms to serialize!
		// another profile session: 13 ms spent in toData conversion!
		// 12.5 ms: SaxonXQProcessor.convertToString -> net.sf.saxon.query.QueryResult.serializeSequence (11 ms)

		// but async serialization takes even more time! because of the thread context switch, most probably
		//IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
		//execService.execute(new Runnable() {
		//	@Override
		//	public void run() {
		//		xqCursor.serialize(hzInstance);
		//	}
		//});
		
		xqCursor.serialize(repo.getHzInstance());
		return xqCursor;
	}

	
}
