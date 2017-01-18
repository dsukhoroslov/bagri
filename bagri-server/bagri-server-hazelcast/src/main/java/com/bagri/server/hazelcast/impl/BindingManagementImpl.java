package com.bagri.server.hazelcast.impl;

import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;

public class BindingManagementImpl extends com.bagri.client.hazelcast.impl.BindingManagementImpl {
	
    public void setRepository(SchemaRepositoryImpl repo) {
    	this.repo = repo;
    }	

}
