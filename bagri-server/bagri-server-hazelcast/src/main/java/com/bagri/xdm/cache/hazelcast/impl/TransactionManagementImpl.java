package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.api.XDMException.ecTransNoNested;
import static com.bagri.xdm.api.XDMException.ecTransNotFound;
import static com.bagri.xdm.api.XDMException.ecTransWrongState;
import static com.bagri.xdm.cache.api.CacheConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.stats.StatisticsProvider;
import com.bagri.common.util.JMXUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.HealthState;
import com.bagri.xdm.api.TransactionIsolation;
import com.bagri.xdm.api.TransactionState;
import com.bagri.xdm.cache.api.TransactionManagement;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentCleaner;
import com.bagri.xdm.client.hazelcast.impl.IdGeneratorImpl;
import com.bagri.xdm.domain.Counter;
import com.bagri.xdm.domain.Transaction;
import com.bagri.xdm.system.TriggerAction.Order;
import com.bagri.xdm.system.TriggerAction.Scope;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiExecutionCallback;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class TransactionManagementImpl implements TransactionManagement, StatisticsProvider, MultiExecutionCallback {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
    private static final long TX_START = 5L; 
	
	private ThreadLocal<Long> thTx = new ThreadLocal<Long>() {
		
		@Override
		protected Long initialValue() {
			return TX_NO;
 		}
		
	};
	
	private AtomicLong cntStarted = new AtomicLong(0);
	private AtomicLong cntCommited = new AtomicLong(0);
	private AtomicLong cntRolled = new AtomicLong(0);
    
	private SchemaRepositoryImpl repo;
    //private HazelcastInstance hzInstance;
	private Cluster cluster;
	private IdGenerator<Long> txGen;
	private ITopic<Counter> cTopic;
	private IExecutorService execService;
	private IMap<Long, Transaction> txCache; 
    private TriggerManagementImpl triggerManager;

	private long txTimeout = 0;
	
    public void setRepository(SchemaRepositoryImpl repo) {
    	this.repo = repo;
    	triggerManager = (TriggerManagementImpl) repo.getTriggerManagement();
    	setHzInstance(repo.getHzInstance());
    }
	
	public void setHzInstance(HazelcastInstance hzInstance) {
		//this.hzInstance = hzInstance;
		cluster = hzInstance.getCluster();
		txCache = hzInstance.getMap(CN_XDM_TRANSACTION);
		txGen = new IdGeneratorImpl(hzInstance.getAtomicLong(SQN_TRANSACTION));
		txGen.adjust(TX_START);
		cTopic = hzInstance.getTopic(TPN_XDM_COUNTERS);
		execService = hzInstance.getExecutorService(PN_XDM_TRANS_POOL);
	}
	
	public long getTransactionTimeout() {
		return txTimeout;
	}
	
	public void setTransactionTimeout(long timeout) throws XDMException {
		this.txTimeout = timeout;
	}
	
	public void adjustTxCounter() {
		Set<Long> ids = txCache.localKeySet();
		if (ids.size() > 0) {
			Long maxId = Collections.max(ids);
			boolean adjusted = txGen.adjust(maxId);
			logger.info("adjustTxCounter; found maxTxId: {}; adjusted: {}", maxId, adjusted);
		}
	}
	
	@Override
	public long beginTransaction() throws XDMException {
		// get default isolation level from some config..
		return beginTransaction(TransactionIsolation.readCommited);
	}

	@Override
	public long beginTransaction(TransactionIsolation txIsolation) throws XDMException {
		logger.trace("beginTransaction.enter; txIsolation: {}", txIsolation); 
		long txId = thTx.get();
		if (txId > TX_NO && txCache.containsKey(txId)) {
			throw new XDMException("nested transactions are not supported; current txId: " + txId, ecTransNoNested);
		}

		txId = txGen.next();
		// TODO: do this via EntryProcessor?
		Transaction xTx = new Transaction(txId, cluster.getClusterTime(), 0, repo.getUserName(), txIsolation, TransactionState.started);
		triggerManager.applyTrigger(xTx, Order.before, Scope.begin); 
		txCache.set(txId, xTx);
		thTx.set(txId);
		cntStarted.incrementAndGet();
		triggerManager.applyTrigger(xTx, Order.after, Scope.begin); 
		logger.trace("beginTransaction.exit; started tx: {}; returning: {}", xTx, txId); 
		return txId;
	}

	@Override
	public void commitTransaction(long txId) throws XDMException {
		logger.trace("commitTransaction.enter; got txId: {}", txId); 
		// TODO: do this via EntryProcessor?
		Transaction xTx = txCache.get(txId);
		if (xTx != null) {
			triggerManager.applyTrigger(xTx, Order.before, Scope.commit); 
			xTx.finish(true, cluster.getClusterTime());
			//txCache.delete(txId);
			txCache.set(txId, xTx);
		} else {
			throw new XDMException("no transaction found for TXID: " + txId, ecTransNotFound);
		}
		thTx.set(TX_NO);
		cntCommited.incrementAndGet();
		triggerManager.applyTrigger(xTx, Order.after, Scope.commit); 
		cTopic.publish(new Counter(true, xTx.getDocsCreated(), xTx.getDocsUpdated(), xTx.getDocsDeleted()));
		cleanAffectedDocuments(xTx);
		logger.trace("commitTransaction.exit; tx: {}", xTx); 
	}

	@Override
	public void rollbackTransaction(long txId) throws XDMException {
		logger.trace("rollbackTransaction.enter; got txId: {}", txId); 
		// TODO: do this via EntryProcessor?
		Transaction xTx = txCache.get(txId);
		if (xTx != null) {
			triggerManager.applyTrigger(xTx, Order.before, Scope.rollback); 
			xTx.finish(false, cluster.getClusterTime());
			txCache.set(txId, xTx);
		} else {
			throw new XDMException("No transaction found for TXID: " + txId, ecTransNotFound);
		}
		thTx.set(TX_NO);
		cntRolled.incrementAndGet();
		triggerManager.applyTrigger(xTx, Order.after, Scope.rollback); 
		cTopic.publish(new Counter(false, xTx.getDocsCreated(), xTx.getDocsUpdated(), xTx.getDocsDeleted()));
		cleanAffectedDocuments(xTx);
		logger.trace("rollbackTransaction.exit; tx: {}", xTx); 
	}
	
	@Override
	public boolean finishCurrentTransaction(boolean rollback) throws XDMException {
		long txId = getCurrentTxId();
		if (txId > TX_NO) {
			if (rollback) {
				rollbackTransaction(txId);
			} else {
				commitTransaction(txId);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isInTransaction() {
		return getCurrentTxId() > TX_NO; 
	}
	
	private void cleanAffectedDocuments(Transaction xTx) {
		// asynchronous cleaning..
		//execService.submitToAllMembers(new DocumentCleaner(xTx), this);
		
		// synchronous cleaning.. causes a deadlock if used from the common schema exec-pool. 
		// that is why we use separate exec-pool for transaction tasks 
		Map<Member, Future<Transaction>> values = execService.submitToAllMembers(new DocumentCleaner(xTx));
        Transaction txClean = null;
		for (Future<Transaction> value: values.values()) {
			try {
				Transaction tx = value.get();
				if (txClean == null) {
					txClean = tx;
				} else {
					txClean.updateCounters(tx.getDocsCreated(), tx.getDocsUpdated(), tx.getDocsDeleted());
				}
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("cleanAffectedDocuments.error;", ex);
			}
		}
		logger.trace("cleanAffectedDocuments; going to complete {}", txClean);
		completeTransaction(txClean); 
	}
	
	boolean isTxVisible(long txId) throws XDMException {
		long cTx = getCurrentTxId();
		if (txId == cTx) {
			// current tx;
			return true;
		}

		Transaction xTx;
		TransactionIsolation txIsolation;
		if (cTx != TX_NO) {
			// can not be null!
			xTx = txCache.get(cTx);
			if (xTx == null) {
				throw new XDMException("Can not find current Transaction with txId " + cTx + "; txId: " + txId, ecTransNotFound);
			}
	
			// current tx is already finished!
			if (xTx.getTxState() != TransactionState.started) {
				throw new XDMException("Current Transaction is already " + xTx.getTxState(), ecTransWrongState);
			}
				
			txIsolation = xTx.getTxIsolation(); 
			if (txIsolation == TransactionIsolation.dirtyRead) {
				// current tx is dirtyRead, can see not-committed tx results
				return true;
			}
		} else {
			// default isolation level
			txIsolation = TransactionIsolation.readCommited;
		}
		
		xTx = txCache.get(txId);
		boolean commited = xTx == null || xTx.getTxState() == TransactionState.commited;
		if (txIsolation == TransactionIsolation.readCommited) {
			return commited;
		}

		// txIsolation is repeatableRead or serializable
		if (txId > cTx) {
			// the tx started after current, so it is not visible
			// for current tx
			return false;
		}
		return commited; 
	}
	
	long getCurrentTxId() {
		return thTx.get(); 
	}
	
	void flushCurrentTx() throws XDMException {
		long txId = getCurrentTxId();
		if (txId > TX_NO) {
			rollbackTransaction(txId);
		}
	}
	
	void updateCounters(int created, int updated, int deleted) throws XDMException {
		long txId = getCurrentTxId();
		if (txId > TX_NO) {
			Transaction xTx = txCache.get(txId);
			if (xTx != null) {
				xTx.updateCounters(created, updated, deleted);
				txCache.set(txId, xTx);
			} else {
				throw new XDMException("no transaction found for TXID: " + txId, ecTransNotFound);
			}
		} else {
			throw new XDMException("not in transaction", ecTransWrongState);
		}
	}
	
	@Override
	public <V> V callInTransaction(long txId, boolean readOnly, Callable<V> call) throws XDMException {
		
		logger.trace("callInTransaction.enter; got txId: {}", txId);
		boolean autoCommit = txId == TX_NO; 
		if (autoCommit) {
			// do not begin tx if it is read-only!
			if (!readOnly) {
				txId = beginTransaction();
			}
		} else {
			thTx.set(txId);
		}
		readOnly = txId == TX_NO;
		
		try {
			V result = call.call();
			if (autoCommit && !readOnly) {
				commitTransaction(txId);
			}
			logger.trace("callInTransaction.exit; returning: {}", result);
			return result;
		} catch (Exception ex) {
			logger.error("callInTransaction.error; in transaction: " + txId, ex);
			// even for non autoCommit ?!
			if (!readOnly) {
				rollbackTransaction(txId);
			}
			if (ex instanceof XDMException) {
				throw (XDMException) ex;
			}
			throw new XDMException(ex, XDMException.ecTransaction);
		}
	}

	@Override
	public CompositeData getStatisticTotals() {
		Map<String, Object> result = new HashMap<String, Object>(4);
		long started = cntStarted.get();
		long commited = cntCommited.get();
		long rolled = cntRolled.get();
		result.put("Started", started);
		result.put("Commited", commited);
		result.put("Rolled Back", rolled);
		result.put("In Progress", started - commited - rolled);
		return JMXUtils.mapToComposite("Transaction statistics", "Transaction statistics", result);
	}

	@Override
	public TabularData getStatisticSeries() {
		// return InProgress Transactions here!?
   		Predicate<Long, Transaction> f = Predicates.equal("txState", TransactionState.started);
		Collection<Transaction> txStarted = txCache.values(f);
		if (txStarted == null || txStarted.isEmpty()) {
			return null;
		}
		
        TabularData result = null;
    	String desc = "InProgress Transactions";
    	String name = "InProgress Transactions";
    	String header = "txId"; 
        for (Transaction xTx: txStarted) {
            try {
                Map<String, Object> txStats = xTx.convert();
                //stats.put(header, entry.getKey());
                CompositeData data = JMXUtils.mapToComposite(name, desc, txStats);
                result = JMXUtils.compositeToTabular(name, desc, header, result, data);
            } catch (Exception ex) {
                logger.error("getStatisticSeries; error", ex);
            }
        }
        //logger.trace("getStatisticSeries.exit; returning: {}", result);
        return result;
	}

	@Override
	public void resetStatistics() {
		cntStarted = new AtomicLong(0);
		cntCommited = new AtomicLong(0);
		cntRolled = new AtomicLong(0);
	}

	@Override
	public void onResponse(Member member, Object value) {
        logger.trace("onResponse; got response: {} from member: {}", value, member);
	}

	@Override
	public void onComplete(Map<Member, Object> values) {
        logger.trace("onComplete; got values: {}", values);
        Transaction txClean = null;
		for (Object value: values.values()) {
			Transaction tx = (Transaction) value;
			if (txClean == null) {
				txClean = tx;
			} else {
				txClean.updateCounters(tx.getDocsCreated(), tx.getDocsUpdated(), tx.getDocsDeleted());
			}
		}
		if (txClean != null) {
			completeTransaction(txClean);
		} else {
			logger.info("onComplete; got empty complete response");
		}
	}
	
	private void completeTransaction(Transaction txClean) {
		Transaction txSource = txCache.get(txClean.getTxId());
		if (txSource != null) {
			if (txSource.getDocsCreated() != txClean.getDocsCreated() ||
				txSource.getDocsUpdated() != txClean.getDocsUpdated() ||
				txSource.getDocsDeleted() != txClean.getDocsDeleted()) {
				logger.info("completeTransaction; wrong number of cleaned documents; expected: {}, reported: {}", txSource, txClean);
			}
			txCache.delete(txClean.getTxId());
		} else {
			logger.info("completeTransaction; got complete response for unknown tx: {}", txClean);
		}
	}

}
