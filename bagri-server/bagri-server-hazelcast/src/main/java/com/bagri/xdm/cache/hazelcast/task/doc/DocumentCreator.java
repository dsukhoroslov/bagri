package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.cache.api.XDMRepository;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCreator extends com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator {

	private transient XDMDocumentManagement docMgr;
	private transient XDMTransactionManagement txMgr;
    
    @Autowired
	public void setRepository(XDMRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
		this.txMgr = (XDMTransactionManagement) repo.getTxManagement();
	}

    @Override
	public XDMDocument call() throws Exception {

    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.modify);
    	
    	return txMgr.callInTransaction(txId, false, new Callable<XDMDocument>() {
    		
	    	public XDMDocument call() throws Exception {
	    		return docMgr.storeDocumentFromString(docId, content, props);
	    	}
    	});
	}
	
}
