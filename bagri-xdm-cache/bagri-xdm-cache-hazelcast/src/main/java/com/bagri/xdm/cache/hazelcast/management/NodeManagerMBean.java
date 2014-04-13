package com.bagri.xdm.cache.hazelcast.management;

import javax.management.openmbean.CompositeData;

public interface NodeManagerMBean {
	
	String getNodeId();
	String getAddress();
	CompositeData getOptions();
	String getOption(String name);
	void setOption(String name, String value);
	void removeOption(String name);
	
	String[] getDeployedSchemas();

}
