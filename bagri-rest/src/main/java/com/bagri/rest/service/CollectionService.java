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

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.SchemaRepository;

@Path("/clns")
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

