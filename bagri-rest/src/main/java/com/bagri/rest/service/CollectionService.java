package com.bagri.rest.service;

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.SchemaRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * JAX-RS collections resource, contains methods for document collections management. Accessible on /clns path. 
 * 
 * @author Denis Sukhoroslov
 *
 */
@Path("/clns")
@Api(value = "collections")
public class CollectionService extends RestService {

    private DocumentManagement getDocManager() {
    	SchemaRepository repo = getRepository();
    	if (repo != null) {
        	return repo.getDocumentManagement();
    	}
		return null;
    }
    
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "getCollections: return Collection names registered in the current schema")
    public Collection<String> getCollections() {
		DocumentManagement docMgr = getDocManager();
		try {
			return docMgr.getCollections();
    	} catch (Exception ex) {
    		logger.error("getCollections.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    	}
	}
	
    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON) 
	@ApiOperation(value = "getCollectionDocuments: return Document uris belonging to the specified Collection")
    public Collection<String> getCollectionDocuments(@PathParam("name") String name) {
		DocumentManagement docMgr = getDocManager();
		try {
			return docMgr.getCollectionDocumentUris(name);
    	} catch (Exception ex) {
    		logger.error("getCollectionDocuments.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    	}
    }    
    
    @DELETE
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON) 
	@ApiOperation(value = "deleteCollectionDocuments: delete all Documents belonging to the specified Collection")
    public int deleteCollectionDocuments(@PathParam("name") String name) {
		DocumentManagement docMgr = getDocManager();
		try {
			return docMgr.removeCollectionDocuments(name);
    	} catch (Exception ex) {
    		logger.error("deleteCollectionDocuments.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    	}
    }    
    
}

