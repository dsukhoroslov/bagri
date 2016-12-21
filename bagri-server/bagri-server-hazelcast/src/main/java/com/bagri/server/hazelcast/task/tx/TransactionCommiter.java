package com.bagri.server.hazelcast.task.tx;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.TransactionManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class TransactionCommiter extends com.bagri.client.hazelcast.task.tx.TransactionCommiter {

	private transient TransactionManagement txMgr;
    
    @Autowired
	public void setTxManager(TransactionManagement txMgr) {
		this.txMgr = txMgr;
	}
    
    @Override
	public Boolean call() throws Exception {
		txMgr.commitTransaction(txId);
		return true;
	}
	
}
