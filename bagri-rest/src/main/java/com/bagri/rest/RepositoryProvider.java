package com.bagri.rest;

import java.util.Collection;

import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.system.Schema;

public interface RepositoryProvider {
	
	Collection<String> getSchemaNames();
	Schema getSchema(String name);
	Collection<Schema> getSchemas();
	SchemaRepository getRepository(String schemaName);
	boolean isRepositoryActive(String schemaName);
	
	// connect to repo -> credentials?
	// disconnect from repo..
}

