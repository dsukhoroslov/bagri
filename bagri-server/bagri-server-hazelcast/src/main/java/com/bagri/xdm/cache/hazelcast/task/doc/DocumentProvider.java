package com.bagri.xdm.cache.hazelcast.task.doc;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.cache.api.XDMRepository;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.system.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentProvider extends com.bagri.xdm.client.hazelcast.task.doc.DocumentProvider {

	private transient XDMDocumentManagement docMgr;
    
    @Autowired
	public void setRepository(XDMRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
	}

    @Override
	public Document call() throws Exception {
    	
    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.Value.read);
    	
		return docMgr.getDocument(uri);
	}


}
