package com.bagri.rest;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.system.Module;
import com.bagri.xdm.system.Schema;

public class LocalRepositoryProvider implements RepositoryProvider {

	private Map<String, SchemaRepository> repos = new ConcurrentHashMap<>();
	
	private Map<String, Schema> schemas;
	
	public LocalRepositoryProvider() {
		initSchemas();
	}
	
	private void initSchemas() {
		schemas = new HashMap<>();
		schemas.put("default", new Schema(1, new Date(), "admin", "default", "default schema for test and demo purpose", true, null));
		schemas.put("TPoX", new Schema(1, new Date(), "admin", "TPoX", "TPoX: schema for TPoX-related tests", false, null));
		schemas.put("XMark", new Schema(1, new Date(), "admin", "XMark", "XMark benchmark schema", false, null));
	}

	@Override
	public Module getModule(String moduleName) {
		return null;
	}
	
	@Override
	public Collection<String> getSchemaNames() {
		//return repos.keySet();
		return schemas.keySet();
	}
	
	@Override
	public Schema getSchema(String name) {
		//
		return schemas.get(name);
	}
	
	@Override
	public Collection<Schema> getSchemas() {
		//
		return schemas.values();
	}
	
	@Override
	public SchemaRepository getRepository(String clientId) {
		return null;
	}
	
	//public boolean isRepositoryActive(String schemaName) {
	//	return false;
	//}
	
	@Override
	public SchemaRepository connect(String schemaName, String userName, String password) {
		return null;
	}

	@Override
	public void disconnect(String clientId) {
		//
	}

}
