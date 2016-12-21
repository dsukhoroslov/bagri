package com.bagri.client.hazelcast.impl;

import static com.bagri.core.server.api.CacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.task.tx.TransactionAborter;
import com.bagri.client.hazelcast.task.tx.TransactionCommiter;
import com.bagri.client.hazelcast.task.tx.TransactionStarter;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.BagriException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

public class TransactionManagementImpl implements TransactionManagement {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
	
    private long txId = TX_NO;
    private long txTimeout = 0;
    private String clientId = null;
    private SchemaRepositoryImpl repo;
	private IExecutorService execService;

	void initialize(SchemaRepositoryImpl repo) {
		this.repo = repo;
		HazelcastInstance hzClient = repo.getHazelcastClient();
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		clientId = repo.getClientId();
	}

	public long getTransactionTimeout() {
		return txTimeout;
	}
	
	public void setTransactionTimeout(long timeout) throws BagriException {
		if (timeout < 0) {
			throw new BagriException("negative timeout value is not supported", BagriException.ecTransaction);
		}
		this.txTimeout = timeout;
	}
	
	@Override
	public boolean isInTransaction() {
		return txId > TX_NO;
	}
	
	@Override
	public long beginTransaction() throws BagriException {
		// TODO: get default value from session config!
		return beginTransaction(TransactionIsolation.readCommited);
	}
	
	@Override
	public long beginTransaction(TransactionIsolation txIsolation) throws BagriException {
		logger.trace("beginTransaction.enter; current txId: {}", txId);
		repo.getHealthManagement().checkClusterState();
		
		if (txId != TX_NO) {
			throw new BagriException("Nested client transactions are not supported", BagriException.ecTransNoNested);
		}

		TransactionStarter txs = new TransactionStarter(clientId, txIsolation);
		Future<Long> future = execService.submitToKeyOwner(txs, clientId);
		try {
			txId = future.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("beginTransaction; error getting result", ex);
			throw new BagriException(ex, BagriException.ecTransaction);
		}
		logger.trace("beginTransaction.exit; returnig txId: {}", txId); 
		return txId;
	}

	@Override
	public void commitTransaction(long txId) throws BagriException {
		logger.trace("commitTransaction.enter; current txId: {}", this.txId);
		if (this.txId != txId) {
			throw new BagriException("Wrong transaction id: " + txId + "; current txId is: " + this.txId, 
					BagriException.ecTransWrongState);
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
			throw new BagriException(ex, BagriException.ecTransaction);
		}
		logger.trace("commitTransaction.exit; commited: {}", result); 
	}

	@Override
	public void rollbackTransaction(long txId) throws BagriException {
		logger.trace("rollbackTransaction.enter; current txId: {}", this.txId);
		if (this.txId != txId) {
			throw new BagriException("Wrong transaction id: " + txId + "; current txId is: " + this.txId, 
					BagriException.ecTransWrongState);
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
			throw new BagriException(ex, BagriException.ecTransaction);
		}
		logger.trace("rollbackTransaction.exit; rolled back: {}", result); 
	}
	
	@Override
	public boolean finishCurrentTransaction(boolean rollback) throws BagriException {
		if (isInTransaction()) {
			if (rollback) {
				rollbackTransaction(txId);
			} else {
				commitTransaction(txId);
			}
			return true;
		}
		return false;
	}
	
	long getTxId() {
		return this.txId;
	}

}
