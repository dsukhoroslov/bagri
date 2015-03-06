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
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

public class TransactionManagementImpl implements XDMTransactionManagement {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
	
	private IExecutorService execService;
    private String txId = null;
    private String clientId = null;

	void initialize(RepositoryImpl repo) {
		HazelcastInstance hzClient = repo.getHazelcastClient();
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		clientId = repo.getClientId();
	}
    
	@Override
	public String beginTransaction() {
		logger.trace("beginTransaction.enter; current txId: {}", txId); 
		if (txId != null) {
			// commit or throw ex?
			return null;
		}

		String result = null;
		//String clientId = ""; //get ClientId somehow..
		TransactionStarter txs = new TransactionStarter(clientId);
		Future<String> future = execService.submitToKeyOwner(txs, clientId);
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("beginTransaction; error getting result", ex);
		}
		txId = result;
		logger.trace("beginTransaction.exit; returnig txId: {}", txId); 
		return txId;
	}

	@Override
	public void commitTransaction(String txId) {
		logger.trace("commitTransaction.enter; current txId: {}", this.txId);
		if (this.txId == null) {
			// throw ex?
			return;
		}

		boolean result = false;
		//String clientId = ""; //get ClientId somehow..
		TransactionCommiter txc = new TransactionCommiter(clientId, this.txId);
		Future<Boolean> future = execService.submitToKeyOwner(txc, clientId);
		try {
			result = future.get();
			this.txId = null;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("commitTransaction; error getting result", ex);
		}
		logger.trace("commitTransaction.exit; commited: {}", result); 
	}

	@Override
	public void rollbackTransaction(String txId) {
		logger.trace("rollbackTransaction.enter; current txId: {}", this.txId);
		if (this.txId == null) {
			// throw ex?
			return;
		}

		boolean result = false;
		//String clientId = ""; //get ClientId somehow..
		TransactionAborter txa = new TransactionAborter(clientId, this.txId);
		Future<Boolean> future = execService.submitToKeyOwner(txa, clientId);
		try {
			result = future.get();
			this.txId = null;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("rollbackTransaction; error getting result", ex);
		}
		logger.trace("rollbackTransaction.exit; rolled back: {}", result); 
	}

}
