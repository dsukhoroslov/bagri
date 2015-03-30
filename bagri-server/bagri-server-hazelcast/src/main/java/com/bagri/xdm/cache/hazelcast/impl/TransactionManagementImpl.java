package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_TRANSACTION;
import static com.bagri.xdm.client.common.XDMCacheConstants.SQN_TRANSACTION;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.manage.JMXUtils;
import com.bagri.common.stats.StatisticsProvider;
import com.bagri.common.stats.StatisticsCollector.Statistics;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.bagri.xdm.client.hazelcast.impl.IdGeneratorImpl;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.common.XDMTransactionIsolation;
import com.bagri.xdm.common.XDMTransactionState;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMTransaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class TransactionManagementImpl implements XDMTransactionManagement, StatisticsProvider {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
	
	private ThreadLocal<Long> thTx = new ThreadLocal<Long>() {
		
		@Override
		protected Long initialValue() {
			return XDMTransactionManagement.TX_NO;
 		}
		
	};
	
	private AtomicLong cntStarted = new AtomicLong(0);
	private AtomicLong cntCommited = new AtomicLong(0);
	private AtomicLong cntRolled = new AtomicLong(0);
    
    //private HazelcastInstance hzInstance;
	private IdGenerator<Long> txGen;
	private IMap<Long, XDMTransaction> txCache; 

	public void setHzInstance(HazelcastInstance hzInstance) {
		//this.hzInstance = hzInstance;
		txCache = hzInstance.getMap(CN_XDM_TRANSACTION);
		txGen = new IdGeneratorImpl(hzInstance.getAtomicLong(SQN_TRANSACTION));
	}
	
	
	@Override
	public long beginTransaction() {
		logger.trace("beginTransaction.enter;"); 
		long txId = txGen.next();
		// TODO: do this via EntryProcessor?
		XDMTransaction xTx = new XDMTransaction(txId, JMXUtils.getCurrentUser());
		txCache.set(txId, xTx);
		thTx.set(txId);
		cntStarted.incrementAndGet();
		logger.trace("beginTransaction.exit; started tx: {}; returning: {}", xTx, txId); 
		return txId;
	}

	@Override
	public long beginTransaction(XDMTransactionIsolation txIsolation) {
		logger.trace("beginTransaction.enter; txIsolation: {}", txIsolation); 
		long txId = txGen.next();
		// TODO: do this via EntryProcessor?
		XDMTransaction xTx = new XDMTransaction(txId, System.currentTimeMillis(), 0, 
				JMXUtils.getCurrentUser(), txIsolation, XDMTransactionState.started);
		txCache.set(txId, xTx);
		thTx.set(txId);
		cntStarted.incrementAndGet();
		logger.trace("beginTransaction.exit; started tx: {}; returning: {}", xTx, txId); 
		return txId;
	}

	@Override
	public void commitTransaction(long txId) {
		logger.trace("commitTransaction.enter; got txId: {}", txId); 
		// TODO: do this via EntryProcessor?
		XDMTransaction xTx = txCache.get(txId);
		if (xTx != null) {
			xTx.finish(true);
			txCache.set(txId, xTx);
		} else {
			throw new IllegalStateException("No transaction found for TXID: " + txId);
		}
		txCache.delete(txId);
		thTx.set(XDMTransactionManagement.TX_NO);
		cntCommited.incrementAndGet();
		logger.trace("commitTransaction.exit; tx: {}", xTx); 
	}

	@Override
	public void rollbackTransaction(long txId) {
		logger.trace("rollbackTransaction.enter; got txId: {}", txId); 
		// TODO: do this via EntryProcessor?
		XDMTransaction xTx = txCache.get(txId);
		if (xTx != null) {
			xTx.finish(false);
			txCache.set(txId, xTx);
		} else {
			throw new IllegalStateException("No transaction found for TXID: " + txId);
		}
		// do not delete rolled back tx for a while
		thTx.set(XDMTransactionManagement.TX_NO);
		cntRolled.incrementAndGet();
		logger.trace("rollbackTransaction.exit; tx: {}", xTx); 
	}
	
	boolean isTxVisible(long txId) {
		long cTx = getCurrentTxId();
		if (txId == cTx) {
			// current tx;
			return true;
		}
		// can not be null!
		XDMTransaction xTx = txCache.get(cTx);
		if (xTx == null) {
			throw new IllegalStateException("Can not find current Transaction with txId " + cTx + "; txId: " + txId);
		}

		// current tx is not finished yet!
		if (xTx.getTxState() != XDMTransactionState.started) {
			throw new IllegalStateException("Current Transaction is already " + xTx.getTxState());
		}
			
		XDMTransactionIsolation txIsolation = xTx.getTxIsolation(); 
		if (txIsolation == XDMTransactionIsolation.dirtyRead) {
			// current tx is dirtyRead, can see not-committed tx results
			return true;
		}
		
		xTx = txCache.get(txId);
		boolean commited = xTx == null || xTx.getTxState() == XDMTransactionState.commited;
		if (txIsolation == XDMTransactionIsolation.readCommited) {
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
	
	@Override
	public <V> V callInTransaction(long txId, Callable<V> call) {
		
		logger.trace("callInTransaction.enter; got txId: {}", txId);
		boolean autoCommit = txId == TX_NO; 
		if (autoCommit) {
			txId = beginTransaction();
		} else {
			thTx.set(txId);
		}
		
		try {
			V result = call.call();
			// handle ResultCursor with failure = true!
			if (result instanceof ResultCursor) {
				if (((ResultCursor) result).isFailure()) {
					rollbackTransaction(txId);
					autoCommit = false;
				}
			}
			if (autoCommit) {
				commitTransaction(txId);
			}
			logger.trace("callInTransaction.exit; returning: {}", result);
			return result;
		} catch (Exception ex) {
			logger.error("callInTransaction.error; in transaction: " + txId, ex);
			// even for non autoCommit ?!
			rollbackTransaction(txId);
			throw new RuntimeException(ex);
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
   		Predicate<Long, XDMTransaction> f = Predicates.equal("txState", XDMTransactionState.started);
		Collection<XDMTransaction> txStarted = txCache.values(f);
		if (txStarted == null || txStarted.isEmpty()) {
			return null;
		}
		
        TabularData result = null;
    	String desc = "InProgress Transactions";
    	String name = "InProgress Transactions";
    	String header = "txId"; 
        for (XDMTransaction xTx: txStarted) {
            try {
                Map<String, Object> txStats = xTx.toMap();
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

}
