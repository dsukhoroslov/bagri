package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.cache.api.SchemaRepository;
import com.bagri.xdm.cache.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.xdm.system.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentUrisProvider  extends com.bagri.xdm.client.hazelcast.task.doc.DocumentUrisProvider {

	private transient DocumentManagement docMgr;
    
    @Autowired
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
	}

    @Override
	public Collection<String> call() throws Exception {
    	
    	// not sure we have to check it at all..
    	((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.Value.read);
    	
		return docMgr.getDocumentUris(pattern);
	}



}
