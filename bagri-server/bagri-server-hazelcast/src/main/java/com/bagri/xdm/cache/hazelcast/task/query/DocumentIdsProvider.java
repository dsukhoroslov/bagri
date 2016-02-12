package com.bagri.xdm.cache.hazelcast.task.query;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentIdsProvider extends com.bagri.xdm.client.hazelcast.task.query.DocumentIdsProvider {

	private transient XDMQueryManagement queryMgr;
    
    @Autowired
	public void setQueryManager(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
		//logger.debug("setQueryManager; got QueryManager: {}", queryMgr); 
	}
    
    @Autowired
	public void setRepository(RepositoryImpl repo) {
		this.repo = repo;
	}

    @Override
	public Collection<XDMDocumentId> call() throws Exception {
    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.read);
       	return queryMgr.getDocumentIds(query, params, context);
	}

}
