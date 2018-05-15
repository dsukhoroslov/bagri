package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.system.DataFormat.df_xml;
import static com.bagri.support.util.XQUtils.getAtomicValue;
import static com.bagri.support.util.XQUtils.isStringTypeCompatible;

import java.io.IOException;
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
import java.util.concurrent.ExecutorService;

import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.impl.BoundedCursorImpl;
import com.bagri.client.hazelcast.impl.CompressingCursorImpl;
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
import com.bagri.core.query.PathBuilder;
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
import com.bagri.support.util.PropUtils;
import com.hazelcast.core.IMap;
import com.hazelcast.core.PartitionService;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class QueryManagementImpl extends QueryManagementBase implements QueryManagement {
	
	private static final transient Logger logger = LoggerFactory.getLogger(QueryManagementImpl.class);
	
	private static final String xqScrollForwardStr = String.valueOf(XQConstants.SCROLLTYPE_FORWARD_ONLY);
	
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

	private ExecutorService execPool;

	
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
    
    public void setExecPool(ExecutorService execSvc) {
    	this.execPool = execSvc;
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
		if (!xqCache.isEmpty()) {
			Set<Integer> qKeys = getQueriesForPaths(pathIds, false);
			if (!qKeys.isEmpty()) {
				removeQueryResults(qKeys);
			}
		}
	}
	
	void removeQueryResults(long docId) {
		logger.trace("removeQueryResults.enter; got docId: {}; result cache size: {}", docId, xrCache.size());
		//int size = logger.isTraceEnabled() ? xrCache.size() : 0;
		int size = xrCache.size();
		xrCache.removeAll(new ResultsDocPredicate(docId));
		//xrCache.removeAll(Predicates.equal("docId", docId));
		//size -= logger.isTraceEnabled() ? xrCache.size() : 0;
		size -= xrCache.size();
		logger.info("removeQueryResults.exit; deleted {} results for docId: {}", size, docId);
	}

	void removeQueryResults(Collection<Integer> queryIds) {
		logger.trace("removeQueryResults.enter; got queryIds: {}; result cache size: {}", queryIds, xrCache.size());
		int size = logger.isTraceEnabled() ? xrCache.size() : 0; 
		xrCache.removeAll(new ResultsQueryPredicate(queryIds));
		size -= logger.isTraceEnabled() ? xrCache.size() : 0; 
		logger.trace("removeQueryResults.exit; deleted {} results", size);
	}
	
	@Override
	public void clearCache() {
		xqCache.clear(); //evictAll();
		xrCache.evictAll();
		repo.clearXQProcessors();
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
	
	private Set<Long> queryKeys(boolean local, Set<Long> found, ExpressionContainer ec, Expression ex) throws BagriException {
		if (ex == null) {
			logger.debug("queryKeys; got null expression in container: {}, skipping..", ec);
			return found;
		}
		
		if (ex instanceof AlwaysExpression) {
			return docMgr.getCollectionDocumentKeys(ex.getCollectionId());
		}
		
		if (ex instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) ex;
			Set<Long> leftKeys = queryKeys(local, found, ec, be.getLeft());
			if (Comparison.AND == be.getCompType()) {
				if (leftKeys != null && leftKeys.isEmpty()) {
					return leftKeys;
				}
				Set<Long> rightKeys = queryKeys(local, leftKeys, ec, be.getRight());
				return rightKeys;
			} else if (Comparison.OR == be.getCompType()) {
				Set<Long> rightKeys = queryKeys(local, found, ec, be.getRight());
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
		return queryPathKeys(local, found, pex, ec.getParam(pex));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object adjustSearchValue(Object value, int pathType) {
		if (value == null) {
			return null;
		}
		logger.trace("adjustSearchValue.enter; adjusting {}:{}; expected type is {}", value.getClass().getName(), value, pathType);
		int valType = XQItemType.XQBASETYPE_ANYTYPE;
		while (value instanceof Collection) {
			Collection values = (Collection) value;
			if (values.size() == 0) {
				return null;
			}
			if (values.size() == 1) {
				value = values.iterator().next();
				if (value == null) {
					return null;
				}
			} else {
				// CompType must be IN !
				List newVals = new ArrayList(values.size());
				for (Object val: values) {
					newVals.add(adjustSearchValue(val, pathType));
				}
				return newVals;
			}
		} 
		logger.trace("adjustSearchValue; after reduction value is {}:{}", value.getClass().getName(), value);
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
				String strVal = value.toString();
				if (strVal.startsWith("[") && strVal.endsWith("]")) {
					String[] values = strVal.substring(1, strVal.length() - 1).split(", ");
					List newVals = new ArrayList(values.length);
					for (String val: values) {
						newVals.add(adjustSearchValue(val, pathType));
					}
					return newVals;
				} else {
					value = getAtomicValue(pathType, value.toString());
				}
			}
		}
		return value;
	}

	private Set<Long> queryPathKeys(boolean local, Set<Long> found, PathExpression pex, Object value) throws BagriException {

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
			logger.debug("queryPathKeys; got query on unknown path: {}", pex);
			if (found != null) {
				result.addAll(found);
			}
			return result;
		}
		Object newVal = adjustSearchValue(value, dataType);
		if (newVal == null) {
			logger.debug("queryPathKeys; got query on empty value, path: {}", pex);
			if (found != null) {
				result.addAll(found);
			}
			return result;
		}
		logger.trace("queryPathKeys; adjusted value: {}({})", newVal.getClass().getName(), newVal); 
		
		if (indexed) {
			for (Integer pathId: paths) {
				Set<Long> docKeys = idxMgr.getIndexedDocuments(pathId, pex, newVal);
				logger.trace("queryPathKeys; search for index - got keys: {}", docKeys == null ? null : docKeys.size()); 
				if (docKeys != null) {
					if (local && !docKeys.isEmpty()) {
						docKeys = checkDocumentsLocal(docKeys);
					}
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
	   	Set<DataKey> xdmKeys = local ? xdmCache.localKeySet(f) : xdmCache.keySet(f);
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
			}
		}
		return result;
	}
	
	private Set<Long> checkDocumentsLocal(Collection<Long> docKeys) {
		// filter out external docKeys; size should be docKeys.size / cluster size
		Set<Long> localKeys = new HashSet<>();
		PartitionService ps = repo.getHzInstance().getPartitionService();
		for (Long key: docKeys) {
			if (ps.getPartition(key).getOwner().localMember()) {
				localKeys.add(key);
			}
		}
		return localKeys;
	}

	@Override
	public void cancelExecution() throws BagriException {
		// no-op on the server side
	}
	
	@Override
	public <T> ResultCursor<T> executeQuery(String query, Map<String, Object> params, Properties props) throws BagriException {

		logger.trace("executeQuery.enter; query: {}; params: {}; properties: {}", query, params, props);
		List<T> resList = null;
		int qKey = 0;
		boolean isQuery = "false".equalsIgnoreCase(props.getProperty(pn_query_command, "false"));
		if (cacheResults) {
			qKey = getQueryKey(query);
			if (isQuery) {
				if (xqCache.containsKey(qKey)) {
					resList = (List<T>) getQueryResults(query, params, props);
				}
			}
		}
		
		ResultCursor<T> cursor;
		String clientId = props.getProperty(pn_client_id);
		XQProcessor xqp = repo.getXQProcessor(clientId);
		if (resList == null) {
			try {
				Iterator<T> iter = runQuery(xqp, query, params, props, isQuery);
				if (cacheResults) {
					Query xQuery = xqp.getCurrentQuery(query);
					if (xQuery != null) {
						addQuery(xQuery);
						cursor = createCachedCursor(query, params, props, iter, false);
					} else {
						// thus, it is updating query
						logger.debug("executeQuery; query is not cached after processing\n: {}", query);
						cursor = createCursor(iter, props);
					}
				} else {
					cursor = createCursor(iter, props);
				}
			} catch (XQException ex) {
				String em = ex.getMessage();
				if (em == null) {
					em = ex.getClass().getName();
				}
				throw new BagriException(em, ex, BagriException.ecQuery);
			}
		} else {
			// already cached
			cursor = createCursor(resList.iterator(), props);
		}
		//logger.info("executeQuery.exit; params: {}; props: {}; cursor: {}", params, props, cursor.getList().size());
		xqp.setResults(cursor);
		logger.trace("executeQuery.exit; returning: {}", cursor);
		return cursor;
	}
	
	@Override
	public Collection<String> getContent(ExpressionContainer query, String template, Map<String, Object> params) throws BagriException {
		
		Collection<Long> docKeys = getDocumentIds(query);
		if (docKeys.size() > 0) {
			return docMgr.buildContent(new HashSet<>(docKeys), template, params, df_xml);
		}
		return Collections.emptyList();
	}
	
	@Override
	public Collection<Long> getDocumentIds(ExpressionContainer query) throws BagriException {
		QueryExecContext ctx = thContext.get();
		Properties props = ctx.getProperties();
		Map<String, Object> params = ctx.getParameters();
		
		String runOn = props.getProperty(pn_client_submitTo, pv_client_submitTo_any);
		boolean localOnly = pv_client_submitTo_all.equalsIgnoreCase(runOn);
		String overrides = props.getProperty(pn_query_customPaths);
		logger.debug("getDocumentIds; got override paths: {}", overrides);
		
		if (query != null) {
			ExpressionBuilder exp = query.getBuilder();
			if (exp != null && exp.getRoot() != null) {
				overridePaths(query, params, overrides);
					
				// TODO: check stats for exp.getRoot().getCollectionId(), 
				// build 'found' set here if collectionId is selective enough
				Set<Long> ids = queryKeys(localOnly, null, query, exp.getRoot());
				// otherwise filter out documents with wrong collectionIds here
				Map<Long, String> result = checkDocumentsCommited(ids, exp.getRoot().getCollectionId());
				ctx.setDocKeys(result);
				return result.keySet();
			}
		}

		logger.info("getDocumentIds; got rootless path: {}", query); 
		// fallback to full IDs set: default collection over all documents. not too good...
		List<Long> result;
		if (localOnly) {
			result = new ArrayList<Long>(xddCache.localKeySet().size());
			for (DocumentKey docKey: xddCache.localKeySet()) {
				if (docMgr.checkDocumentCommited(docKey.getKey(), 0) != null) {
					result.add(docKey.getKey());
				}
			}
		} else {
			result = new ArrayList<Long>(xddCache.keySet().size());
			for (DocumentKey docKey: xddCache.keySet()) {
				// we must provide only visible docIds!
				if (docMgr.checkDocumentCommited(docKey.getKey(), 0) != null) {
					result.add(docKey.getKey());
				}
			}
		}
		// We don't want to cache all docIds here in result? 
		return result;
	}
	
	private void overridePaths(ExpressionContainer query, Map<String, Object> params, String overrides) {
		if (overrides != null) {
			try {
				Properties ops = PropUtils.propsFromString(overrides);
				// override paths in expression container with ops..
				int idx = 0;
				for (Expression ex: query.getBuilder().getExpressions()) {
					String op = ops.getProperty(String.valueOf(idx));
					if (op != null) {
						String[] parts = op.split(" "); 
						ex.setPath(new PathBuilder(parts[0]));
						if (parts.length > 1) {
							ex.setCompType(Comparison.valueOf(parts[1]));
							if (parts.length > 2) {
								((PathExpression) ex).setParamName(parts[2]);
								if (params.containsKey(parts[2])) {
									query.getParams().put(parts[2], params.get(parts[2]));
								}
							}
						}
					}
					idx++;
				}
			} catch (IOException ex) {
				logger.warn("overrideQuery.error; can't read paths overrides, skipping");
			}
			logger.debug("overrideQuery; overriden query is: {}", query);
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultCursor<String> getDocumentUris(String query, Map<String, Object> params, Properties props) throws BagriException {
		logger.trace("getDocumentUris.enter; query: {}, params: {}; properties: {}", query, params, props);
		Collection resList = null;
		int qKey = 0;
		if (cacheResults) {
			qKey = getQueryKey(query);
			if (xqCache.containsKey(qKey)) {
				Map<Long, String> keys = getQueryUris(query, params, props);
				if (keys != null) {
					resList = keys.values();
				}
			}
		}

		ResultCursor cursor = null;
		if (resList == null) {
			String clientId = props.getProperty(pn_client_id);
			XQProcessor xqp = repo.getXQProcessor(clientId);
			try {
				Iterator<Object> iter = runQuery(xqp, query, params, props, true);
				if (cacheResults) {
					Query xQuery = xqp.getCurrentQuery(query);
					if (xQuery != null) {
						addQuery(xQuery);
						cursor = createCachedCursor(query, params, props, iter, true);
					} else {
						// TODO: fix it!
						logger.info("executeQuery; query is not cached after processing: {}", query);
						cursor = createCursor(thContext.get().getDocKeys().values().iterator(), props);
					}
				} else {
					cursor = createCursor(thContext.get().getDocKeys().values().iterator(), props);
				}
			} catch (XQException ex) {
				throw new BagriException(ex, BagriException.ecQuery);
			}
		} else {
			// already cached
			cursor = createCursor(resList.iterator(), props);
		}
		
		logger.trace("getDocumentUris.exit; returning: {}", cursor);
		return cursor;
	}

	@Override
	public boolean isQueryReadOnly(String query, Properties props) throws BagriException {
		Integer qKey = getQueryKey(query);
		Query xQuery = xqCache.get(qKey);
		if (xQuery == null) {
			XQProcessor xqp;
			String clientId = props.getProperty(pn_client_id);
			if (clientId == null) {
				logger.warn("isQueryReadOnly; got no clientId");
				xqp = repo.getXQProcessor();
			} else {
				xqp = repo.getXQProcessor(clientId);
			}
			try {
				return xqp.isQueryReadOnly(query, props);
			} catch (XQException ex) {
				throw new BagriException(ex, BagriException.ecQuery);
			}
		}
		return xQuery.isReadOnly();
	}

	@Override
	public Collection<String> prepareQuery(String query) {
		// not used on the server side?
		logger.info("prepareQuery; query: {}", query);
		return null;
	}

	private <T> Iterator<T> runQuery(XQProcessor xqp, String query, Map<String, Object> params, Properties props, boolean isQuery) throws XQException {
		
        Throwable ex = null;
        boolean failed = false;
        stopWatch.start();

		Iterator<T> iter = null;
		try {
			QueryExecContext ctx = thContext.get();
			ctx.reset(props, params);
				
			if (params != null) {
				for (Map.Entry<String, Object> var: params.entrySet()) {
					xqp.bindVariable(var.getKey(), var.getValue());
				}
			}
			
			if (isQuery) {
				iter = (Iterator<T>) xqp.executeXQuery(query, props);
			} else {
				iter = (Iterator<T>) xqp.executeXCommand(query, params, props);
			}

			if (params != null) {
				for (Map.Entry<String, Object> var: params.entrySet()) {
					xqp.unbindVariable(var.getKey());
				}
			}
		} catch (Throwable t) {
            failed = true;
            ex = t;
        }
        long stamp = stopWatch.stop();
        if (!timeQueue.offer(new StatisticsEvent(query, !failed, new Object[] {stamp}))) {
        	logger.warn("runQuery: the timeQueue is full!!");
        }
        if (failed) {
       		logger.error("runQuery.error: ", ex);
            throw new XQException(ex.getMessage());
        }
			
		return iter;
	}
	
	private <T> ResultCursor<T> createCursor(final Iterator<T> iter, final Properties props) {

		final ResultCursorBase<T> cursor = getResultCursor(props);
		if (cursor.isAsynch()) {
			execPool.execute(new Runnable() {
				@Override
				public void run() {
					fetchResults(iter, props, cursor);
					cursor.finish();
				}
			});
		} else {
			fetchResults(iter, props, cursor);
		}
		
		return cursor;
	}

	private <T> ResultCursor<T> createCachedCursor(final String query, final Map<String, Object> params, final Properties props, final Iterator<T> iter, final boolean returnUris) {

		final QueryExecContext ctx = thContext.get();
		final ResultCursorBase<T> cursor = getResultCursor(props);
		if (cursor.isAsynch()) {
			execPool.execute(new Runnable() {
				@Override
				public void run() {
					fetchAndCacheResults(ctx, query, params, iter, props, cursor, returnUris);
					cursor.finish();
				}
			});
		} else {
			fetchAndCacheResults(ctx, query, params, iter, props, cursor, returnUris);
		}
		
		return cursor;
	}
	
	private <T> void fetchResults(Iterator<T> results, Properties props, ResultCursorBase<T> cursor) {
		int fetchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
		if (fetchSize > 0) {
			int cnt = 0;
			while (results.hasNext() && cnt < fetchSize) {
				cursor.add(results.next());
				cnt++;
			}
		} else {
			while (results.hasNext()) {
				cursor.add(results.next());
			}
		}
	}

	private <T> void fetchAndCacheResults(QueryExecContext ctx, String query, Map<String, Object> params, Iterator<T> results, Properties props, 
			ResultCursorBase<T> cursor, boolean returnUris) {
		int fetchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
		List<Object> resList;
		if (returnUris) {
			Iterator<String> uris = ctx.getDocKeys().values().iterator();
			if (fetchSize > 0) {
				resList = collectResults(results, uris, cursor, fetchSize);
			} else {
				resList = collectResults(results, uris, cursor);
			}
		} else {
			if (fetchSize > 0) {
				resList = collectResults(results, cursor, fetchSize);
			} else {
				resList = collectResults(results, cursor);
			}
		}
		
		if (resList.size() > 0 && ctx.getDocKeys().size() == 0) {
			logger.warn("fetchAndCacheResults.exit; got inconsistent query results; params: {}, docKeys: {}, results: {}", 
					params, ctx.getDocKeys(), resList);
		} else {
			long qpKey = getResultsKey(query, params);
			QueryResult xqr = new QueryResult(params, ctx.getDocKeys(), resList);
			// what is better to use here: putAsync or set ?
			xrCache.set(qpKey, xqr);
			updateStats(query, 1, resList.size());
			logger.trace("fetchAndCacheResults.exit; stored results: {} for key: {}", xqr, qpKey);
		}
	}
	
	private <T> List<Object> collectResults(Iterator<T> results, ResultCursorBase<T> cursor) {
		List<Object> resList = new ArrayList<>();
		while (results.hasNext()) {
			T o = results.next();
			resList.add(o);
			cursor.add(o);
		}
		return resList;
	}

	private <T> List<Object> collectResults(Iterator<T> results, ResultCursorBase<T> cursor, int fetchSize) {
		List<Object> resList = new ArrayList<>(fetchSize);
		int cnt = 0;
		while (results.hasNext() && cnt < fetchSize) {
			T o = results.next();
			resList.add(o);
			cursor.add(o);
			cnt++;
		}
		return resList;
	}

	// not sure uries are in synch with results.. can bet NoSuchElementException here and below..
	private <T> List<Object> collectResults(Iterator<T> results, Iterator<String> uris, ResultCursorBase<T> cursor) {
		List<Object> resList = new ArrayList<>();
		while (results.hasNext()) {
			T o = results.next();
			resList.add(o);
			cursor.add((T) uris.next());
		}
		return resList;
	}

	private <T> List<Object> collectResults(Iterator<T> results, Iterator<String> uris, ResultCursorBase<T> cursor, int fetchSize) {
		List<Object> resList = new ArrayList<>(fetchSize);
		int cnt = 0;
		while (results.hasNext() && cnt < fetchSize) {
			T o = results.next();
			resList.add(o);
			cursor.add((T) uris.next());
			cnt++;
		}
		return resList;
	}

	private <T> ResultCursorBase<T> getResultCursor(Properties props) {
		ResultCursorBase<T> cursor;

		int fetchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
		int scrollType = Integer.parseInt(props.getProperty(pn_xqj_scrollability, xqScrollForwardStr));
		if (scrollType == XQConstants.SCROLLTYPE_SCROLLABLE) {
			cursor = new FixedCursorImpl<>(fetchSize);
		} else {
			if (Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"))) {
				String clientId = props.getProperty(pn_client_id);
				String queueName = "client:" + clientId;
				if (fetchSize > 0) {
					cursor = new BoundedCursorImpl<>(repo.getHzInstance(), queueName, fetchSize);
				} else {
					cursor = new QueuedCursorImpl<>(repo.getHzInstance(), queueName);
				}
			} else {
				if (Boolean.parseBoolean(props.getProperty(pn_document_compress, "false"))) {
					cursor = new CompressingCursorImpl<>(repo, fetchSize);
				} else {
					cursor = new FixedCursorImpl<>(fetchSize);
				}
			}
		}
		logger.trace("getResultCursor.exit; created {} for props {}", cursor, props);
		return cursor;
	}

	
	private class QueryExecContext {
		
		private Properties props;
		private Map<Long, String> docKeys;
		private Map<String, Object> params;
		
		void clear() {
			this.docKeys = null;
			this.props = null;
			this.params = null;
		}
		
		Map<Long, String> getDocKeys() {
			if (docKeys == null) {
				return Collections.emptyMap();
			}
			return docKeys;
		}
		
		Properties getProperties() {
			if (props == null) {
				return new Properties();
			}
			return props;
		}
		
		Map<String, Object> getParameters() {
			if (params == null) {
				return Collections.emptyMap();
			}
			return params;
		}
		
		void setDocKeys(Map<Long, String> docKeys) {
			this.docKeys = docKeys;
		}
		
		void setProperties(Properties props) {
			this.props = props;
		}
		
		void setParameters(Map<String, Object> params) {
			this.params = params;
		}
		
		void reset(Properties props, Map<String, Object> params) {
			this.docKeys = null;
			this.props = props;
			this.params = params;
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

