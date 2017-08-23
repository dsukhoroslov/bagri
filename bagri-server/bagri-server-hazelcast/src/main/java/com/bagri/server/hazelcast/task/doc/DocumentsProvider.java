package com.bagri.server.hazelcast.task.doc;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.ResultCollection;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.AccessManagementImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentsProvider extends com.bagri.client.hazelcast.task.doc.DocumentsProvider {

	private transient DocumentManagement docMgr;
    
    @Autowired
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
	}

    @Override
	public ResultCollection call() throws Exception {
    	
    	// not sure we have to check it at all..
    	((AccessManagementImpl) repo.getAccessManagement()).checkPermission(clientId, Permission.Value.read);
    	return (ResultCollection) docMgr.getDocuments(pattern, props);
	}


}
