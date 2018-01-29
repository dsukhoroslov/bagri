package com.bagri.server.hazelcast.task.query;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.system.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class QueryUrisProvider extends com.bagri.client.hazelcast.task.query.QueryUrisProvider {

	private transient QueryManagement queryMgr;
    
    @Autowired
    @Override
	public void setRepository(SchemaRepository repo) {
    	super.setRepository(repo);
		this.queryMgr = repo.getQueryManagement();
	}

    @Override
	public ResultCursor<String> call() throws Exception {
    	//((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.Value.read);
       	return queryMgr.getDocumentUris(query, params, context);
	}

}
