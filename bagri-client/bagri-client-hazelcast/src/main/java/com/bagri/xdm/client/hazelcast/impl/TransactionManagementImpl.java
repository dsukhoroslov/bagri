package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionAborter;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionCommiter;
import com.bagri.xdm.client.hazelcast.task.tx.TransactionStarter;
import com.bagri.xdm.common.XDMTransactionIsolation;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

public class TransactionManagementImpl implements XDMTransactionManagement {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
	
	private IExecutorService execService;
    private long txId = 0;
    private String clientId = null;

	void initialize(RepositoryImpl repo) {
		HazelcastInstance hzClient = repo.getHazelcastClient();
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		clientId = repo.getClientId();
	}

	@Override
	public long beginTransaction() {
		// TODO: get default value from session config!
		return beginTransaction(XDMTransactionIsolation.readCommited);
	}
	
	@Override
	public long beginTransaction(XDMTransactionIsolation txIsolation) {
		logger.trace("beginTransaction.enter; current txId: {}", txId); 
		if (txId != 0) {
			// commit or throw ex?
			return 0;
		}

		//String clientId = ""; //get ClientId somehow..
		TransactionStarter txs = new TransactionStarter(clientId, txIsolation);
		Future<Long> future = execService.submitToKeyOwner(txs, clientId);
		try {
			txId = future.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("beginTransaction; error getting result", ex);
		}
		logger.trace("beginTransaction.exit; returnig txId: {}", txId); 
		return txId;
	}

	@Override
	public void commitTransaction(long txId) {
		logger.trace("commitTransaction.enter; current txId: {}", this.txId);
		if (this.txId == 0) {
			// throw ex?
			return;
		}

		boolean result = false;
		//String clientId = ""; //get ClientId somehow..
		TransactionCommiter txc = new TransactionCommiter(clientId, this.txId);
		Future<Boolean> future = execService.submitToKeyOwner(txc, clientId);
		try {
			result = future.get();
			this.txId = 0;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("commitTransaction; error getting result", ex);
		}
		logger.trace("commitTransaction.exit; commited: {}", result); 
	}

	@Override
	public void rollbackTransaction(long txId) {
		logger.trace("rollbackTransaction.enter; current txId: {}", this.txId);
		if (this.txId == 0) {
			// throw ex?
			return;
		}

		boolean result = false;
		//String clientId = ""; //get ClientId somehow..
		TransactionAborter txa = new TransactionAborter(clientId, this.txId);
		Future<Boolean> future = execService.submitToKeyOwner(txa, clientId);
		try {
			result = future.get();
			this.txId = 0;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("rollbackTransaction; error getting result", ex);
		}
		logger.trace("rollbackTransaction.exit; rolled back: {}", result); 
	}
	
	long getTxId() {
		return this.txId;
	}

}
