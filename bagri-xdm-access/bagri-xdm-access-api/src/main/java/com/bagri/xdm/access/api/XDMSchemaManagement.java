package com.bagri.xdm.access.api;

import java.util.Collection;
import java.util.Properties;

import com.bagri.xdm.system.XDMSchema;

public interface XDMSchemaManagement {

	Collection<XDMSchema> getSchemas();
	XDMSchema addSchema(String schemaName, String description, Properties props);
	XDMSchema deleteSchema(String schemaName);
	//boolean initSchema(String schemaName, Properties props);
	//boolean denitSchema(String schemaName);
	XDMSchemaManagerBase getSchemaManager(String schemaName);
}
