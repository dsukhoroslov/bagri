package com.bagri.xdm.cache.hazelcast.management;

import javax.management.openmbean.CompositeData;

public interface SchemaManagementMBean {

	CompositeData getDefaultProperties();
	void setDefaultProperty(String name, String value);

	String[] getSchemaNames();
	boolean activateSchema(String schemaName, boolean activate);
	boolean createSchema(String schemaName, String description, String properties);
	boolean destroySchema(String schemaName);
	
}
