package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.cache.api.XDMRepository;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class CollectionDocumentsProvider extends com.bagri.xdm.client.hazelcast.task.doc.CollectionDocumentsProvider {

	private transient XDMDocumentManagement docMgr;
	
    @Autowired
	public void setRepository(XDMRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
	}

    @Override
	public Collection<XDMDocumentId> call() throws Exception {
    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.read);
   		return docMgr.getCollectionDocumentIds(collection);
	}
	
}
