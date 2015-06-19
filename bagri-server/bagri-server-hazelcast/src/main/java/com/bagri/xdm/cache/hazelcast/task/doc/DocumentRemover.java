package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.cache.api.XDMRepository;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentRemover extends com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover {

    private static final transient Logger logger = LoggerFactory.getLogger(DocumentRemover.class);
    
	private transient XDMRepository repo;
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

    @Autowired
	public void setRepository(XDMRepository repo) {
		this.repo = repo;
		logger.debug("setRepository; got Repo: {}", repo); 
	}

    @Override
	public XDMDocument call() throws Exception {
    	
    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	txMgr.callInTransaction(txId, false, new Callable<Void>() {
    		
	    	public Void call() {
	    		docMgr.removeDocument(docId);
	    		return null;
	    	}
    	});
    	return null;
	}


}
