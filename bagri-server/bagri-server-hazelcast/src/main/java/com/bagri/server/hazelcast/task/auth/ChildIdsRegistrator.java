package com.bagri.server.hazelcast.task.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.ClientManagementImpl;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class ChildIdsRegistrator extends com.bagri.client.hazelcast.task.auth.ChildIdsRegistrator {

	private static final transient Logger logger = LoggerFactory.getLogger(ChildIdsRegistrator.class);

	@Autowired
    @Override
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
	}

    @Override
	public Void call() throws Exception {
    	SchemaRepositoryImpl xRepo = (SchemaRepositoryImpl) repo;
    	ClientManagementImpl cmi = (ClientManagementImpl) xRepo.getClientManagement();
    	String userName = cmi.getClientUser(clientId);
    	logger.trace("registering client: {}", clientId);
    	for (String childId: childIds) {
        	logger.trace("registering child: {}", childId);
    		cmi.addClient(childId, userName);
    		xRepo.getXQProcessor(childId);
    	}
    	xRepo.setClientId(clientId);
    	return null;
    }

}
