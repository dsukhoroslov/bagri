package com.bagri.client.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_QUERY;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_RESULT;
import static com.bagri.core.server.api.CacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.task.query.QueryExecutor;
import com.bagri.client.hazelcast.task.query.QueryUrisProvider;
import com.bagri.client.hazelcast.task.query.QueryProcessor;
import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.impl.QueryManagementBase;
import com.bagri.core.model.Query;
import com.bagri.core.model.QueryResult;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.ReplicatedMap;

public class QueryManagementImpl extends QueryManagementBase implements QueryManagement {
	
    private final static Logger logger = LoggerFactory.getLogger(QueryManagementImpl.class);

    private String defTxLevel = null;
    private boolean queryCache = true;
    private SchemaRepositoryImpl repo;
	private IExecutorService execService;
    private Future<?> execution = null; 
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
	
	public void setDefaultTxLevel(String txLevel) {
		this.defTxLevel = txLevel;
	}
	
	public void setQueryCache(boolean queryCache) {
		this.queryCache = queryCache;
	}
	
	@Override
	public void cancelExecution() throws BagriException {
		if (execution != null) {
			// synchronize on it?
			if (!execution.isDone()) {
				execution.cancel(true);
			}
		}
	}
	
	private Properties checkQueryProperties(Properties props) {
		if (props == null) {
			props = new Properties();
		}
		props.setProperty(pn_schema_name, repo.getSchemaName());
		props.setProperty(pn_client_id, repo.getClientId());
		props.setProperty(pn_client_txId, String.valueOf(repo.getTransactionId()));
		if (defTxLevel != null) {
			props.setProperty(pn_client_txLevel, defTxLevel);
		}
		return props;
	}
	
	@Override
	public Collection<String> getDocumentUris(String query, Map<String, Object> params, Properties props) throws BagriException {

		logger.trace("getDocumentUris.enter; query: {}", query);
		props = checkQueryProperties(props);
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
	public ResultCursor executeQuery(String query, Map<String, Object> params, Properties props) throws BagriException {

		logger.trace("executeQuery.enter; query: {}; bindings: {}; context: {}", query, params, props);
		props = checkQueryProperties(props);
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
		
		// TODO: implement asynch querying..

		QueryExecutor task = new QueryExecutor(repo.getClientId(), repo.getTransactionId(), query, params, props);
		Future<ResultCursor> future = null;
		String runOn = props.getProperty(pn_client_submitTo, pv_client_submitTo_any);
		if (pv_client_submitTo_all.equalsIgnoreCase(runOn)) {
			// TODO: implement it
			// future = execService.submitToAllMembers(task);
		} else if (pv_client_submitTo_query_key_owner.equalsIgnoreCase(runOn)) {
			future = execService.submitToKeyOwner(task, qKey);
		} else if (pv_client_submitTo_param_hash_owner.equalsIgnoreCase(runOn) || pv_client_submitTo_param_value_owner.equalsIgnoreCase(runOn)) {
			String param = props.getProperty(pn_client_ownerParam);
			if (param == null) {
				logger.info("executeQuery; the routing parameter not provided: {}", props);
			} else {
				Object value = params.get(param);
				if (value == null) {
					logger.info("executeQuery; the routing parameter '{}' not found: {}", param, params);
				} else {
					//logger.info("executeQuery; the routing parameter '{}'; value: {}; owner: {}", param, value,
					//		repo.getHazelcastClient().getPartitionService().getPartition(value).getOwner());
					if (pv_client_submitTo_param_hash_owner.equalsIgnoreCase(runOn)) {
						value = value.toString().hashCode();
					}
					if (value != null) {
						//logger.info("executeQuery; the routing parameter '{}'; value: {}; owner: {}", param, value,
						//		repo.getHazelcastClient().getPartitionService().getPartition(value).getOwner());
						future = execService.submitToKeyOwner(task, value);
					}
				}
			}
		} else {
			// not sure this is correct, just for future investigation..
			Long partKey = new Long(runOn.hashCode());
			//future = execService.submitToKeyOwner(task, partKey);
			QueryProcessor qp = new QueryProcessor(true, query, params, props);
			return (ResultCursor) resCache.executeOnKey(partKey, qp);
		}

		if (future == null) {
			// this is for ANY and default/not implemented cases
			future = execService.submit(task);
		}
		
		execution = future;

		long timeout = Long.parseLong(props.getProperty(pn_xqj_queryTimeout, "0"));

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

	private <T> T getResults(Future<T> future, long timeout) throws BagriException {

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
			throw new BagriException(ex, BagriException.ecQueryTimeout);
		} catch (InterruptedException | ExecutionException ex) {
			int errorCode = BagriException.ecQuery;
			if (ex.getCause() != null && ex.getCause() instanceof CancellationException) {
				errorCode = BagriException.ecQueryCancel;
				logger.warn("getResults.interrupted; request cancelled: {}", future.isCancelled());
			} else {
				future.cancel(false); 
				logger.error("getResults.error; error getting result", ex);
				Throwable err = ex;
				while (err.getCause() != null) {
					err = err.getCause();
					if (err instanceof BagriException) {
						throw (BagriException) err;
					//} else if (err instanceof UndefinedErrorCodeException) {
					//	if (XDMException.class.getName().equals(((UndefinedErrorCodeException) err).getOriginClassName())) {
					//		throw new XDMException(ex.getMessage(), errorCode);
					//	}
					}
				}
			}
			throw new BagriException(ex, errorCode);
		}
	}
	
	@Override
	public Collection<String> prepareQuery(String query) { //throws BagriException {

		logger.trace("prepareQuery.enter; query: {}", query);
		Collection<String> result = null;
		Query xq = xqCache.get(getQueryKey(query));
		if (xq != null) {
			result = xq.getXdmQuery().getParamNames();
		}
		logger.trace("prepareQuery.exit; returning: {}", result);
		return result;
	}
	
}
