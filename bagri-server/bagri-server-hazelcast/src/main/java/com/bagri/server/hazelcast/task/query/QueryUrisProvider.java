package com.bagri.server.hazelcast.task.query;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.QueryManagement;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class QueryUrisProvider extends com.bagri.client.hazelcast.task.query.QueryUrisProvider {

	private transient QueryManagement queryMgr;
    
    @Autowired
	public void setQueryManager(QueryManagement queryMgr) {
		this.queryMgr = queryMgr;
		//logger.debug("setQueryManager; got QueryManager: {}", queryMgr); 
	}
    
    @Autowired
	public void setRepository(SchemaRepositoryImpl repo) {
		this.repo = repo;
	}

    @Override
	public Collection<String> call() throws Exception {
    	((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.Value.read);
       	return queryMgr.getDocumentUris(query, params, context);
	}

}
