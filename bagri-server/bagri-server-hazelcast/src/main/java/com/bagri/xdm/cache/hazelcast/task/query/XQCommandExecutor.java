package com.bagri.xdm.cache.hazelcast.task.query;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class XQCommandExecutor extends com.bagri.xdm.client.hazelcast.task.query.XQCommandExecutor {

	private static final transient Logger logger = LoggerFactory.getLogger(XQCommandExecutor.class);
    
	private transient XDMQueryManagement queryMgr;
	private transient XDMTransactionManagement txMgr;
    
    @Autowired
    @Qualifier("queryProxy") //queryProxy //queryManager
	public void setQueryManager(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
		logger.debug("setQueryManager; got QueryManager: {}", queryMgr); 
	}
    
    @Autowired
	public void setTxManager(XDMTransactionManagement txMgr) {
		this.txMgr = txMgr;
		logger.debug("setTxManager; got TxManager: {}", txMgr); 
	}

    @Override
	public Object call() throws Exception {
		
    	long txId = XDMTransactionManagement.TX_NO;
    	String id = context.getProperty("txId");
		if (id != null) {
			txId = Long.parseLong(id);
		}
    	return txMgr.callInTransaction(txId, new Callable<Object>() {
    		
	    	public Object call() {
				if (isQuery) {
					return queryMgr.executeXQuery(command, bindings, context);
				} else {
			        return queryMgr.executeXCommand(command, bindings, context);
				}
	    	}
    	});
    }

}
