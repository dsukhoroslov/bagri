package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.cache.api.TransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.system.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentRemover extends com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover {

	private transient DocumentManagement docMgr;
	private transient TransactionManagement txMgr;
    
    @Autowired
	public void setRepository(RepositoryImpl repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
		this.txMgr = (TransactionManagement) repo.getTxManagement();
	}

    @Override
	public Document call() throws Exception {

    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.Value.modify);
    	
    	txMgr.callInTransaction(txId, false, new Callable<Void>() {
    		
	    	public Void call() throws Exception {
	    		docMgr.removeDocument(uri);
	    		return null;
	    	}
    	});
    	return null;
	}


}
