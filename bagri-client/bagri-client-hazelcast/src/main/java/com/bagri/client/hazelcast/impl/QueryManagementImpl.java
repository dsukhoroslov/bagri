package com.bagri.client.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_QUERY;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_RESULT;
import static com.bagri.core.server.api.CacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.task.query.QueryExecutor;
import com.bagri.client.hazelcast.task.query.QueryUrisProvider;
import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.impl.QueryManagementBase;
import com.bagri.core.model.Query;
import com.bagri.core.model.QueryResult;
import com.hazelcast.core.ICompletableFuture;
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
    private AtomicLong runIdx = new AtomicLong(0);
    
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
	public ResultCursor<String> getDocumentUris(String query, Map<String, Object> params, Properties props) throws BagriException {

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
				return new FixedCursorImpl<String>(res.getDocUris());
			}
		}
		
		Callable<ResultCursor<String>> task = new QueryUrisProvider(repo.getClientId(), repo.getTransactionId(), query, params, props);
		ResultCursor<String> cursor = executeQueryTask(task, params, props, qKey);
		logger.trace("getDocumentUris.exit; returning: {}", cursor);
		return cursor;
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> ResultCursor<T> executeQuery(String query, Map<String, Object> params, Properties props) throws BagriException {

		logger.trace("executeQuery.enter; query: {}; params: {}; context: {}", query, params, props);
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

		ResultCursor<T> cursor = null;
		String splitBy = props.getProperty(pn_query_splitBy);
		String fetchType = props.getProperty(pn_client_fetchType, pv_client_fetchType_fixed);
		props.setProperty(pn_client_fetchAsynch, String.valueOf(pv_client_fetchType_queue.equals(fetchType)));
		if (splitBy != null) {
			cursor = executeSplitQuery(query, params, props, splitBy, useCache);
		}
		if (cursor == null) {
			Callable<ResultCursor<T>> task = new QueryExecutor(repo.getClientId(), repo.getTransactionId(), query, params, props);
			cursor = executeQueryTask(task, params, props, qKey);
		}
		logger.debug("executeQuery.exit; returning: {}", cursor);
		return cursor; 
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> ResultCursor<T> executeSplitQuery(String query, Map<String, Object> params, final Properties props, String splitBy, boolean useCache) throws BagriException {
		Object param = params.get(splitBy);
		if (param != null && param instanceof Collection) {
			Collection collect = (Collection) param;
			if (collect.size() > 1) {
				// split...
				int limit = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
				CombinedCursorImpl<T> cci = new CombinedCursorImpl<>(limit); 
				Iterator<Object> itr = collect.iterator();

				String fetchType = props.getProperty(pn_client_fetchType, pv_client_fetchType_fixed);
				boolean doBatch = pv_client_fetchType_batch.equals(fetchType);
				List<Future<ResultCursor<T>>> futures = null;
				if (doBatch) {
					futures = new ArrayList<>(collect.size());
				}
				while (itr.hasNext()) {
					List<Object> splitSeq = new ArrayList<>(1);
					splitSeq.add(itr.next());
					Map<String, Object> splitParams = new HashMap<>(params);
					splitParams.remove(splitBy);
					splitParams.put(splitBy, splitSeq);
					long splitQKey = getResultsKey(query, splitParams);
					if (useCache) {
						QueryResult splitRes = resCache.get(splitQKey);
						if (splitRes != null) {
							logger.trace("executeSplitQuery; got cached split results: {}", splitRes);
							cci.addResults(new FixedCursorImpl(splitRes.getResults()));
							continue;
						}
					}
					
					Callable<ResultCursor<T>> task = new QueryExecutor(repo.getClientId(), repo.getTransactionId(), query, splitParams, props);
					if (doBatch) {
						futures.addAll(submitQueryTask(task, params, props, splitQKey));
					} else {
						cci.addResults(executeQueryTask(task, params, props, splitQKey));
					}
				}
				if (doBatch && !futures.isEmpty()) {
					cci.addResults(getResults(futures, props));
				}
				return cci; 
			}
		}
		return null;
	}
	
	private <T> ResultCursor<T> executeQueryTask(Callable<ResultCursor<T>> task, Map<String, Object> params, Properties props, long qKey) throws BagriException {
		
		List<Future<ResultCursor<T>>> futures = submitQueryTask(task, params, props, qKey);
	
		String fetchType = props.getProperty(pn_client_fetchType, pv_client_fetchType_fixed);
		if (pv_client_fetchType_asynch.equals(fetchType)) {
			int fetchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
			AsynchCursorImpl<T> cursor = new AsynchCursorImpl<>(fetchSize, futures.size());
			for (Future<ResultCursor<T>> f: futures) {
				if (f instanceof ICompletableFuture) {
					ICompletableFuture<ResultCursor<T>> icf = (ICompletableFuture<ResultCursor<T>>) f;
					icf.andThen(cursor);
				} else {
					logger.info("executeQueryTask; got unexpected future: {}", f);
				}
			}
			logger.debug("executeQueryTask.exit; returning: {}", cursor);
			return cursor;
		}

		//if (cursor != null && cursor instanceof QueuedCursorImpl) {
		//  purge queue, fetch/close current cursor..?
		//}
		
		ResultCursor<T> cursor = getResults(futures, props);
		if (cursor.isAsynch()) {
			((QueuedCursorImpl<T>) cursor).init(repo.getHazelcastClient());
		}
		
		logger.debug("executeQueryTask.exit; returning: {}", cursor);
		return cursor; 
	}
	
	private <T> List<Future<ResultCursor<T>>> submitQueryTask(Callable<ResultCursor<T>> task, Map<String, Object> params, Properties props, long qKey) throws BagriException {
		
		List<Future<ResultCursor<T>>> results = null;
		String runOn = props.getProperty(pn_client_submitTo, pv_client_submitTo_any);
		if (pv_client_submitTo_all.equalsIgnoreCase(runOn)) {
			// run query on all nodes in cluster
			Map<Member, Future<ResultCursor<T>>> futures = execService.submitToAllMembers(task);
			results = new ArrayList<>(futures.values());
		} else {
			Object runKey = null;
			results = new ArrayList<>(1);
			if (pv_client_submitTo_query_key_owner.equalsIgnoreCase(runOn)) {
				runKey = qKey;
			} else if (pv_client_submitTo_param_hash_owner.equalsIgnoreCase(runOn) || pv_client_submitTo_param_value_owner.equalsIgnoreCase(runOn)) {
				String param = props.getProperty(pn_client_ownerParam);
				if (param == null) {
					logger.debug("submitQueryTask; the routing parameter not provided: {}", props);
				} else {
					runKey = params.get(param);
					if (runKey == null) {
						logger.debug("submitQueryTask; the routing parameter '{}' not found: {}", param, params);
					} else {
						if (pv_client_submitTo_param_hash_owner.equalsIgnoreCase(runOn)) {
							runKey = runKey.toString().hashCode();
						}
					}
				}
			//} else {
				// just for future investigation..
				// runKey = new Long(runOn.hashCode());
			}

			Member owner = null;
			if (runKey == null) {
				// this is for any/default cases: balance job between nodes...
				Collection<Member> members = repo.getHazelcastClient().getCluster().getMembers();
				int cnt = (int) (runIdx.incrementAndGet() % members.size());
				Iterator<Member> itr = members.iterator();
				for (int i=0; i <= cnt; i++) {
					owner = itr.next();
				}
				logger.debug("submitQueryTask; routing task to node: {}; runIdx: {}; cnt: {}", owner, runIdx, cnt);
				results.add(execService.submitToMember(task, owner));
			} else {
				owner = repo.getHazelcastClient().getPartitionService().getPartition(runKey).getOwner();
				results.add(execService.submitToKeyOwner(task, runKey));
			}
		}
		return results;
	}	

	@SuppressWarnings({ "unchecked", "resource" })
	private <T> ResultCursor<T> getResults(List<Future<ResultCursor<T>>> futures, Properties props) throws BagriException {

		boolean asynch = Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"));
		long timeout = Long.parseLong(props.getProperty(pn_xqj_queryTimeout, "0"));

		ResultCursor<T> result;
		Future<ResultCursor<T>> future; 
		if (asynch) {
			// get the fastest result somehow..
			// no need to use combined cursor as results from all members 
			// will go to the queue anyway 
			future = futures.iterator().next();
			result = getResult(future, timeout);
			((QueuedCursorImpl<ResultCursor<T>>) result).init(repo.getHazelcastClient());
		} else {
			if (futures.size() > 1) { 
				int fetchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
				CombinedCursorImpl<T> comb = new CombinedCursorImpl<>(fetchSize);
				for (Future<ResultCursor<T>> f: futures) {
					comb.addResults(getResult(f, timeout));
				}
				result = comb;
			} else {
				result = getResult(futures.iterator().next(), timeout);
			}
		}
		return result;
	}
	
	private <T> ResultCursor<T> getResult(Future<ResultCursor<T>> future, long timeout) throws BagriException {
	
		ResultCursor<T> result;
		execution = future;
		try {
			if (timeout > 0) {
				result = future.get(timeout, TimeUnit.MILLISECONDS); 
			} else {
				result = future.get();
			}
			return result;
		} catch (TimeoutException ex) {
			logger.warn("getResult.timeout; request timed out after {}; cancelled: {}", timeout, future.isCancelled());
			future.cancel(true);
			throw new BagriException(ex, BagriException.ecQueryTimeout);
		} catch (InterruptedException | ExecutionException ex) {
			int errorCode = BagriException.ecQuery;
			if (ex.getCause() != null && ex.getCause() instanceof CancellationException) {
				errorCode = BagriException.ecQueryCancel;
				logger.warn("getResult.interrupted; request cancelled: {}", future.isCancelled());
			} else {
				future.cancel(false); 
				logger.error("getResult.error; error getting result", ex);
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
