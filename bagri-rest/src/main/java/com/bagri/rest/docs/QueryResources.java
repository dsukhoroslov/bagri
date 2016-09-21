package com.bagri.rest.docs;

import static com.bagri.common.util.PropUtils.propsFromString;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.SchemaRepository;

@Singleton
//@Path("/query")
public class QueryResources {

    private static final transient Logger logger = LoggerFactory.getLogger(QueryResources.class);

    @Inject
    private RepositoryProvider repos;
    
    private QueryManagement getQueryManager(String schemaName) {
    	if (repos == null) {
    		logger.warn("getQueryManager; resource not initialized: RepositoryProvider is null");
    		return null;
    	}
    	SchemaRepository repo = repos.getRepository(schemaName);
    	if (repo == null) {
    		logger.warn("getQueryManager; Repository is not active for schema {}", schemaName);
    		return null;
    	}
    	return repo.getQueryManagement();
    }
    
    @POST
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON) 
	public Response postQuery(String query, String params, String properties) {
		String schema = "default";
		QueryManagement queryMgr = getQueryManager(schema);
    	try {
    		queryMgr.executeQuery(query, convertParams(params), propsFromString(properties));
            return null; //Response.ok(dr).build();
    	} catch (Exception ex) {
    		logger.error("postQuery.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    @POST
    @Path("/query/uris")
    @Produces(MediaType.TEXT_PLAIN) 
	public Response getURIs(String query, String params, String properties) {
		String schema = "default";
		QueryManagement queryMgr = getQueryManager(schema);
    	try {
    		Collection<String> uris = queryMgr.getDocumentUris(query, convertParams(params), propsFromString(properties));
            return Response.ok(uris).build();
    	} catch (Exception ex) {
    		logger.error("postQuery.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    private Map<String, Object> convertParams(String params) {
    	return null;
    }
	
}
