package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.Constants.pn_client_fetchSize;
import static com.bagri.xdm.common.Constants.pn_client_id;
import static com.bagri.xdm.common.Constants.pn_query_command;
import static com.bagri.xdm.common.Constants.pn_scrollability;
import static com.bagri.xdm.common.Constants.xdm_schema_fetch_size;
import static com.bagri.xquery.api.XQUtils.getAtomicValue;
import static com.bagri.xquery.api.XQUtils.isStringTypeCompatible;

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
import java.util.concurrent.BlockingQueue;

import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.stats.watch.StopWatch;
import com.bagri.common.util.CollectionUtils;
import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.impl.QueryManagementBase;
import com.bagri.xdm.api.impl.ResultCursorBase;
import com.bagri.xdm.cache.api.ModelManagement;
import com.bagri.xdm.cache.api.QueryManagement;
import com.bagri.xdm.cache.hazelcast.predicate.DocsAwarePredicate;
import com.bagri.xdm.cache.hazelcast.predicate.QueryPredicate;
import com.bagri.xdm.cache.hazelcast.predicate.ResultsDocPredicate;
import com.bagri.xdm.cache.hazelcast.predicate.ResultsQueryPredicate;
import com.bagri.xdm.client.hazelcast.impl.FixedCursorImpl;
import com.bagri.xdm.client.hazelcast.impl.QueuedCursorImpl;
import com.bagri.xdm.common.DataKey;
import com.bagri.xdm.common.DocumentKey;
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
	
	private static final String xqScrollForwardStr = String.valueOf(XQConstants.SCROLLTYPE_FORWARD_ONLY);
	private static final String xqDefFetchSizeStr = "50";
	
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

	private boolean cacheResults = true;
	
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
    
    public void setCacheResults(boolean cacheResults) {
    	this.cacheResults = cacheResults;
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

	void addQueryResults(final String query, final Map<String, Object> params, final Properties props, 
			final ResultCursor cursor, final Iterator<Object> results) {

		final QueryExecContext ctx = thContext.get();

		final List<Object> resList;
		if (cursor != null) {
			try {
				resList = cursor.getList();
			} catch (XDMException ex) {
				logger.error("addQueryResults.error", ex);
				return;
			}
		} else {
			resList = new ArrayList<>();
		}
		
		//IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
		//execService.execute(new Runnable() {
		new Thread(new Runnable() {
				@Override
				public void run() {
					long qpKey = getResultsKey(query, params);
					if (cursor != null) {
						if (!cursor.isFixed()) {
							CollectionUtils.copyIterator(results, resList);
						}
					} else {
						CollectionUtils.copyIterator(results, resList);
					}

					if (resList.size() == 0 && ctx.getDocKeys().size() > 0) {
						logger.warn("addQueryResults; got empty results but docs were found: {}", ctx.getDocKeys());
					}
					QueryResult xqr = new QueryResult(params, ctx.getDocKeys(), resList);
					// what is better to use here: putAsync or set ?
					xrCache.set(qpKey, xqr);
					updateStats(query, 1, resList.size());
					
					//String clientId = props.getProperty(pn_client_id);
					//XQProcessor xqp = repo.getXQProcessor(clientId);
					//xqp.setResults(cursor);
					logger.trace("addQueryResults.exit; stored results: {} for key: {}", xqr, qpKey);
				}
		}).start();
	}
	
	List<Object> getQueryResults(String query, Map<String, Object> params, Properties props) {
		long qpKey = getResultsKey(query, params);
		logger.trace("getQueryResults; got result key: {}; parts: {}", qpKey, getResultsKeyParts(qpKey));
		QueryResult xqr = xrCache.get(qpKey);
		List<Object> result = null;
		if (xqr != null) {
			result = xqr.getResults();
			updateStats(query, 0, 1);
		} else {
			updateStats(query, 0, -1);
		}
		logger.trace("getQueryResults; returning: {}", xqr);
		return result;
	}

	Map<Long, String> getQueryUris(String query, Map<String, Object> params, Properties props) {
		long qpKey = getResultsKey(query, params);
		logger.trace("getQueryUris; got result key: {}; parts: {}", qpKey, getResultsKeyParts(qpKey));
		QueryResult xqr = xrCache.get(qpKey);
		Map<Long, String> result = null;
		if (xqr != null) {
			result = xqr.getDocKeys();
			updateStats(query, 0, 1);
		} else {
			updateStats(query, 0, -1);
		}
		logger.trace("getQueryUris; returning: {}", result);
		return result;
	}
	
	void invalidateQueryResults(Set<Integer> paths) {
		Set<Integer> qKeys = getQueriesForPaths(paths, false);
		if (!qKeys.isEmpty()) {
			removeQueryResults(qKeys);
		}
	}
	
	void removeQueryResults(long docId) {
		logger.trace("removeQueryResults.enter; got docId: {}; result cache size: {}", docId, xrCache.size());
		Predicate rdp = new ResultsDocPredicate(docId);
		Set<Long> rdKeys = xrCache.keySet(rdp);
		for (Long rdKey: rdKeys) {
			xrCache.delete(rdKey);
		}
		logger.trace("removeQueryResults.exit; deleted {} results for docId: {}", rdKeys.size(), docId);
	}

	void removeQueryResults(Collection<Integer> queryIds) {
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
	
	private Set<Long> queryKeys(Set<Long> found, ExpressionContainer ec, Expression ex) throws XDMException {
		if (ex == null) {
			logger.debug("queryKeys; got null expression in container: {}, skipping..", ec);
			return found;
		}
		
		if (ex instanceof AlwaysExpression) {
			return docMgr.getCollectionDocumentKeys(ex.getCollectionId());
		}
		
		if (ex instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) ex;
			Set<Long> leftKeys = queryKeys(found, ec, be.getLeft());
			if (Comparison.AND == be.getCompType()) {
				if (leftKeys != null && leftKeys.isEmpty()) {
					return leftKeys;
				}
				Set<Long> rightKeys = queryKeys(leftKeys, ec, be.getRight());
				return rightKeys;
			} else if (Comparison.OR == be.getCompType()) {
				Set<Long> rightKeys = queryKeys(found, ec, be.getRight());
				if (leftKeys != null) {
					if (rightKeys != null) {
						leftKeys.addAll(rightKeys);
					}
				} else {
					leftKeys = rightKeys;
				}
				return leftKeys;
			} else {
				throw new XDMException("Wrong BinaryExpression type: " + be.getCompType(), XDMException.ecQuery);
			}
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
		
		Set<Long> result = new HashSet<>();
		if (paths == null || paths.isEmpty()) {
			logger.info("queryPathKeys; got query on unknown path: {}", pex); 
			return result;
		}
		Object newVal = adjustSearchValue(value, dataType);
		if (newVal == null) {
			logger.info("queryPathKeys; got query on empty value sequence: {}", value); 
			return result;
		}
		logger.trace("queryPathKeys; adjusted value: {}({})", newVal.getClass().getName(), newVal); 
		
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

	private Map<Long, String> checkDocumentsCommited(Collection<Long> docKeys, int clnId) throws XDMException {
		Map<Long, String> result = new HashMap<>(docKeys.size());  
		Iterator<Long> itr = docKeys.iterator();
		while (itr.hasNext()) {
			long docKey = itr.next();
			String uri = docMgr.checkDocumentCommited(docKey, clnId); 
			if (uri != null) {
				result.put(docKey, uri);
			//} else {
			//	itr.remove();
			}
		}
		return result;
	}

	@Override
	public Collection<String> getContent(ExpressionContainer query, String template, Map<String, Object> params) throws XDMException {
		
		Collection<Long> docKeys = getDocumentIds(query);
		if (docKeys.size() > 0) {
			return docMgr.buildDocument(new HashSet<>(docKeys), template, params);
		}
		return Collections.emptyList();
	}
	
	@Override
	public Collection<Long> getDocumentIds(ExpressionContainer query) throws XDMException {
		if (query != null) {
			ExpressionBuilder exp = query.getBuilder();
			if (exp != null && exp.getRoot() != null) {
				// TODO: check stats for exp.getRoot().getCollectionId(), 
				// build 'found' set here if collectionId is selective enough
				Set<Long> ids = queryKeys(null, query, exp.getRoot());
				// otherwise filter out documents with wrong collectionIds here
				Map<Long, String> result = checkDocumentsCommited(ids, exp.getRoot().getCollectionId());
				thContext.get().setDocKeys(result);
				return result.keySet();
			}
		}
		logger.info("getDocumentIds; got rootless path: {}", query); 
		
		// fallback to full IDs set: default collection over all documents. not too good...
		// TODO: how could we distribute it over cache nodes? can we use local keySet only !?
		List<Long> result = new ArrayList<Long>(xddCache.keySet().size());
		for (DocumentKey docKey: xddCache.keySet()) {
			// we must provide only visible docIds!
			if (docMgr.checkDocumentCommited(docKey.getKey(), 0) != null) {
				result.add(docKey.getKey());
			}
		}
		// I don't want to cache all docIds here in result
		return result;
	}

	@Override
	public Collection<String> getDocumentUris(String query, Map<String, Object> params, Properties props) throws XDMException {
		logger.trace("getDocumentUris.enter; query: {}, params: {}; properties: {}", query, params, props);
		Collection<String> result = null;
		int qCode = 0;
		if (cacheResults) {
			qCode = getQueryKey(query);
			if (xqCache.containsKey(qCode)) {
				Map<Long, String> keys = getQueryUris(query, params, props);
				if (keys != null) {
					result = keys.values();
				}
			}
		}
		
		if (result == null) {
			try {
				Iterator<Object> iter = runQuery(query, params, props);
				result = thContext.get().getDocKeys().values();
				if (cacheResults) {
					if (xqCache.containsKey(qCode)) {
						addQueryResults(query, params, props, null, iter);
					} else {
						logger.warn("getDocumentUris; query is not cached after processing: {}", query);
					}
				}
			} catch (XQException ex) {
				throw new XDMException(ex, XDMException.ecQuery);
			}
		}

		logger.trace("getDocumentUris.exit; returning: {}", result);
		return result;
	}

	@Override
	public boolean isReadOnlyQuery(String query) {
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
	
	@Override
	public ResultCursor executeQuery(String query, Map<String, Object> params, Properties props) throws XDMException {

		logger.trace("executeQuery.enter; query: {}; params: {}; properties: {}", query, params, props);
		List<Object> resList = null;
		int qCode = 0;
		if (cacheResults) {
			boolean isQuery = "false".equalsIgnoreCase(props.getProperty(pn_query_command, "false"));
			qCode = getQueryKey(query);
			if (isQuery) {
				if (xqCache.containsKey(qCode)) {
					resList = getQueryResults(query, params, props);
				}
			}
		}
		
		ResultCursor cursor;
		if (resList == null) {
			try {
				Iterator<Object> iter = runQuery(query, params, props);
				cursor = createCursor(resList, iter, props);
				if (cacheResults) {
					if (xqCache.containsKey(qCode)) {
						// no need to check for isQuery, commands are not cached
						addQueryResults(query, params, props, cursor, iter);
					} else {
						logger.warn("executeQuery; query is not cached after processing: {}", query);
					}
				}
			} catch (XQException ex) {
				throw new XDMException(ex, XDMException.ecQuery);
			}
		} else {
			// already cached
			cursor = createCursor(resList, null, props);
		}
		String clientId = props.getProperty(pn_client_id);
		XQProcessor xqp = repo.getXQProcessor(clientId);
		xqp.setResults(cursor);
		logger.trace("executeQuery.exit; returning: {}", cursor);
		return cursor;
	}
	
	@Override
	public Collection<String> prepareQuery(String query) {
		// not used on the server side?
		logger.info("prepareQuery; query: {}", query);
		return null;
	}

	private Iterator<Object> runQuery(String query, Map<String, Object> params, Properties props) throws XQException {
		
        Throwable ex = null;
        boolean failed = false;
        stopWatch.start();
		
		Iterator<Object> iter = null;
		try {
			String clientId = props.getProperty(pn_client_id);
			boolean isQuery = "false".equalsIgnoreCase(props.getProperty(pn_query_command, "false"));
			XQProcessor xqp = repo.getXQProcessor(clientId);
			
			QueryExecContext ctx = thContext.get();
			ctx.clear();
				
			if (params != null) {
				for (Map.Entry<String, Object> var: params.entrySet()) {
					xqp.bindVariable(var.getKey(), var.getValue());
				}
			}
					
			if (isQuery) {
				iter = xqp.executeXQuery(query, props);
			} else {
				iter = xqp.executeXCommand(query, params, props);
			}
					
			if (params != null) {
				for (Map.Entry<String, Object> var: params.entrySet()) {
					xqp.unbindVariable(var.getKey());
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
	
	private ResultCursor createCursor(List<Object> results, Iterator<Object> iter, Properties props) {
		int count = 0;
		final ResultCursorBase xqCursor;

		int batchSize = 0;
		boolean fixed = true;
		int scrollType = Integer.parseInt(props.getProperty(pn_scrollability, xqScrollForwardStr));
		if (scrollType == XQConstants.SCROLLTYPE_SCROLLABLE) {
			if (results == null) {
				results = CollectionUtils.copyIterator(iter);
			}
		} else {
			String fetchSize = props.getProperty(pn_client_fetchSize);
			// not set -> use default BS
			if (fetchSize == null) {
				fetchSize = repo.getSchema().getProperty(xdm_schema_fetch_size);
				if (fetchSize == null) {
					fetchSize = xqDefFetchSizeStr;
				}
			}
			batchSize = Integer.parseInt(fetchSize);
			// fetch BS results.
			if (results == null) {
				results = CollectionUtils.copyIterator(iter, batchSize);
			}
			// if RS < BS -> put them to FixedCursor
			// else -> serialize them in QueuedCursor
			fixed = results.size() <= batchSize;
		}

		if (fixed) {
			xqCursor = new FixedCursorImpl(results);
			count = results.size();
		} else {
			int size = QueuedCursorImpl.UNKNOWN;
			if (iter != null && iter instanceof XQIterator) {
				size = ((XQIterator) iter).getFullSize();
			}
			String clientId = props.getProperty(pn_client_id);
			// we do not close cursors on the server side
			@SuppressWarnings("resource")
			QueuedCursorImpl qc = new QueuedCursorImpl(results, clientId, batchSize, size, iter);
			count = qc.serialize(repo.getHzInstance());
			xqCursor = qc;
		}
		logger.trace("createCursor.exit; serialized: {} results", count);
		return xqCursor;
	}
	
	private class QueryExecContext {
		
		private Map<Long, String> docKeys;
		
		void clear() {
			this.docKeys = null;
		}
		
		Map<Long, String> getDocKeys() {
			if (docKeys == null) {
				return Collections.emptyMap();
			}
			return docKeys;
		}
		
		void setDocKeys(Map<Long, String> docKeys) {
			this.docKeys = docKeys;
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

