package com.bagri.server.hazelcast.task.doc;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentContentProvider extends com.bagri.client.hazelcast.task.doc.DocumentContentProvider {

	private transient DocumentManagement docMgr;
    
    @Autowired
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
	}

    @Override
	//public String call() throws Exception {
	public Object process(Entry<DocumentKey, Document> entry) {

    	try {
	    	((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
	    	checkPermission(Permission.Value.read);
	    	
			return docMgr.getDocumentAsString(uri, props);
    	} catch (BagriException ex) {
    		return ex;
    	}
	}
}
