package com.bagri.xdm.cache.hazelcast.task.doc;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.cache.api.SchemaRepository;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.system.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentContentProvider extends com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider {

	private transient DocumentManagement docMgr;
    
    @Autowired
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
	}

    @Override
	public String call() throws Exception {
    	
    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.Value.read);
    	
		return docMgr.getDocumentAsString(uri);
	}
}
