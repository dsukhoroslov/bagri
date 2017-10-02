package com.bagri.server.hazelcast.task.doc;

import static com.bagri.core.Constants.pn_client_txLevel;
import static com.bagri.core.Constants.pv_client_txLevel_skip;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.server.api.TransactionManagement;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.AccessManagementImpl;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentRemover extends com.bagri.client.hazelcast.task.doc.DocumentRemover {

	private transient DocumentManagement docMgr;
	private transient TransactionManagement txMgr;
    
    @Autowired
	public void setRepository(SchemaRepositoryImpl repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
		this.txMgr = (TransactionManagement) repo.getTxManagement();
	}

    @Override
	public DocumentAccessor call() throws Exception {

    	((AccessManagementImpl) repo.getAccessManagement()).checkPermission(clientId, Permission.Value.modify);
    	
    	String txLevel = context.getProperty(pn_client_txLevel);
    	if (pv_client_txLevel_skip.equals(txLevel)) {
    		// bypass tx stack completely..?
    		return docMgr.removeDocument(uri, context);
    	}
    	
    	// do we have default isolation level?
    	TransactionIsolation tiLevel = TransactionIsolation.readCommited; 
    	if (txLevel != null) {
    		tiLevel = TransactionIsolation.valueOf(txLevel);
    	}
    	
    	txMgr.callInTransaction(txId, false, tiLevel, new Callable<DocumentAccessor>() {
    		
	    	public DocumentAccessor call() throws Exception {
	    		return docMgr.removeDocument(uri, context);
	    	}
    	});
    	return null;
	}


}
