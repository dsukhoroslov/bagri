package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_TRANSACTION;
import static com.bagri.xdm.client.common.XDMCacheConstants.SQN_TRANSACTION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.client.hazelcast.impl.IdGeneratorImpl;
import com.bagri.xdm.domain.XDMTransaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class TransactionManagementImpl implements XDMTransactionManagement {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
	
	private ThreadLocal<Long> thTx = new ThreadLocal<Long>() {
		
		@Override
		protected Long initialValue() {
			return XDMTransactionManagement.TX_NO;
 		}
		
	};
    
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
		thTx.set(XDMTransactionManagement.TX_NO);
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
			// throw ex?
		}
		thTx.set(XDMTransactionManagement.TX_NO);
		logger.trace("rollbackTransaction.exit; tx: {}", xTx); 
	}
	
	long getCurrentTxId() {
		return thTx.get(); 
	}

}
