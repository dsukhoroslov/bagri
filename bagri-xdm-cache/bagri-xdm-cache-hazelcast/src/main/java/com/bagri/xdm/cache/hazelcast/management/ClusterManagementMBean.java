package com.bagri.xdm.cache.hazelcast.management;

public interface ClusterManagementMBean {
	
	String[] getNodes();
	boolean addNode(String address, String nodeId, String options);
	boolean deleteNode(String address, String nodeId);
	
}
