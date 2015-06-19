package com.bagri.xdm.cache.hazelcast.task.query;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentUrisProvider extends com.bagri.xdm.client.hazelcast.task.query.DocumentUrisProvider {

	private static final transient Logger logger = LoggerFactory.getLogger(DocumentUrisProvider.class);
	    
	private transient XDMQueryManagement queryMgr;
	//private transient XDMTransactionManagement txMgr;
    
    @Autowired
    @Qualifier("queryProxy")
	public void setQueryManager(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
		logger.debug("setQueryManager; got QueryManager: {}", queryMgr); 
	}
	    
    //@Autowired
	//public void setTxManager(XDMTransactionManagement txMgr) {
	//	this.txMgr = txMgr;
	//	logger.debug("setTxManager; got TxManager: {}", txMgr); 
	//}

	@Override
	public Collection<String> call() throws Exception {

    	//return txMgr.callInTransaction(txId, true, new Callable<Collection<String>>() {
    		
	    //	public Collection<String> call() {
	        	return queryMgr.getDocumentURIs(exp);
	    //	}
    	//});
	}

}
