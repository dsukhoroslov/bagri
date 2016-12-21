package com.bagri.rest.service;

import static com.bagri.rest.RestConstants.bg_cookie;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.SchemaRepository;
import com.bagri.rest.RepositoryProvider;

public abstract class RestService {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	@CookieParam(bg_cookie) 
	protected Cookie bgAuth;
	
    @Inject
    protected RepositoryProvider repos;
	
    protected String getClientId() {
    	return bgAuth.getValue();
    }
    
    protected SchemaRepository getRepository() {
    	if (repos == null) {
    		logger.warn("getRepository; service is not yet initialized: RepositoryProvider is null");
    		return null;
    	}
    	String clientId = getClientId();
    	SchemaRepository repo = repos.getRepository(clientId);
    	if (repo == null) {
    		logger.warn("getRepository; Repository is not active for client {}", clientId);
    	}
    	return repo;
    }
    
}
