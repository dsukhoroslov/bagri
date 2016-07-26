package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.cache.api.CacheConstants.CN_XDM_QUERY;
import static com.bagri.xdm.cache.api.CacheConstants.CN_XDM_RESULT;
import static com.bagri.xdm.cache.api.CacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.xdm.common.Constants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.impl.QueryManagementBase;
import com.bagri.xdm.client.hazelcast.task.query.QueryUrisProvider;
import com.bagri.xdm.client.hazelcast.task.query.QueryExecutor;
import com.bagri.xdm.domain.Query;
import com.bagri.xdm.domain.QueryResult;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.ReplicatedMap;

public class QueryManagementImpl extends QueryManagementBase implements QueryManagement {
	
    private final static Logger logger = LoggerFactory.getLogger(QueryManagementImpl.class);
	
    private boolean queryCache = true;
    private SchemaRepositoryImpl repo;
	private IExecutorService execService;
    private Future execution = null; 
    private IMap<Long, QueryResult> resCache;
    private ReplicatedMap<Integer, Query> xqCache;
    
	public QueryManagementImpl() {
		// what should we do here? 
	}
	
	void initialize(SchemaRepositoryImpl repo) {
		this.repo = repo;
		execService = repo.getHazelcastClient().getExecutorService(PN_XDM_SCHEMA_POOL);
		resCache = repo.getHazelcastClient().getMap(CN_XDM_RESULT);
		xqCache = repo.getHazelcastClient().getReplicatedMap(CN_XDM_QUERY);
	}
	
	public void setQueryCache(boolean queryCache) {
		this.queryCache = queryCache;
	}
	
	@Override
	public void cancelExecution() throws XDMException {
		if (execution != null) {
			// synchronize on it?
			if (!execution.isDone()) {
				execution.cancel(true);
			}
		}
	}
	
	@Override
	public Collection<String> getDocumentUris(String query, Map<String, Object> params, Properties props) throws XDMException {

		logger.trace("getDocumentUris.enter; query: {}", query);
		boolean useCache = this.queryCache; 
		String qCache = props.getProperty(pn_client_queryCache);
		if (qCache != null) {
			useCache = Boolean.parseBoolean(qCache); 
		}
		long qKey = getResultsKey(query, params);
		if (useCache) {
			QueryResult res = resCache.get(qKey);
			if (res != null) {
				logger.trace("getDocumentUris; got cached results: {}", res);
				return res.getDocUris();
			}
		}
		
		QueryUrisProvider task = new QueryUrisProvider(repo.getClientId(), repo.getTransactionId(), query, params, props);
		Future<Collection<String>> future = execService.submit(task);
		execution = future;
		Collection<String> result = getResults(future, 0);
		logger.trace("getDocumentUris.exit; returning: {}", result);
		return result;
	}
	
	@Override
	public ResultCursor executeQuery(String query, Map<String, Object> params, Properties props) throws XDMException {

		logger.trace("executeQuery.enter; query: {}; bindings: {}; context: {}", query, params, props);
		boolean useCache = this.queryCache; 
		String qCache = props.getProperty(pn_client_queryCache);
		if (qCache != null) {
			useCache = Boolean.parseBoolean(qCache); 
		}
		long qKey = getResultsKey(query, params);
		if (useCache) {
			QueryResult res = resCache.get(qKey);
			if (res != null) {
				logger.trace("executeQuery; got cached results: {}", res);
				return new FixedCursorImpl(res.getResults());
			}
		}

		props.setProperty(pn_client_id, repo.getClientId());
		//props.setProperty(pn_client_txId, String.valueOf(repo.getTransactionId()));
		
		QueryExecutor task = new QueryExecutor(repo.getClientId(), repo.getTransactionId(), query, params, props);
		Future<ResultCursor> future;
		String runOn = props.getProperty(pn_client_submitTo, pv_client_submitTo_any);
		if (pv_client_submitTo_owner.equalsIgnoreCase(runOn)) {
			// not sure it'll use partition thread in this case!
			future = execService.submitToKeyOwner(task, qKey);
		} else if (pv_client_submitTo_member.equalsIgnoreCase(runOn)) {
			Member member = repo.getHazelcastClient().getPartitionService().getPartition(qKey).getOwner();
			future = execService.submitToMember(task, member);
		} else {
			future = execService.submit(task);
		}
		execution = future;

		long timeout = Long.parseLong(props.getProperty(pn_queryTimeout, "0"));

		//if (cursor != null && cursor instanceof QueuedCursorImpl) {
		//  purge queue, fetch/close current cursor..?
		//}
		ResultCursor cursor = getResults(future, timeout);
		logger.trace("execXQuery; got cursor: {}", cursor);
		if (cursor instanceof QueuedCursorImpl) {
			((QueuedCursorImpl) cursor).deserialize(repo.getHazelcastClient());
		}
		logger.trace("executeQuery.exit; returning: {}", cursor);
		return cursor; 
	}

	private <T> T getResults(Future<T> future, long timeout) throws XDMException {

		T result;
		try {
			if (timeout > 0) {
				result = future.get(timeout, TimeUnit.MILLISECONDS); 
			} else {
				result = future.get();
			}
			return result;
		} catch (TimeoutException ex) {
			logger.warn("getResults.timeout; request timed out after {}; cancelled: {}", timeout, future.isCancelled());
			future.cancel(true);
			throw new XDMException(ex, XDMException.ecQueryTimeout);
		} catch (InterruptedException | ExecutionException ex) {
			int errorCode = XDMException.ecQuery;
			if (ex.getCause() != null && ex.getCause() instanceof CancellationException) {
				errorCode = XDMException.ecQueryCancel;
				logger.warn("getResults.interrupted; request cancelled: {}", future.isCancelled());
			} else {
				future.cancel(false); 
				logger.error("getResults.error; error getting result", ex);
				Throwable err = ex;
				while (err.getCause() != null) {
					err = err.getCause();
					if (err instanceof XDMException) {
						throw (XDMException) err;
					}
				}
			}
			throw new XDMException(ex, errorCode);
		}
	}
	
	@Override
	public Collection<String> prepareQuery(String query) { //throws XDMException {

		logger.trace("prepareQuery.enter; query: {}", query);
		Collection<String> result = null;
		Query xq = xqCache.get(getQueryKey(query));
		if (xq != null) {
			result = xq.getXdmQuery().getParamNames();
		}
		logger.trace("prepareQuery.exit; returning: {}", query);
		return result;
	}
	
}
