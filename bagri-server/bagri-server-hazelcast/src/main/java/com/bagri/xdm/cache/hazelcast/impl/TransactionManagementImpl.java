package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_TRANSACTION;
import static com.bagri.xdm.client.common.XDMCacheConstants.SQN_TRANSACTION;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.client.common.XDMCacheConstants;
import com.bagri.xdm.client.hazelcast.impl.IdGeneratorImpl;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMTransaction;
import com.hazelcast.core.BaseMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionOptions.TransactionType;

public class TransactionManagementImpl implements XDMTransactionManagement {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
	
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
			// throw ex?
		}
		logger.trace("commitTransaction.exit;"); 
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
			// throw ex?
		}
		logger.trace("rollbackTransaction.exit;"); 
	}
	
	//String getCurrentTxId() {
	//	return txCache.size() > 0 ? txCache.keySet().iterator().next() : null;
	//}

}
