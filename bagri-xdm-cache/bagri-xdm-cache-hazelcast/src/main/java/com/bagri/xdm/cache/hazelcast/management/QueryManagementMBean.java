package com.bagri.xdm.cache.hazelcast.management;

public interface QueryManagementMBean {
	
	String getSchema();
	String runQuery(String query);

}
