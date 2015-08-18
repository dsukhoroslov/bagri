package com.bagri.xdm.cache.hazelcast.task.query;

import static com.bagri.xdm.common.XDMConstants.pn_client_txId;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class XQCommandExecutor extends com.bagri.xdm.client.hazelcast.task.query.XQCommandExecutor {

	//private static final transient Logger logger = LoggerFactory.getLogger(XQCommandExecutor.class);
    
	private transient XDMQueryManagement queryMgr;
	private transient XDMTransactionManagement txMgr;
    
    @Autowired
    @Qualifier("queryProxy") //queryProxy //queryManager
	public void setQueryManager(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
		//logger.trace("setQueryManager; got QueryManager: {}", queryMgr); 
	}
    
    @Autowired
	public void setTxManager(XDMTransactionManagement txMgr) {
		this.txMgr = txMgr;
		//logger.trace("setTxManager; got TxManager: {}", txMgr); 
	}

    @Override
	public ResultCursor call() throws Exception {
		
    	long txId = XDMTransactionManagement.TX_NO;
    	String id = context.getProperty(pn_client_txId);
    	if (id == null || "0".equals(id)) {
    		if (queryMgr.isReadOnlyQuery(command)) {
    			if (isQuery) {
    				return (ResultCursor) queryMgr.executeXQuery(command, bindings, context);
    			} else {
    				return (ResultCursor) queryMgr.executeXCommand(command, bindings, context);
    			}
    		}
		} else {
			txId = Long.parseLong(id);
		}
		
    	return txMgr.callInTransaction(txId, false, new Callable<ResultCursor>() {
    		
	    	public ResultCursor call() throws XDMException {
				if (isQuery) {
					return (ResultCursor) queryMgr.executeXQuery(command, bindings, context);
				} else {
			        return (ResultCursor) queryMgr.executeXCommand(command, bindings, context);
				}
	    	}
    	});
    }

}
