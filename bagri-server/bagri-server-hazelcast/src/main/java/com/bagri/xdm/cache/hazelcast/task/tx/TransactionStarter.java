package com.bagri.xdm.cache.hazelcast.task.tx;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.XDMTransactionManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class TransactionStarter extends com.bagri.xdm.client.hazelcast.task.tx.TransactionStarter {

	private transient XDMTransactionManagement txMgr;
    
    @Autowired
	public void setTxManager(XDMTransactionManagement txMgr) {
		this.txMgr = txMgr;
	}
    
    @Override
	public String call() throws Exception {
		return txMgr.beginTransaction();
	}
	
}
