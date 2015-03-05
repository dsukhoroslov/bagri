package com.bagri.xdm.cache.hazelcast.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.client.common.XDMCacheConstants;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.hazelcast.core.BaseMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;

public class TransactionManagementImpl implements XDMTransactionManagement {
	
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagementImpl.class);
	
    private HazelcastInstance hzInstance;
	private boolean isInTx = false;
	private Map<String, TransactionContext> txCache; // = new ConcurrentHashMap<>();

    private IMap<Long, String> xmlCache;
	private IMap<Long, XDMDocument> xddCache;
    private IMap<XDMDataKey, XDMElements> xdmCache;
	
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		txCache = new ConcurrentHashMap<>();
		xmlCache = hzInstance.getMap(XDMCacheConstants.CN_XDM_XML);
		xddCache = hzInstance.getMap(XDMCacheConstants.CN_XDM_DOCUMENT);
		xdmCache = hzInstance.getMap(XDMCacheConstants.CN_XDM_ELEMENT);
		//txCache = hzInstance.getMap("xdm-tx");
		//com.hazelcast.nio.serialization.HazelcastSerializationException: 
		//There is no suitable serializer for class com.hazelcast.transaction.impl.TransactionContextImpl
	}
	
	
	@Override
	public String beginTransaction() {
		logger.trace("beginTransaction.enter;"); 
		TransactionContext txCtx = hzInstance.newTransactionContext();
		String txId = txCtx.getTxnId();
		txCache.put(txId, txCtx);
		txCtx.beginTransaction();
		logger.trace("beginTransaction.exit; got context: {}; returning: {}", txCtx, txId); 
		//TransactionalMap<Long, XDMDocument> txDocCache = txCtx.getMap(XDMCacheConstants.CN_XDM_DOCUMENT);
		//logger.trace("beginTransaction.exit; got transactional cache: {};", txDocCache); 
		return txId;
	}

	@Override
	public void commitTransaction(String txId) {
		logger.trace("commitTransaction.enter; got txId: {}", txId); 
		// perform commit via Hazelcast...
		TransactionContext txCtx = txCache.get(txId);
		if (txCtx != null) {
			txCtx.commitTransaction();
			txCache.remove(txId);
		} else {
			// throw ex?
		}
		logger.trace("commitTransaction.exit;"); 
	}

	@Override
	public void rollbackTransaction(String txId) {
		logger.trace("rollbackTransaction.enter; got txId: {}", txId); 
		// perform rollback via Hazelcast...
		TransactionContext txCtx = txCache.get(txId);
		if (txCtx != null) {
			txCtx.rollbackTransaction();
			txCache.remove(txId);
		} else {
			// throw ex?
		}
		logger.trace("rollbackTransaction.exit;"); 
	}
	
	String getCurrentTxId() {
		return txCache.size() > 0 ? txCache.keySet().iterator().next() : null;
	}

	BaseMap getTransactionalCache(String txId, String cacheName) {
		if (txId == null) {
			return hzInstance.getMap(cacheName);
		}
		
		TransactionContext txCtx = txCache.get(txId);
		if (txCtx != null) {
			return txCtx.getMap(cacheName);
		}
		// throw ex?
		return null;
	}

}
