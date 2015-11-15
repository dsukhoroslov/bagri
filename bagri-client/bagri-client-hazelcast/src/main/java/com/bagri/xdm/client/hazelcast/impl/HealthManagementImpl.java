package com.bagri.xdm.client.hazelcast.impl;

import com.bagri.xdm.api.XDMHealthManagement;
import com.hazelcast.core.HazelcastInstance;

public class HealthManagementImpl implements XDMHealthManagement {

	private HazelcastInstance hzInstance;
	
	public HealthManagementImpl(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
	}
	
	@Override
	public boolean isClusterSafe() {
		// TODO: implement it properly
		return getClusterSize() > 0; 
	}

	@Override
	public int getClusterSize() {
		return hzInstance.getCluster().getMembers().size();
	}

}
