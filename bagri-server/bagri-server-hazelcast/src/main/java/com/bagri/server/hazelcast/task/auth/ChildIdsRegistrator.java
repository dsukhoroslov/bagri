package com.bagri.server.hazelcast.task.auth;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.ClientManagementImpl;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class ChildIdsRegistrator extends com.bagri.client.hazelcast.task.auth.ChildIdsRegistrator {

    @Autowired
    @Override
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
	}

    @Override
	public Void call() throws Exception {
    	SchemaRepositoryImpl sri = (SchemaRepositoryImpl) repo;
    	ClientManagementImpl cmi = (ClientManagementImpl) sri.getClientManagement();
    	String userName = cmi.getClientUser(clientId);
    	for (String childId: childIds) {
    		cmi.addClient(childId, userName);
    		sri.getXQProcessor(childId);
    	}
    	sri.setClientId(clientId);
    	return null;
    }

}
