package com.bagri.server.hazelcast.task.doc;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.server.api.TransactionManagement;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentRemover extends com.bagri.client.hazelcast.task.doc.DocumentRemover {

	private transient DocumentManagement docMgr;
	private transient TransactionManagement txMgr;
    
    @Autowired
    @Override
	public void setRepository(SchemaRepository repo) {
		super.setRepository(repo);
		this.docMgr = repo.getDocumentManagement();
		this.txMgr = (TransactionManagement) repo.getTxManagement();
	}

    @Override
	public DocumentAccessor call() throws Exception {

		checkPermission(Permission.Value.modify);
    	
    	TransactionIsolation tiLevel = ((SchemaRepositoryImpl) repo).getTransactionLevel(context); 
    	if (tiLevel == null) {
    		return docMgr.removeDocument(uri, context);
    	}
    	
    	txMgr.callInTransaction(txId, false, tiLevel, new Callable<DocumentAccessor>() {
    		
	    	public DocumentAccessor call() throws Exception {
	    		return docMgr.removeDocument(uri, context);
	    	}
    	});
    	return null;
	}


}
