package com.bagri.xdm.cache.hazelcast.task.auth;


import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;

public class UserAuthenticator extends com.bagri.xdm.client.hazelcast.task.auth.UserAuthenticator {

	private transient RepositoryImpl repo;
    
    @Autowired
	public void setRepository(RepositoryImpl repo) {
		this.repo = repo;
	}

    @Override
	public Boolean call() throws Exception {

    	return repo.getAccessManagement().authenticate(userName, new String(password));
    	
	}


}
