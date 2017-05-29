package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_client_fetchSize;
import static com.bagri.core.Constants.pn_client_id;
import static com.bagri.core.Constants.pn_query_command;
import static com.bagri.core.Constants.pn_xqj_scrollability;
import static com.bagri.support.util.XQUtils.getAtomicValue;
import static com.bagri.support.util.XQUtils.isStringTypeCompatible;
import static com.bagri.core.Constants.pn_schema_fetch_size;

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

import com.bagri.client.hazelcast.impl.FixedCursorImpl;
import com.bagri.client.hazelcast.impl.QueuedCursorImpl;
import com.bagri.core.DataKey;
import com.bagri.core.DocumentKey;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.impl.QueryManagementBase;
import com.bagri.core.api.impl.ResultCursorBase;
import com.bagri.core.model.Document;
import com.bagri.core.model.Elements;
import com.bagri.core.model.Path;
import com.bagri.core.model.Query;
import com.bagri.core.model.QueryResult;
import com.bagri.core.query.AlwaysExpression;
import com.bagri.core.query.BinaryExpression;
import com.bagri.core.query.Comparison;
import com.bagri.core.query.Expression;
import com.bagri.core.query.ExpressionBuilder;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.query.PathExpression;
import com.bagri.core.query.QueriedPath;
import com.bagri.core.query.QueryBuilder;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.server.hazelcast.predicate.DocsAwarePredicate;
import com.bagri.server.hazelcast.predicate.QueryPredicate;
import com.bagri.server.hazelcast.predicate.ResultsDocPredicate;
import com.bagri.server.hazelcast.predicate.ResultsQueryPredicate;
import com.bagri.support.stats.StatisticsEvent;
import com.bagri.support.stats.watch.StopWatch;
import com.bagri.support.util.CollectionUtils;
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
		Integer qKey = getQueryKey(query);
		Query result = xqCache.get(qKey);
		// as I see we use BINARY format for this cache, no need for clone!
		//if (result != null) {
			// TODO: are you sure we have to perform clone here?
			// we got it by ref from replicated cache?
		//	result = result.clone();
		//}
		updateStats(query, result != null, 1);
		logger.trace("getQuery.exit; returning {}", result);
		return result;
	}

	@Override
	public boolean addQuery(String query, boolean readOnly, QueryBuilder xdmQuery) {
		return addQuery(new Query(query, readOnly, xdmQuery));
	}

	private boolean addQuery(Query query) {
		Integer qKey = getQueryKey(query.getQuery());
		//logger.trace("addQuery.enter; got code: {}; query cache size: {}", qCode, xQueries.size());
		//boolean result = xqCache.putIfAbsent(qCode, new XDMQuery(query, readOnly, xdmQuery)) == null;
		if (!xqCache.containsKey(qKey)) {
			// throws exception: Failed to serialize 'com.bagri.core.model.Query'
			try {
				xqCache.put(qKey, query);
			} catch (Exception ex) {
				logger.error("addQuery.error", ex);
			}
			return true;
		}
		return false;
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
			if (intersects(e.getValue(), pathIds, checkIndexed)) {
				result.add(e.getKey());
			}
		}
		logger.trace("getQueriesForPaths.exit; returning: {}", result);
		return result;
	}
	
	private boolean intersects(Query query, Collection<Integer> pathIds, boolean checkIndexed) {
		if (checkIndexed) {
			for (ExpressionContainer ec: query.getXdmQuery().getContainers()) {
				for (Expression ex: ec.getBuilder().getExpressions()) {
					if (ex.isCached()) {
						QueriedPath qp = ((PathExpression) ex).getCachedPath();
						if (qp.isIndexed()) {
							for (Integer pid: pathIds) {
								if (qp.getPathIds().contains(pid)) {
									return true;
								}
							}
						}
					}
				}
			}
		} else {
			for (ExpressionContainer ec: query.getXdmQuery().getContainers()) {
				for (Expression ex: ec.getBuilder().getExpressions()) {
					if (ex.isCached()) {
						QueriedPath qp = ((PathExpression) ex).getCachedPath();
						for (Integer pid: pathIds) {
							if (qp.getPathIds().contains(pid)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	void addQueryResults(final String query, final Map<String, Object> params, final Properties props, 
			final ResultCursor cursor, final Iterator<Object> results) {

		final QueryExecContext ctx = thContext.get();

		final List<Object> resList;
		if (cursor != null) {
			try {
				resList = cursor.getList();
			} catch (BagriException ex) {
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
	
	void invalidateQueryResults(Set<Integer> pathIds) {
		if (xqCache.size() > 0) {
			Set<Integer> qKeys = getQueriesForPaths(pathIds, false);
			if (!qKeys.isEmpty()) {
				removeQueryResults(qKeys);
			}
		}
	}
	
	void removeQueryResults(long docId) {
		logger.trace("removeQueryResults.enter; got docId: {}; result cache size: {}", docId, xrCache.size());
		int size = xrCache.size(); 
		xrCache.removeAll(new ResultsDocPredicate(docId));
		size = size - xrCache.size(); 
		logger.trace("removeQueryResults.exit; deleted {} results for docId: {}", size, docId);
	}

	void removeQueryResults(Collection<Integer> queryIds) {
		logger.trace("removeQueryResults.enter; got queryIds: {}; result cache size: {}", queryIds, xrCache.size());
		int size = xrCache.size(); 
		xrCache.removeAll(new ResultsQueryPredicate(queryIds));
		size = size - xrCache.size(); 
		logger.trace("removeQueryResults.exit; deleted {} results", size);
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
	
	private Set<Long> queryKeys(Set<Long> found, ExpressionContainer ec, Expression ex) throws BagriException {
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
				throw new BagriException("Wrong BinaryExpression type: " + be.getCompType(), BagriException.ecQuery);
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

	protected Set<Long> queryPathKeys(Set<Long> found, PathExpression pex, Object value) throws BagriException {

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
				paths = model.translatePathFromRegex(null, pex.getRegex());
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
				Path xPath = model.getPath(model.getPathRoot(path), path);
				if (xPath == null) {
					xPath = model.getPath("/", path);
				}
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

	private Map<Long, String> checkDocumentsCommited(Collection<Long> docKeys, int clnId) throws BagriException {
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
	public Collection<String> getContent(ExpressionContainer query, String template, Map<String, Object> params) throws BagriException {
		
		Collection<Long> docKeys = getDocumentIds(query);
		if (docKeys.size() > 0) {
			return docMgr.buildDocument(new HashSet<>(docKeys), template, params);
		}
		return Collections.emptyList();
	}
	
	@Override
	public Collection<Long> getDocumentIds(ExpressionContainer query) throws BagriException {
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
	public Collection<String> getDocumentUris(String query, Map<String, Object> params, Properties props) throws BagriException {
		logger.trace("getDocumentUris.enter; query: {}, params: {}; properties: {}", query, params, props);
		Collection<String> result = null;
		int qKey = 0;
		if (cacheResults) {
			qKey = getQueryKey(query);
			if (xqCache.containsKey(qKey)) {
				Map<Long, String> keys = getQueryUris(query, params, props);
				if (keys != null) {
					result = keys.values();
				}
			}
		}
		
		if (result == null) {
			String clientId = props.getProperty(pn_client_id);
			XQProcessor xqp = repo.getXQProcessor(clientId);
			try {
				Iterator<Object> iter = runQuery(query, params, props);
				result = thContext.get().getDocKeys().values();
				if (cacheResults) {
					Query xQuery = xqp.getCurrentQuery(query);
					if (xQuery != null) {
						addQuery(xQuery);
						addQueryResults(query, params, props, null, iter);
					} else {
						logger.warn("getDocumentUris; query is not cached after processing: {}", query);
					}
				}
			} catch (XQException ex) {
				throw new BagriException(ex, BagriException.ecQuery);
			}
		}

		logger.trace("getDocumentUris.exit; returning: {}", result);
		return result;
	}

	@Override
	public boolean isQueryReadOnly(String query, Properties props) throws BagriException {
		Integer qKey = getQueryKey(query);
		Query xQuery = xqCache.get(qKey);
		if (xQuery == null) {
			XQProcessor xqp = repo.getXQProcessor();
			try {
				return xqp.isQueryReadOnly(query, props);
			} catch (XQException ex) {
				throw new BagriException(ex, BagriException.ecQuery);
			}
		}
		return xQuery.isReadOnly();
	}

	@Override
	public void cancelExecution() throws BagriException {
		// no-op on the server side
	}
	
	@Override
	public ResultCursor executeQuery(String query, Map<String, Object> params, Properties props) throws BagriException {

		logger.trace("executeQuery.enter; query: {}; params: {}; properties: {}", query, params, props);
		List<Object> resList = null;
		int qKey = 0;
		if (cacheResults) {
			boolean isQuery = "false".equalsIgnoreCase(props.getProperty(pn_query_command, "false"));
			qKey = getQueryKey(query);
			if (isQuery) {
				if (xqCache.containsKey(qKey)) {
					resList = getQueryResults(query, params, props);
				}
			}
		}
		
		ResultCursor cursor;
		String clientId = props.getProperty(pn_client_id);
		XQProcessor xqp = repo.getXQProcessor(clientId);
		if (resList == null) {
			try {
				Iterator<Object> iter = runQuery(query, params, props);
				cursor = createCursor(resList, iter, props);
				if (cacheResults) {
					Query xQuery = xqp.getCurrentQuery(query);
					if (xQuery != null) {
						addQuery(xQuery);
						addQueryResults(query, params, props, cursor, iter);
					} else {
						// TODO: fix it!
						logger.debug("executeQuery; query is not cached after processing: {}", query);
					}
				}
			} catch (XQException ex) {
				throw new BagriException(ex.getMessage(), ex, BagriException.ecQuery);
			}
		} else {
			// already cached
			cursor = createCursor(resList, null, props);
		}
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
        	if (logger.isDebugEnabled()) {
        		logger.error("runQuery.error: ", ex);
        	}
            throw new XQException(ex.getMessage());
        }
			
		return iter;
	}
	
	private ResultCursor createCursor(List<Object> results, Iterator<Object> iter, Properties props) {
		int count = 0;
		final ResultCursorBase xqCursor;

		int batchSize = 0;
		boolean fixed = true;
		int scrollType = Integer.parseInt(props.getProperty(pn_xqj_scrollability, xqScrollForwardStr));
		if (scrollType == XQConstants.SCROLLTYPE_SCROLLABLE) {
			if (results == null) {
				results = CollectionUtils.copyIterator(iter);
			}
		} else {
			String fetchSize = props.getProperty(pn_client_fetchSize);
			// not set -> use default BS
			if (fetchSize == null) {
				fetchSize = repo.getSchema().getProperty(pn_schema_fetch_size);
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

