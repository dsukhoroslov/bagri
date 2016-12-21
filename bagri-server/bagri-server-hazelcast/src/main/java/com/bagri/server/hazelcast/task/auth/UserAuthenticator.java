package com.bagri.server.hazelcast.task.auth;


import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;

public class UserAuthenticator extends com.bagri.client.hazelcast.task.auth.UserAuthenticator {

	private transient SchemaRepositoryImpl repo;
    
    @Autowired
	public void setRepository(SchemaRepositoryImpl repo) {
		this.repo = repo;
	}

    @Override
	public Boolean call() throws Exception {

    	return repo.getAccessManagement().authenticate(userName, new String(password));
    	
	}


}
