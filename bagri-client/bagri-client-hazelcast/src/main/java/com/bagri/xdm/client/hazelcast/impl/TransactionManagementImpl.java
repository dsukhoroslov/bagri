package com.bagri.xdm.client.hazelcast.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMTransactionManagement;

public class TransactionManagementImpl implements XDMTransactionManagement {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
	
	private boolean isInTx = false;

	@Override
	public void beginTransaction() {
		if (isInTx) {
			logger.trace("beginTransaction; in transaction now: yes"); 
			// commit or throw ex?
		} else {
			logger.trace("beginTransaction; in transaction now: no"); 
		}
		isInTx = true;
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
