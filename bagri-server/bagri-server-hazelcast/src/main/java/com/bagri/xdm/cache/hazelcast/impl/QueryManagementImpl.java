package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.XDMConstants.pn_client_fetchSize;
import static com.bagri.xdm.common.XDMConstants.pn_client_id;
import static com.bagri.xdm.common.XDMConstants.pn_query_command;
import static com.bagri.xquery.api.XQUtils.getAtomicValue;
import static com.bagri.xquery.api.XQUtils.isStringTypeCompatible;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.stats.watch.StopWatch;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.impl.QueryManagementBase;
import com.bagri.xdm.cache.api.ModelManagement;
import com.bagri.xdm.cache.api.QueryManagement;
import com.bagri.xdm.cache.hazelcast.predicate.DocsAwarePredicate;
import com.bagri.xdm.cache.hazelcast.predicate.QueryPredicate;
import com.bagri.xdm.cache.hazelcast.predicate.ResultsDocPredicate;
import com.bagri.xdm.cache.hazelcast.predicate.ResultsQueryPredicate;
import com.bagri.xdm.client.hazelcast.data.QueryParamsKey;
import com.bagri.xdm.client.hazelcast.impl.FixedCursor;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.common.DataKey;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.common.QueryResultKey;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.domain.Elements;
import com.bagri.xdm.domain.Path;
import com.bagri.xdm.domain.Query;
import com.bagri.xdm.domain.QueryResult;
import com.bagri.xdm.query.AlwaysExpression;
import com.bagri.xdm.query.BinaryExpression;
import com.bagri.xdm.query.Comparison;
import com.bagri.xdm.query.Expression;
import com.bagri.xdm.query.ExpressionBuilder;
import com.bagri.xdm.query.ExpressionContainer;
import com.bagri.xdm.query.PathExpression;
import com.bagri.xdm.query.QueriedPath;
import com.bagri.xdm.query.QueryBuilder;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.XQIterator;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class QueryManagementImpl extends QueryManagementBase implements QueryManagement {
	
	private static final transient Logger logger = LoggerFactory.getLogger(QueryManagementImpl.class);
	
	private SchemaRepositoryImpl repo;
	private ModelManagement model;
    private IndexManagementImpl idxMgr;
	private DocumentManagementImpl docMgr;
	//private TransactionManagementImpl txMgr;
    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;
	
    private ReplicatedMap<Integer, Query> xqCache;
    private IMap<Long, QueryResult> xrCache;
    
    private IMap<DataKey, Elements> xdmCache;
	private IMap<DocumentKey, Document> xddCache;
    
	private StopWatch stopWatch;
	private BlockingQueue<StatisticsEvent> timeQueue;
	
	private boolean testMode = false; 

	private ThreadLocal<QueryExecContext> thContext = new ThreadLocal<QueryExecContext>() {
		
		@Override
		protected QueryExecContext initialValue() {
			return new QueryExecContext();
 		}
	};
	
    public QueryManagementImpl() {
    	logger.info("<init>; query cache initialized");
    }
    
    public void setRepository(SchemaRepositoryImpl repo) {
    	this.repo = repo;
    	this.model = repo.getModelManagement();
    	this.docMgr = (DocumentManagementImpl) repo.getDocumentManagement();
    	//this.txMgr = (TransactionManagementImpl) repo.getTxManagement();
    	this.xddCache = docMgr.getDocumentCache();
    	this.xdmCache = docMgr.getElementCache();
    	docMgr.setRepository(repo);
    }

    public void setQueryCache(ReplicatedMap<Integer, Query> cache) {
    //public void setQueryCache(IMap<Integer, XDMQuery> cache) {
    	this.xqCache = cache;
    }
    
    public void setResultCache(IMap<Long, QueryResult> cache) {
    	this.xrCache = cache;
    }
    
    public void setIndexManager(IndexManagementImpl indexManager) {
    	this.idxMgr = indexManager;
    }
    
    public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
    	this.queue = queue;
    }

    public void setStatsEnabled(boolean enable) {
    	this.enableStats = enable;
    }
    
    public void setTestMode(boolean testMode) {
    	this.testMode = testMode;
    }
    
	public void setStopWatch(StopWatch stopWatch) {
		this.stopWatch = stopWatch;
	}
	
    public void setTimeQueue(BlockingQueue<StatisticsEvent> timeQueue) {
    	this.timeQueue = timeQueue;
    }

	@Override
	public Query getQuery(String query) {
		Integer qCode = getQueryKey(query);
		//XDMQuery result = xQueries.get(qCode);
		Query result = xqCache.get(qCode);
		if (result != null) {
		//	result = xqCache.get(qCode);
		//} else {
			result = result.clone();
		}
		updateStats(query, result != null, 1);
		logger.trace("getQuery.exit; returning {}", result);
		return result;
	}

	@Override
	public boolean addQuery(String query, boolean readOnly, QueryBuilder xdmQuery) {
		Integer qKey = getQueryKey(query);
		//logger.trace("addQuery.enter; got code: {}; query cache size: {}", qCode, xQueries.size());
		//boolean result = xqCache.putIfAbsent(qCode, new XDMQuery(query, readOnly, xdmQuery)) == null;
		boolean result = xqCache.put(qKey, new Query(query, readOnly, xdmQuery)) == null;
		logger.trace("addQuery.exit; returning: {}", result);
		return result;
	}
	
	public void removeQueries(Set<Integer> qKeys) {
		for (Integer qKey: qKeys) {
			xqCache.remove(qKey);
		}
	}
	
	public Set<Integer> getQueriesForPaths(Collection<Integer> pathIds, boolean checkIndexed) {
		// TODO: also specify: do we care about unique indexes or not..
		logger.trace("getQueriesForPaths.enter; got pathIds: {}; query cache size: {}", pathIds, xqCache.size());
		Set<Integer> result = new HashSet<>();
		for (Map.Entry<Integer, Query> e: xqCache.entrySet()) {
			for (ExpressionContainer ec: e.getValue().getXdmQuery().getContainers()) {
				boolean foundPath = false;
				for (Expression ex: ec.getBuilder().getExpressions()) {
					if (!foundPath && ex.isCached()) {
						QueriedPath qp = ((PathExpression) ex).getCachedPath();
						if (checkIndexed && !qp.isIndexed()) {
							continue;
						}
						for (Integer pid: pathIds) {
							if (qp.getPathIds().contains(pid)) {
								foundPath = true;
								break;
							}
						}
					}
				}
				if (foundPath) {
					result.add(e.getKey());
					break;
				}
			}
		}
		logger.trace("getQueriesForPaths.exit; returning: {}", result);
		return result;
	}

	@Override
	public Iterator<?> getQueryResults(String query, Map<QName, Object> params, Properties props) {
		//QueryParamsKey qpKey = getResultsKey(query, params);
		long qpKey = getResultsKey(query, params);
		logger.trace("getQueryResults; got result key: {}; parts: {}", qpKey, getResultsKeyParts(qpKey));
		QueryResult xqr = xrCache.get(qpKey);
		//XDMResults xqr = xResults.get(qpKey);
		Iterator<?> result = null;
		if (xqr != null) {
			result = xqr.getResults().iterator();
			updateStats(query, 0, 1);
		} else {
			updateStats(query, 0, -1);
		}
		logger.trace("getQueryResults; returning: {}", xqr);
		return result;
	}
	
	@Override
	public Iterator<?> addQueryResults(String query, Map<QName, Object> params, Properties props, Iterator<?> results) {
		//QueryParamsKey qpKey = getResultsKey(query, params);
		QueryExecContext ctx = thContext.get();
		if (ctx.getDocIds().size() == 0) {
			return results;
		}
		long qpKey = getResultsKey(query, params);
		// TODO: think about lazy solution... EntryProcessor? or, try local Map?
		List<Object> resList = new ArrayList<>();
		while (results.hasNext()) {
			//Object res = results.next();
			//logger.info("addQueryResults; caching result: {}; {}", res == null ? "null" : res.getClass().getName(), res); 
			//resList.add(res); 
			resList.add(results.next());
		}
		if (resList.size() == 0) {
			logger.warn("addQueryResults; got empty results but docs were found: {}", ctx.getDocIds());
			return results;
		}
		QueryResult xqr = new QueryResult(params, ctx.getDocIds(), resList);
		//XDMResults oldRes = 
		xrCache.putAsync(qpKey, xqr);
		//xResults.put(qpKey, xqr);
		updateStats(query, 1, 0);
		logger.trace("addQueryResults; stored results: {} for key: {}", xqr, qpKey);
		return xqr.getResults().iterator();
	}
	
	public void invalidateQueryResults(Set<Integer> paths) {
		Set<Integer> qKeys = getQueriesForPaths(paths, false);
		if (!qKeys.isEmpty()) {
			removeQueryResults(qKeys);
		}
	}
	
	public void removeQueryResults(long docId) {
		logger.trace("removeQueryResults.enter; got docId: {}; result cache size: {}", docId, xrCache.size());
		Predicate rdp = new ResultsDocPredicate(docId);
		Set<Long> rdKeys = xrCache.keySet(rdp);
		for (Long rdKey: rdKeys) {
			xrCache.delete(rdKey);
		}
		logger.trace("removeQueryResults.exit; deleted {} results for docId: {}", rdKeys.size(), docId);
	}

	public void removeQueryResults(Collection<Integer> queryIds) {
		logger.trace("removeQueryResults.enter; got queryIds: {}; result cache size: {}", queryIds, xrCache.size());
		Predicate rqp = new ResultsQueryPredicate(queryIds);
		Set<Long> rqKeys = xrCache.keySet(rqp);
		for (Long rqKey: rqKeys) {
			xrCache.delete(rqKey);
		}
		logger.trace("removeQueryResults.exit; deleted {} results", rqKeys.size());
	}
	
	@Override
	public void clearCache() {
		xqCache.clear(); //evictAll();
		xrCache.evictAll();
	}
	
	private void updateStats(String name, boolean success, int count) {
		if (enableStats) {
			if (!queue.offer(new StatisticsEvent(name, success, new Object[] {count}))) {
				logger.warn("updateStats; queue is full!!");
			}
		}
	}
	
	private void updateStats(String name, int results, int hits) {
		if (enableStats) {
			// ??? weird stats format!
			if (!queue.offer(new StatisticsEvent(name, hits > 0, new Object[] {results, true}))) {
				logger.warn("updateQueryStats; queue is full!!");
			}
		}
	}
	
	public Set<Long> queryKeys(Set<Long> found, ExpressionContainer ec, Expression ex) throws XDMException {
		if (ex instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) ex;
			Set<Long> leftKeys = queryKeys(found, ec, be.getLeft());
			if (Comparison.AND == be.getCompType()) {
				if (leftKeys.isEmpty()) {
					return leftKeys;
				}
				Set<Long> rightKeys = queryKeys(leftKeys, ec, be.getRight());
				return rightKeys;
			} else if (Comparison.OR == be.getCompType()) {
				Set<Long> rightKeys = queryKeys(found, ec, be.getRight());
				leftKeys.addAll(rightKeys);
				return leftKeys;
			} else {
				throw new XDMException("Wrong BinaryExpression type: " + be.getCompType(), XDMException.ecQuery);
			}
		}
		
		if (ex instanceof AlwaysExpression) {
			AlwaysExpression ae = (AlwaysExpression) ex;
			Collection<Long> docKeys = docMgr.getCollectionDocumentKeys(ae.getCollectionId());
			return new HashSet<Long>(docKeys);
		}
		
		PathExpression pex = (PathExpression) ex;
		return queryPathKeys(found, pex, ec.getParam(pex));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object adjustSearchValue(Object value, int pathType) {
		if (value == null) {
			return null;
		}
		int valType = XQItemType.XQBASETYPE_ANYTYPE;
		if (value instanceof Collection) {
			Collection values = (Collection) value;
			if (values.size() == 0) {
				return null;
			}
			if (values.size() == 1) {
				value = values.iterator().next();
			} else {
				// CompType must be IN !
				List newVals = new ArrayList(values.size());
				for (Object val: values) {
					newVals.add(adjustSearchValue(val, pathType));
				}
				return newVals;
			}
		} 
		if (value instanceof XQItem) {
			try {
				valType = ((XQItem) value).getItemType().getBaseType();
				value = ((XQItem) value).getObject();
			} catch (XQException ex) {
				logger.error("adjustSearchValue.error getting XQItem", ex);
				value = value.toString();
				valType = XQItemType.XQBASETYPE_STRING;
			}
		}
		
		if (pathType != valType) {
			if (isStringTypeCompatible(pathType)) {
				value = value.toString();
			} else {				
				// conversion from value type to path type
				value = getAtomicValue(pathType, value.toString());
			}
		}
		return value;
	}

	protected Set<Long> queryPathKeys(Set<Long> found, PathExpression pex, Object value) throws XDMException {

		logger.trace("queryPathKeys.enter; found: {}; value: {}", (found == null ? "null" : found.size()), value);
		Predicate pp = null;
		int dataType = 0;
		boolean indexed = true;
		Set<Integer> paths = null;
		boolean cached = pex.isCached();
		if (cached) {
			QueriedPath qPath = pex.getCachedPath();
			dataType = qPath.getDataType();
			indexed = qPath.isIndexed();
			paths = new HashSet<>(qPath.getPathIds());
			if (paths.size() > 1) {
				Integer[] pa = paths.toArray(new Integer[paths.size()]); 
				pp = Predicates.in("pathId", pa);
			} else {
				pp = Predicates.equal("pathId", paths.iterator().next());
			}
		} else {
			if (pex.isRegex()) {
				// pass "any" docType here..
				paths = model.translatePathFromRegex(0, pex.getRegex());
				logger.trace("queryPathKeys; regex: {}; pathIds: {}", pex.getRegex(), paths);
				if (paths.size() > 0) {
					Integer[] pa = paths.toArray(new Integer[paths.size()]); 
					pp = Predicates.in("pathId", pa);
					Path xPath = model.getPath(pa[0]);
					dataType = xPath.getDataType();
				}
			} else {
				String path = pex.getFullPath();
				logger.trace("queryPathKeys; path: {}; comparison: {}", path, pex.getCompType());
				Path xPath = model.getPath(path);
				if (xPath != null) {
					paths = new HashSet<>(1);
					pp = Predicates.equal("pathId", xPath.getPathId());
					paths.add(xPath.getPathId());
					dataType = xPath.getDataType();
				}
			}
		}
		
		if (paths == null || paths.isEmpty()) {
			logger.info("queryPathKeys; got query on unknown path: {}", pex); 
			return Collections.emptySet();
		}
		Object newVal = adjustSearchValue(value, dataType);
		if (newVal == null) {
			logger.info("queryPathKeys; got query on empty value sequence: {}", value); 
			return Collections.emptySet();
		}
		logger.trace("queryPathKeys; adjusted value: {}({})", newVal.getClass().getName(), newVal); 
		
		Set<Long> result = new HashSet<>();
		if (indexed) {
			for (Integer pathId: paths) {
				Set<Long> docKeys = idxMgr.getIndexedDocuments(pathId, pex, newVal);
				logger.trace("queryPathKeys; search for index - got keys: {}", docKeys == null ? null : docKeys.size()); 
				if (docKeys != null) {
					if (found == null) {
						result.addAll(docKeys);
					} else {
						found.retainAll(docKeys);
						result = found;
					}
				} else {
					//fallback to full scan below..
					result = null;
					break;
				}
			}
		
			if (result != null) {
				logger.trace("queryPathKeys.exit; returning {} indexed keys", result.size());
				if (!cached) {
					pex.setCachedPath(dataType, indexed, paths);
				}
				return result;
			}
			indexed = false;
		}
		
		QueryPredicate qp;
		if (found == null) {
			qp = new QueryPredicate(pex, newVal);
		} else {
			qp = new DocsAwarePredicate(pex, newVal, found);
		}			
		Predicate<DataKey, Elements> f = Predicates.and(pp, qp);
	   	Set<DataKey> xdmKeys = xdmCache.keySet(f);
		logger.trace("queryPathKeys; got {} query results", xdmKeys.size()); 
		result = new HashSet<>(xdmKeys.size());
		for (DataKey key: xdmKeys) {
			result.add(key.getDocumentKey());
		}
		logger.trace("queryPathKeys.exit; returning {} keys", result.size()); 
		if (!cached) {
			pex.setCachedPath(dataType, indexed, paths);
		}
		return result;
	}

	private Collection<Long> checkDocumentsCommited(Collection<Long> docKeys, int clnId) throws XDMException {
		Iterator<Long> itr = docKeys.iterator();
		if (clnId > 0) {
			while (itr.hasNext()) {
				long docKey = itr.next();
				if (!docMgr.checkDocumentCollectionCommited(docKey, clnId)) {
					itr.remove();
				}
			}
		} else {
			while (itr.hasNext()) {
				long docKey = itr.next();
				if (!docMgr.checkDocumentCommited(docKey)) {
					itr.remove();
				}
			}
		}
		return docKeys;
	}

	@Override
	public Collection<String> getContent(ExpressionContainer query, String template, Map params) throws XDMException {
		
		Collection<Long> docKeys = getDocumentIds(query);
		if (docKeys.size() > 0) {
			return docMgr.buildDocument(new HashSet<>(docKeys), template, params);
		}
		return Collections.emptyList();
	}
	
	public Collection<Long> getDocumentIds(ExpressionContainer query) throws XDMException {
		if (query != null) {
			ExpressionBuilder exp = query.getBuilder();
			if (exp != null && exp.getRoot() != null) {
				// TODO: check stats for exp.getRoot().getCollectionId(), 
				// build 'found' set here if collectionId is selective enough
				Set<Long> ids = queryKeys(null, query, exp.getRoot());
				// otherwise filter out documents with wrong collectionIds here
				Collection<Long> result = checkDocumentsCommited(ids, exp.getRoot().getCollectionId());
				thContext.get().setDocIds(result);
				return result;
			}
		}
		logger.info("getDocumentIds; got rootless path: {}", query); 
		
		// fallback to full IDs set: default collection over all documents. not too good...
		// how could we distribute it over cache nodes? can we use local keySet only !?
		List<Long> result = new ArrayList<Long>(xddCache.keySet().size());
		for (DocumentKey docKey: xddCache.keySet()) {
			// we must provide only visible docIds!
			if (docMgr.checkDocumentCommited(docKey.getKey())) {
				result.add(docKey.getKey());
			}
		}
		// I don't want to cache all docIds here in result
		return result;
	}

	@Override
	public Collection<String> getDocumentUris(String query, Map<QName, Object> params, Properties props) throws XDMException {
		logger.trace("getDocumentUris.enter; query: {}, command: {}; params: {}; properties: {}", query, params, props);
		Collection<Long> keys = getDocumentKeys(query, params, props);
		List<String> result = new ArrayList<>(keys.size());
		for (Long docKey: keys) {
			Document doc = docMgr.getDocument(docKey);
			result.add(doc.getUri());
		}
		logger.trace("getDocumentUris.exit; returning: {}", result);
		return result;
	}

	public Collection<Long> getDocumentKeys(String query, Map<QName, Object> params, Properties props) throws XDMException {
		logger.trace("getDocumentKeys.enter; query: {}, command: {}; params: {}; properties: {}", query, params, props);
		Collection<Long> result = null;
		try {
			Iterator<?> iter = runQuery(query, params, props);
			result = thContext.get().getDocIds();
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
		logger.trace("getDocumentKeys.exit; returning: {}", result);
		return result;
	}
	
	@Override
	public boolean isReadOnlyQuery(String query) {

		if (testMode) {
			return true;
		}
		
		Integer qCode = getQueryKey(query);
		Query xQuery = xqCache.get(qCode);
		//XDMQuery xQuery = this.getQuery(query);
		if (xQuery == null) {
			//not cached yet, returning false, just to be safe..
			return false;
			// calc it via xqp...
			//XQProcessor xqp = repo.getXQProcessor(clientId);
		}
		return xQuery.isReadOnly();
	}

	@Override
	public void cancelExecution() throws XDMException {
		// no-op on the server side
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<?> executeQuery(String query, Map<QName, Object> params, Properties props) throws XDMException {

		logger.trace("executeQuery.enter; query: {}; params: {}; properties: {}", query, params, props);
		ResultCursor result = null;
		String clientId = props.getProperty(pn_client_id);
		int batchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
		try {
			XQProcessor xqp = repo.getXQProcessor(clientId);
			Iterator<?> iter = runQuery(query, params, props);
			result = createCursor(clientId, batchSize, iter);
			xqp.setResults(result);
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
		logger.trace("executeQuery.exit; returning: {}, for client: {}", result, clientId);
		return result;
	}
	
	@Override
	public Collection<String> prepareQuery(String query) {
		// not used on the server side?
		logger.info("prepareQuery; query: {}", query);
		return null;
	}

	private Iterator<?> runQuery(String query, Map<QName, Object> params, Properties props) throws XQException {
		
        Throwable ex = null;
        boolean failed = false;
        stopWatch.start();
		
		Iterator<?> iter = null;
		String clientId = props.getProperty(pn_client_id);
		boolean isQuery = "false".equalsIgnoreCase(props.getProperty(pn_query_command, "false"));
		XQProcessor xqp = repo.getXQProcessor(clientId);

		try {
			if (testMode) {
				iter = Collections.emptyIterator();
			} else {
				int qCode = getQueryKey(query);
				if (isQuery) {
					if (xqCache.containsKey(qCode)) {
						iter = getQueryResults(query, params, props);
					}
				}
				
				xqp.setResults(null);
				if (iter == null) {
					QueryExecContext ctx = thContext.get();
					ctx.clear();
				
					if (params != null) {
						for (Map.Entry<QName, Object> var: params.entrySet()) {
							xqp.bindVariable(var.getKey(), var.getValue());
						}
					}
					
					if (isQuery) {
						iter = xqp.executeXQuery(query, props);
					} else {
						iter = xqp.executeXCommand(query, params, props);
					}
					
					if (params != null) {
						for (Map.Entry<QName, Object> var: params.entrySet()) {
							xqp.unbindVariable(var.getKey());
						}
					}
	
					//XDMQuery xquery = xqCache.get(qCode);
					//if (xquery != null && xquery.isReadOnly()) {
					if (xqCache.containsKey(qCode)) {
						iter = addQueryResults(query, params, props, iter);
					}
				}
			}
			
        } catch (Throwable t) {
        	//t.printStackTrace();
            failed = true;
            ex = t;
        }
        long stamp = stopWatch.stop();
        if (!timeQueue.offer(new StatisticsEvent(query, !failed, new Object[] {stamp}))) {
        	logger.warn("runQuery: the timeQueue is full!!");
        }
        if (failed) {
            throw new XQException(ex.getMessage());
        }
			
		return iter;
	}

	private ResultCursor createCursor(String clientId, int batchSize, Iterator iter) {
		int size = ResultCursor.UNKNOWN;
		if (iter instanceof XQIterator) {
			size = ((XQIterator) iter).getFullSize();
		}
		final ResultCursor xqCursor;
		if (batchSize == 1) {
			if (iter.hasNext()) {
				xqCursor = new FixedCursor(clientId, batchSize, iter.next());
			} else {
				xqCursor = new FixedCursor(clientId, batchSize, null);
			}
		} else {
			xqCursor = new ResultCursor(clientId, batchSize, iter, size);
		}
		
		// async serialization takes even more time! because of the thread context switch, most probably
		//IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
		//execService.execute(new Runnable() {
		//	@Override
		//	public void run() {
		//		xqCursor.serialize(hzInstance);
		//	}
		//});
		
		int count = xqCursor.serialize(repo.getHzInstance());
		logger.trace("createCursor.exit; serialized: {} results", count);
		return xqCursor;
	}
	
	private class QueryExecContext {
		
		private Collection<Long> docIds;
		
		void clear() {
			this.docIds = null;
		}
		
		Collection<Long> getDocIds() {
			if (docIds == null) {
				return Collections.emptyList();
			}
			return docIds;
		}
		
		void setDocIds(Collection<Long> docIds) {
			this.docIds = docIds;
		}
		
	}

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

