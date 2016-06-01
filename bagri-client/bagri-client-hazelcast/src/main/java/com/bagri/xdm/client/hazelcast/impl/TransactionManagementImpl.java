package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.cache.api.XDMCacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMTransactionIsolation;
import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionAborter;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionCommiter;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionStarter;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

public class TransactionManagementImpl implements XDMTransactionManagement {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
	
    private long txId = TX_NO;
    private long txTimeout = 0;
    private String clientId = null;
    private RepositoryImpl repo;
	private IExecutorService execService;

	void initialize(RepositoryImpl repo) {
		this.repo = repo;
		HazelcastInstance hzClient = repo.getHazelcastClient();
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		clientId = repo.getClientId();
	}

	public long getTransactionTimeout() {
		return txTimeout;
	}
	
	public void setTransactionTimeout(long timeout) throws XDMException {
		if (timeout < 0) {
			throw new XDMException("negative timeout value is not supported", XDMException.ecTransaction);
		}
		this.txTimeout = timeout;
	}
	
	@Override
	public boolean isInTransaction() {
		return txId > TX_NO;
	}
	
	@Override
	public long beginTransaction() throws XDMException {
		// TODO: get default value from session config!
		return beginTransaction(XDMTransactionIsolation.readCommited);
	}
	
	@Override
	public long beginTransaction(XDMTransactionIsolation txIsolation) throws XDMException {
		logger.trace("beginTransaction.enter; current txId: {}", txId);
		repo.getHealthManagement().checkClusterState();
		
		if (txId != TX_NO) {
			throw new XDMException("Nested client transactions are not supported", XDMException.ecTransNoNested);
		}

		TransactionStarter txs = new TransactionStarter(clientId, txIsolation);
		Future<Long> future = execService.submitToKeyOwner(txs, clientId);
		try {
			txId = future.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("beginTransaction; error getting result", ex);
			throw new XDMException(ex, XDMException.ecTransaction);
		}
		logger.trace("beginTransaction.exit; returnig txId: {}", txId); 
		return txId;
	}

	@Override
	public void commitTransaction(long txId) throws XDMException {
		logger.trace("commitTransaction.enter; current txId: {}", this.txId);
		if (this.txId != txId) {
			throw new XDMException("Wrong transaction id: " + txId + "; current txId is: " + this.txId, 
					XDMException.ecTransWrongState);
		}

		boolean result = false;
		TransactionCommiter txc = new TransactionCommiter(clientId, this.txId);
		Future<Boolean> future = execService.submitToKeyOwner(txc, clientId);
		try {
			result = future.get();
			this.txId = TX_NO;
		} catch (InterruptedException | ExecutionException ex) {
			this.txId = TX_NO;
			logger.error("commitTransaction; error getting result", ex);
			throw new XDMException(ex, XDMException.ecTransaction);
		}
		logger.trace("commitTransaction.exit; commited: {}", result); 
	}

	@Override
	public void rollbackTransaction(long txId) throws XDMException {
		logger.trace("rollbackTransaction.enter; current txId: {}", this.txId);
		if (this.txId != txId) {
			throw new XDMException("Wrong transaction id: " + txId + "; current txId is: " + this.txId, 
					XDMException.ecTransWrongState);
		}

		boolean result = false;
		TransactionAborter txa = new TransactionAborter(clientId, this.txId);
		Future<Boolean> future = execService.submitToKeyOwner(txa, clientId);
		try {
			result = future.get();
			this.txId = TX_NO;
		} catch (InterruptedException | ExecutionException ex) {
			this.txId = TX_NO;
			logger.error("rollbackTransaction; error getting result", ex);
			throw new XDMException(ex, XDMException.ecTransaction);
		}
		logger.trace("rollbackTransaction.exit; rolled back: {}", result); 
	}
	
	@Override
	public void finishCurrentTransaction(boolean rollback) throws XDMException {
		if (rollback) {
			rollbackTransaction(txId);
		} else {
			commitTransaction(txId);
		}
	}
	
	long getTxId() {
		return this.txId;
	}

}
