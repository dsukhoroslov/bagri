package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.Properties;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.cache.api.XDMClientManagement;
import com.bagri.xdm.cache.api.XDMRepository;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCreator extends com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator {

	private static final transient Logger logger = LoggerFactory.getLogger(DocumentCreator.class);
	
	private transient RepositoryImpl repo;
	private transient XDMDocumentManagement docMgr;
	private transient XDMTransactionManagement txMgr;
    
    @Autowired
    @Qualifier("docProxy")
	public void setDocManager(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
	}
    
    @Autowired
	public void setTxManager(XDMTransactionManagement txMgr) {
		this.txMgr = txMgr;
	}

    @Autowired
	public void setRepository(RepositoryImpl repo) {
		this.repo = repo;
	}

    @Override
	public XDMDocument call() throws Exception {

    	XDMClientManagement clientMgr = repo.getClientManagement();
    	String user = clientMgr.getCurrentUser();
    	//repo.
    	
    	repo.getXQProcessor(clientId);
    	
    	return txMgr.callInTransaction(txId, false, new Callable<XDMDocument>() {
    		
	    	public XDMDocument call() throws Exception {
	    		return docMgr.storeDocumentFromString(docId, xml, props);
	    	}
    	});
	}
	
}
