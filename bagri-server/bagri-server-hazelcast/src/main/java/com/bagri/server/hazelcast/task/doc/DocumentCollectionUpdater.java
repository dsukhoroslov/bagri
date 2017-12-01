package com.bagri.server.hazelcast.task.doc;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.AccessManagementImpl;
import com.bagri.server.hazelcast.impl.DocumentManagementImpl;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCollectionUpdater extends com.bagri.client.hazelcast.task.doc.DocumentCollectionUpdater {

	private Document doc;
	private transient DocumentManagement docMgr;
    
    @Autowired
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
	}

	@Override
	public EntryBackupProcessor<DocumentKey, Document> getBackupProcessor() {
		return new DocumentBackupProcessor(doc);
	}

	@Override
	public Object process(Entry<DocumentKey, Document> entry) {
    	try {
			((AccessManagementImpl) repo.getAccessManagement()).checkPermission(clientId, Permission.Value.modify);
			doc = entry.getValue();
    		return ((DocumentManagementImpl) docMgr).updateDocumentCollections(add, entry, collections);
		} catch (BagriException ex) {
			throw new RuntimeException(ex);
		}
	}
   
}
