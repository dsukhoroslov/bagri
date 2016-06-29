package com.bagri.xdm.cache.hazelcast.task.tx;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.TransactionManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class TransactionAborter extends com.bagri.xdm.client.hazelcast.task.tx.TransactionAborter {

	private transient TransactionManagement txMgr;
    
    @Autowired
	public void setTxManager(TransactionManagement txMgr) {
		this.txMgr = txMgr;
	}
    
    @Override
	public Boolean call() throws Exception {
		txMgr.rollbackTransaction(txId);
		return true;
	}
	
}
