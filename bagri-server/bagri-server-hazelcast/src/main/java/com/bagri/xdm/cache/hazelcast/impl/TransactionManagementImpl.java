package com.bagri.xdm.cache.hazelcast.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMTransactionManagement;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionContext;

public class TransactionManagementImpl implements XDMTransactionManagement {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
	
    private HazelcastInstance hzInstance;
	private boolean isInTx = false;

	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
	}
	
	
	@Override
	public void beginTransaction() {
		if (isInTx) {
			logger.trace("beginTransaction; in transaction now: yes"); 
			// commit or throw ex?
		} else {
			logger.trace("beginTransaction; in transaction now: no"); 
		}
		isInTx = true;
		
		//TransactionContext txCtx = hzInstance.newTransactionContext();
		//txCtx.getTxnId();
	}

	@Override
	public void commitTransaction() {
		if (!isInTx) {
			logger.trace("commitTransaction; in transaction now: no"); 
			// throw ex?
			return;
		} else {
			logger.trace("commitTransaction; in transaction now: yes"); 
		}
		// perform commit via Hazelcast...
	}

	@Override
	public void rollbackTransaction() {
		if (!isInTx) {
			logger.trace("rollbackTransaction; in transaction now: no"); 
			// throw ex?
			return;
		} else {
			logger.trace("rollbackTransaction; in transaction now: yes"); 
		}
		// perform rollback via Hazelcast...
	}


}
