package com.bagri.xdm.cache.hazelcast.management;

import java.util.Collection;

import javax.management.openmbean.CompositeData;

public interface SchemaManagerMBean {

	String getSchemaName();
	
	CompositeData getAllProperties();
	String getProperty(String name);
	void setProperty(String name, String value);
	void removeProperty(String name);
	
	String[] getRegisteredTypes();
	
	int registerSchema(String schemaFile);
	int registerSchemas(String schemasCatalog);
	
	int registerDocument(String docFile);
	int registerDocuments(String docCatalog);
}
