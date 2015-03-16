package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCreator extends com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator {

	private static final transient Logger logger = LoggerFactory.getLogger(DocumentCreator.class);
    
	private transient XDMDocumentManagement docMgr;
	private transient XDMTransactionManagement txMgr;
    
    @Autowired
    @Qualifier("docProxy")
	public void setDocManager(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
		logger.debug("setDocManager; got DocumentManager: {}", docMgr); 
	}
    
    @Autowired
	public void setTxManager(XDMTransactionManagement txMgr) {
		this.txMgr = txMgr;
		logger.debug("setTxManager; got TxManager: {}", txMgr); 
	}

    @Override
	public XDMDocument call() throws Exception {

    	long txId = XDMTransactionManagement.TX_NO;
    	return txMgr.callInTransaction(txId, new Callable<XDMDocument>() {
    		
	    	public XDMDocument call() {
	    		return docMgr.storeDocumentFromString(docId, uri, xml);
	    	}
    	});
	}
	
}
