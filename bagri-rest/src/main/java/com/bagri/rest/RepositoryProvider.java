package com.bagri.rest;

import java.util.Collection;

import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.system.Module;
import com.bagri.xdm.system.Schema;

public interface RepositoryProvider {
	
	Module getModule(String moduleName);
	Collection<String> getSchemaNames();
	Schema getSchema(String name);
	Collection<Schema> getSchemas();
	SchemaRepository getRepository(String clientId);
	//boolean isRepositoryActive(String schemaName);
	SchemaRepository connect(String schemaName, String userName, String password);
	void disconnect(String clientId);
	
}

