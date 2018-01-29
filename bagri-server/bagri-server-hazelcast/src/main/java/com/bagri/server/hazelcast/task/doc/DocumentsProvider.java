package com.bagri.server.hazelcast.task.doc;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.system.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentsProvider extends com.bagri.client.hazelcast.task.doc.DocumentsProvider {

	private transient DocumentManagement docMgr;
    
    @Autowired
    @Override
	public void setRepository(SchemaRepository repo) {
		super.setRepository(repo);
		this.docMgr = repo.getDocumentManagement();
	}

    @Override
	public ResultCursor call() throws Exception {
    	
    	// not sure we have to check it at all..
		checkPermission(Permission.Value.read);
    	return docMgr.getDocuments(pattern, context);
	}


}
