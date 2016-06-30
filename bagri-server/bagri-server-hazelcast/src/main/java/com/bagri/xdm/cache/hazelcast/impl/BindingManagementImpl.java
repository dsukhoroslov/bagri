package com.bagri.xdm.cache.hazelcast.impl;

import com.bagri.xdm.cache.hazelcast.impl.SchemaRepositoryImpl;

public class BindingManagementImpl extends com.bagri.xdm.client.hazelcast.impl.BindingManagementImpl {
	
    public void setRepository(SchemaRepositoryImpl repo) {
    	this.repo = repo;
    }	

}
