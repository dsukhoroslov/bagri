package com.bagri.server.hazelcast.task.doc;

import static com.bagri.core.Constants.pn_client_txLevel;
import static com.bagri.core.Constants.pv_client_txLevel_skip;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.ResultCollection;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.server.api.TransactionManagement;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.AccessManagementImpl;

public class DocumentsCreator extends com.bagri.client.hazelcast.task.doc.DocumentsCreator {

	private transient DocumentManagement docMgr;
	private transient TransactionManagement txMgr;
    
	@Autowired
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
		this.txMgr = (TransactionManagement) repo.getTxManagement();
	}

	@Override
	public ResultCollection<DocumentAccessor> call() throws Exception {
    	
    	//((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
    	//checkPermission(Permission.Value.modify);
    	((AccessManagementImpl) repo.getAccessManagement()).checkPermission(clientId, Permission.Value.modify);
    	
    	String txLevel = props.getProperty(pn_client_txLevel);
    	if (pv_client_txLevel_skip.equals(txLevel)) {
    		// bypass tx stack completely!
    		return (ResultCollection<DocumentAccessor>) docMgr.storeDocuments(documents, props);
    	}
    	
    	// do we have default isolation level?
    	TransactionIsolation tiLevel = TransactionIsolation.readCommited; 
    	if (txLevel != null) {
    		tiLevel = TransactionIsolation.valueOf(txLevel);
    	}
    	
    	return txMgr.callInTransaction(txId, false, tiLevel, new Callable<ResultCollection<DocumentAccessor>>() {
    		
	    	public ResultCollection<DocumentAccessor> call() throws Exception {
	    		return (ResultCollection<DocumentAccessor>) docMgr.storeDocuments(documents, props);
	    	}
    	});
    }
	


}
