package com.bagri.xdm.cache.hazelcast.task.query;

import static com.bagri.xdm.common.XDMConstants.pn_client_txId;
import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.app_context;
import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.getContext;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.context.ApplicationContext;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
//import com.hazelcast.spring.context.SpringAware;

//@SpringAware
public class QueryExecutor extends com.bagri.xdm.client.hazelcast.task.query.QueryExecutor {

	//private static final transient Logger logger = LoggerFactory.getLogger(XQCommandExecutor.class);
    
	//private transient XDMQueryManagement queryMgr;
	//private transient XDMTransactionManagement txMgr;
    
    //@Autowired
    //@Qualifier("queryProxy") //queryProxy //queryManager
	//public void setQueryManager(XDMQueryManagement queryMgr) {
	//	this.queryMgr = queryMgr;
		//logger.trace("setQueryManager; got QueryManager: {}", queryMgr); 
	//}
    
    //@Autowired
	//public void setTxManager(XDMTransactionManagement txMgr) {
	//	this.txMgr = txMgr;
		//logger.trace("setTxManager; got TxManager: {}", txMgr); 
	//}

    @Override
	public ResultCursor call() throws Exception {
    	
    	ApplicationContext ctx = (ApplicationContext) getContext(schemaName, app_context);
    	final XDMQueryManagement queryMgr = ctx.getBean("queryProxy", XDMQueryManagement.class);
    	final XDMTransactionManagement txMgr = ctx.getBean(XDMTransactionManagement.class);
		
    	long txId = XDMTransactionManagement.TX_NO;
    	String id = context.getProperty(pn_client_txId);
    	if (id == null || "0".equals(id)) {
    		if (queryMgr.isReadOnlyQuery(query)) {
   				return (ResultCursor) queryMgr.executeQuery(query, bindings, context);
    		}
		} else {
			txId = Long.parseLong(id);
		}
		
    	return txMgr.callInTransaction(txId, false, new Callable<ResultCursor>() {
    		
	    	public ResultCursor call() throws XDMException {
				return (ResultCursor) queryMgr.executeQuery(query, bindings, context);
	    	}
    	});
    }

}
