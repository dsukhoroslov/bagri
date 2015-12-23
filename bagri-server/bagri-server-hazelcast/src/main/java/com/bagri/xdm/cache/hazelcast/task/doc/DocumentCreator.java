package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.XDMAccessManagement;
import com.bagri.xdm.cache.api.XDMClientManagement;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCreator extends com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator {

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
    	if (!((XDMAccessManagement) repo.getAccessManagement()).hasPermission(user, Permission.modify)) {
    		throw new XDMException("User " + user + " has no permission to create/update documents", XDMException.ecAccess);
    	}
    	
    	repo.getXQProcessor(clientId);
    	return txMgr.callInTransaction(txId, false, new Callable<XDMDocument>() {
    		
	    	public XDMDocument call() throws Exception {
	    		return docMgr.storeDocumentFromString(docId, xml, props);
	    	}
    	});
	}
	
}
