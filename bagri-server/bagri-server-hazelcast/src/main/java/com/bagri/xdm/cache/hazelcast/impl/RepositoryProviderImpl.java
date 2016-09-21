package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.cache.api.SchemaRepository.bean_id;
import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.getHazelcastClientByName;

import java.util.Arrays;
import java.util.Collection;

import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.cache.hazelcast.management.SchemaManagement;
import com.bagri.xdm.system.Schema;
import com.hazelcast.core.HazelcastInstance;

public class RepositoryProviderImpl implements RepositoryProvider {

	private SchemaManagement schemaService;
	
    public SchemaManagement getSchemaManagement() {
    	return schemaService;
    }
    
    public void setSchemaManagement(SchemaManagement schemaService) {
    	this.schemaService = schemaService;
    }
	
	
	@Override
	public Collection<String> getSchemaNames() {
		return Arrays.asList(schemaService.getSchemaNames());
	}

	@Override
	public Schema getSchema(String name) {
		return schemaService.getSchema(name);
	}

	@Override
	public Collection<Schema> getSchemas() {
		return schemaService.getEntities();
	}

	@Override
	public SchemaRepository getRepository(String schemaName) {
		HazelcastInstance schemaInstance = getHazelcastClientByName(schemaName);
		if (schemaInstance != null) {
			return (SchemaRepository) schemaInstance.getUserContext().get(bean_id);
		}
		return null;
	}

	@Override
	public boolean isRepositoryActive(String schemaName) {
		return getRepository(schemaName) != null;
	}

}
