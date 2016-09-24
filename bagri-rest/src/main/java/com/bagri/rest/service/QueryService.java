package com.bagri.rest.service;

import java.util.Collection;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.SchemaRepository;

@Singleton
@Path("/query")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON) 
public class QueryService extends RestService {

    private QueryManagement getQueryManager() {
    	SchemaRepository repo = getRepository();
    	if (repo != null) {
        	return repo.getQueryManagement();
    	}
		return null;
    }
    
    @POST
	public Response postQuery(final QueryParams params) {
		QueryManagement queryMgr = getQueryManager();
    	try {
    		ResultCursor cursor = queryMgr.executeQuery(params.query, params.params, params.props);
            return null; //Response.ok(dr).build();
    	} catch (Exception ex) {
    		logger.error("postQuery.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    @POST
    @Path("/uris")
	public Response getURIs(final QueryParams params) {
		QueryManagement queryMgr = getQueryManager();
    	try {
    		logger.info("getURIs; got params: {}", params);
    		Collection<String> uris = queryMgr.getDocumentUris(params.query, params.params, params.props);
    		logger.info("getURIs; got URIs: {}", uris);
            return Response.ok(uris).build();
    	} catch (Exception ex) {
    		logger.error("postQuery.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
	
}
