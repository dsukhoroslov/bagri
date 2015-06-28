package com.bagri.xdm.cache.hazelcast.impl;

import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;

public class BindingManagementImpl extends com.bagri.xdm.client.hazelcast.impl.BindingManagementImpl {
	
    public void setRepository(RepositoryImpl repo) {
    	this.repo = repo;
    }	

}
