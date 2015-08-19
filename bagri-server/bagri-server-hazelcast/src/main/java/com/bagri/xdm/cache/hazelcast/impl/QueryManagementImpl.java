package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;
import static com.bagri.xdm.common.XDMConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.BinaryExpression;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.Expression;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathExpression;
import com.bagri.common.query.QueryBuilder;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.bagri.xdm.cache.hazelcast.predicate.DocsAwarePredicate;
import com.bagri.xdm.cache.hazelcast.predicate.QueryPredicate;
import com.bagri.xdm.client.hazelcast.data.QueryParamsKey;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMResultsKey;
import com.bagri.xdm.domain.XDMCardinality;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.domain.XDMQuery;
import com.bagri.xdm.domain.XDMResults;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.ExceptionIterator;
import com.bagri.xquery.saxon.XQSequenceIterator;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class QueryManagementImpl implements XDMQueryManagement {
	
	private static final transient Logger logger = LoggerFactory.getLogger(QueryManagementImpl.class);
	
	private RepositoryImpl repo;
	private XDMModelManagement model;
    private IndexManagementImpl idxMgr;
	private DocumentManagementImpl docMgr;
	private TransactionManagementImpl txMgr;
	
    private IMap<Integer, XDMQuery> xqCache;
    private IMap<XDMResultsKey, XDMResults> xrCache;
    private Map<Integer, XDMQuery> xQueries = new ConcurrentHashMap<Integer, XDMQuery>();
    
	private IMap<XDMDocumentKey, XDMDocument> xddCache;
    private IMap<XDMDataKey, XDMElements> xdmCache;
    
    public QueryManagementImpl() {
    	logger.info("<init>; query cache initialized");
    }
    
    public void setRepository(RepositoryImpl repo) {
    	this.repo = repo;
    	this.model = repo.getModelManagement();
    	this.docMgr = (DocumentManagementImpl) repo.getDocumentManagement();
    	this.txMgr = (TransactionManagementImpl) repo.getTxManagement();
    	this.xddCache = docMgr.getDocumentCache();
    	this.xdmCache = docMgr.getElementCache();
    	docMgr.setRepository(repo);
    }

    public void setQueryCache(IMap<Integer, XDMQuery> cache) {
    	this.xqCache = cache;
    }
    
    public void setResultCache(IMap<XDMResultsKey, XDMResults> cache) {
    	this.xrCache = cache;
    }
    
    public void setIndexManager(IndexManagementImpl indexManager) {
    	this.idxMgr = indexManager;
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
	public boolean addQuery(String query, boolean readOnly, Object xqExpression, QueryBuilder xdmQuery) {
		Integer qCode = getQueryKey(query);
		logger.trace("addQuery.enter; got code: {}; query cache size: {}", qCode, xQueries.size());
		boolean result = false;
		//if (xqCache.tryLock(qCode)) {
		//	try {
		result = xQueries.put(qCode, new XDMQuery(query, readOnly, xqExpression, xdmQuery)) == null;
		//		xqObjects.put(qCode, xqExpression);
		//	} finally {
		//		xqCache.unlock(qCode);
		//	}
		//}
		logger.trace("addQuery.exit; returning: {}", result);
		return result;
	}

	@Override
	public void addExpression(String query, boolean readOnly, Object xqExpression) {
		Integer qCode = getQueryKey(query);
		logger.trace("addExpression.enter; got code: {}; query cache size: {}", qCode, xQueries.size());
		QueryBuilder xdmQuery = null;
		XDMQuery xQuery = xQueries.get(qCode);
		if (xQuery != null) {
			xdmQuery = xQuery.getXdmQuery(); 
		}
		xQuery = new XDMQuery(query, readOnly, xqExpression, xdmQuery);
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
	public void addExpression(String query, boolean readOnly, QueryBuilder xdmQuery) {
		Integer qCode = getQueryKey(query);
		logger.trace("addExpression.enter; got code: {}; query cache size: {}", qCode, xQueries.size());
		Object xqExpression = null;
		XDMQuery xQuery = xQueries.get(qCode);
		if (xQuery != null) {
			xqExpression = xQuery.getXqExpression(); 
		}
		xQuery = new XDMQuery(query, readOnly, xqExpression, xdmQuery);
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

	@Override
	public void clearCache() {
		xqCache.evictAll();
		xrCache.evictAll();
		xQueries.clear();
	}
	
	public Set<Long> queryKeys(Set<Long> found, ExpressionContainer ec, Expression ex) throws XDMException {
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

	protected Set<Long> queryPathKeys(Set<Long> found, PathExpression pex, Object value) throws XDMException {

		logger.trace("queryPathKeys.enter; found: {}; value: {}", (found == null ? "null" : found.size()), value); 
		Predicate pp = null;
		Set<Integer> paths;
		if (pex.isRegex()) {
			// TODO: do not create new path here!
			paths = model.translatePathFromRegex(pex.getDocType(), pex.getRegex());
			logger.trace("queryPathKeys; regex: {}; pathIds: {}", pex.getRegex(), paths);
			if (paths.size() > 0) {
				pp = Predicates.in("pathId", paths.toArray(new Integer[paths.size()]));
			}
		} else {
			String path = pex.getFullPath();
			logger.trace("queryPathKeys; path: {}; comparison: {}", path, pex.getCompType());
			XDMPath xPath = model.getPath(path);
			paths = new HashSet<>(1);
			if (xPath != null) {
				pp = Predicates.equal("pathId", xPath.getPathId());
				paths.add(xPath.getPathId());
			}
		}
		
		if (paths.size() == 0) {
			logger.info("queryPathKeys; got query on unknown path: {}", pex); 
			return Collections.emptySet();
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

		Set<Long> result = new HashSet<>();
		for (Integer pathId: paths) {
			if (idxMgr.isIndexEnabled(pathId)) {
				Set<Long> docIds = idxMgr.getIndexedDocuments(pathId, pex, value);
				logger.trace("queryPathKeys; search for index - got ids: {}", docIds == null ? null : docIds.size()); 
				if (docIds != null) {
					if (found == null) {
						//result.addAll(checkDocumentsCommited(docIds));
						result.addAll(docIds);
					} else {
						found.retainAll(docIds);
						result = found;
					}
				} else {
					//fallback to full scan below..
					result = null;
					break;
				}
			} else {
				result = null;
				break;
			}
		}
		if (result != null) {
			logger.trace("queryPathKeys.exit; returning {} indexed keys", result.size()); 
			return result;
		}

		if (value instanceof Collection) {
			Collection values = (Collection) value;
			if (values.size() == 0) {
				return Collections.emptySet();
			}
			if (values.size() == 1) {
				value = values.iterator().next();
			}
		} 
		if (value instanceof XQItem) {
			try {
				value = ((XQItem) value).getObject();
			} catch (XQException ex) {
				logger.error("getIndexedDocuments.error", ex);
				value = value.toString();
			}
		}

		if (found == null) {
			QueryPredicate qp = new QueryPredicate(pex, value);
			Predicate<XDMDataKey, XDMElements> f = Predicates.and(pp, qp);
	   		Set<XDMDataKey> xdmKeys = xdmCache.keySet(f);
			logger.trace("queryPathKeys; got {} query results", xdmKeys.size()); 
			result = new HashSet<Long>(xdmKeys.size());
			for (XDMDataKey key: xdmKeys) {
				long docId = key.getDocumentId();
				//if (checkDocumentCommited(docId)) {
					result.add(docId);
				//}
			}
		} else {
			QueryPredicate qp = new DocsAwarePredicate(pex, value, found);
			Predicate<XDMDataKey, XDMElements> f = Predicates.and(pp, qp);
	   		Set<XDMDataKey> xdmKeys = xdmCache.keySet(f);
			logger.trace("queryPathKeys; got {} docs aware query results", xdmKeys.size()); 
			result = new HashSet<Long>(xdmKeys.size());
			for (XDMDataKey key: xdmKeys) {
				long docId = key.getDocumentId();
				result.add(docId);
			}
		}
		logger.trace("queryPathKeys.exit; returning {} keys", result.size()); 
		return result;
	}

	private boolean checkDocumentCommited(long docId) {
		XDMDocument doc = docMgr.getDocument(docId);
		if (doc.getTxFinish() > TX_NO && txMgr.isTxVisible(doc.getTxFinish())) {
			return false;
		}
		return txMgr.isTxVisible(doc.getTxStart());
	}
	
	private Collection<Long> checkDocumentsCommited(Collection<Long> docIds) {
		Iterator<Long> itr = docIds.iterator();
		while (itr.hasNext()) {
			long docId = itr.next();
			if (!checkDocumentCommited(docId)) {
				itr.remove();
			}
		}
		return docIds;
	}
	
	@Override
	public Collection<Long> getDocumentIDs(ExpressionContainer query) throws XDMException {
		ExpressionBuilder exp = query.getExpression();
		if (exp.getRoot() != null) {
			Set<Long> ids = queryKeys(null, query, exp.getRoot()); 
			return checkDocumentsCommited(ids);
		}
		logger.info("getDocumentIDs; got rootless path: {}", query); 
		// can we use local keySet only !?
		List<Long> result = new ArrayList<Long>(xddCache.keySet().size());
		for (XDMDocumentKey docKey: xddCache.keySet()) {
			// we must provide only visible docIds!
			if (checkDocumentCommited(docKey.getKey())) {
				result.add(docKey.getKey());
			}
		}
		return result;
	}

	//private Collection<XDMDocumentKey> getDocumentKeys(ExpressionContainer query) {
	//	return null;
	//}
	
	@Override
	public Collection<String> getDocumentURIs(ExpressionContainer query) throws XDMException {
		// TODO: remove this method completely, or
		// make reverse cache..? or, make URI from docId somehow..
		Collection<Long> ids = getDocumentIDs(query);
		Set<XDMDocumentKey> keys = new HashSet<XDMDocumentKey>(ids.size());
		for (Long id: ids) {
			keys.add(docMgr.getXdmFactory().newXDMDocumentKey(id));
		}
		Set<String> result = new HashSet<String>(ids.size());
		// TODO: better to do this via EP or aggregator?
		Map<XDMDocumentKey, XDMDocument> docs = xddCache.getAll(keys);
		for (XDMDocument doc: docs.values()) {
			result.add(doc.getUri());
		}
		return result;
	}

	@Override
	public Collection<String> getXML(ExpressionContainer query, String template, Map params) throws XDMException {
		
		// TODO: get rid of transaction management here! 
		// it is here for easier tests management only!
		long txId = 0;
		if (txMgr.getCurrentTxId() == TX_NO) {
			txId = txMgr.beginTransaction();
		}
		Collection<Long> docIds = getDocumentIDs(query);
		if (txId > TX_NO) {
			txMgr.commitTransaction(txId);
		}
		if (docIds.size() > 0) {
			return docMgr.buildDocument(new HashSet<Long>(docIds), template, params);
		}
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean isReadOnlyQuery(String query) {

		XDMQuery xQuery = this.getQuery(query);
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
	public Iterator executeXCommand(String command, Map bindings, Properties props) {
		
		return execXQCommand(false, command, bindings, props);
	}

	@Override
	public Iterator executeXQuery(String query, Map bindings, Properties props) {

		return execXQCommand(true, query, bindings, props);
	}

	private Iterator execXQCommand(boolean isQuery, String xqCmd, Map bindings, Properties props) {

		logger.trace("execXQCommand.enter; query: {}, command: {}; bindings: {}; properties: {}", 
				isQuery, xqCmd, bindings, props);
		ResultCursor result = null;
		Iterator iter = null;
		String clientId = props.getProperty(pn_client_id);
		int batchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
		try {
			//if (isQuery) {
			//	iter = queryManager.getQueryResults(xqCmd, bindings, props);
			//}
			
			XQProcessor xqp = repo.getXQProcessor(clientId);
			xqp.setResults(null);
			if (iter == null) {
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
			xqp.setResults(result);
		} catch (Throwable ex) {
			logger.error("execXQCommand.error;", ex);
			result = createCursor(clientId, 0, new ExceptionIterator(ex), true);
		}
		logger.trace("execXQCommand.exit; returning: {}, for client: {}", result, clientId);
		return result;
	}

	private ResultCursor createCursor(String clientId, int batchSize, Iterator iter, boolean failure) {
		int size = ResultCursor.UNKNOWN;
		if (iter instanceof XQSequenceIterator) {
			size = ((XQSequenceIterator) iter).getFullSize();
		}
		final ResultCursor xqCursor = new ResultCursor(clientId, batchSize, iter, size, failure);
		
		// async serialization takes even more time! because of the thread context switch, most probably
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
