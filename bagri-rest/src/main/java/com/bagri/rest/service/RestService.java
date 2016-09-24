package com.bagri.rest.service;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.SchemaRepository;

public abstract class RestService {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
    @Inject
    protected RepositoryProvider repos;
	
    protected String getCurrentSchema() {
    	// TODO: implement it properly
    	return "default";
    }
    
    protected SchemaRepository getRepository() {
    	if (repos == null) {
    		logger.warn("getRepository; service is not yet initialized: RepositoryProvider is null");
    		return null;
    	}
    	String schemaName = getCurrentSchema();
    	SchemaRepository repo = repos.getRepository(schemaName);
    	if (repo == null) {
    		logger.warn("getRepository; Repository is not active for schema {}", schemaName);
    	}
    	return repo;
    }
    
}
