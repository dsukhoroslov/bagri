package com.bagri.xdm.cache.hazelcast.impl;

import com.hazelcast.core.HazelcastInstance;

public class HealthManagementImpl extends com.bagri.xdm.client.hazelcast.impl.HealthManagementImpl {

	public HealthManagementImpl() {
		super();
	}
	
	public HealthManagementImpl(HazelcastInstance hzInstance) {
		super(hzInstance);
	}

}
