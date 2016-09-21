package com.bagri.rest.docs;

import static com.bagri.common.util.PropUtils.propsFromString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.domain.Document;

@Singleton
@Path("/docs")
public class DocumentResources  {
	
    private static final transient Logger logger = LoggerFactory.getLogger(DocumentResources.class);

    @Inject
    private RepositoryProvider repos;
    
    private DocumentManagement getDocManager(String schemaName) {
    	if (repos == null) {
    		logger.warn("getDocManager; resource not initialized: RepositoryProvider is null");
    		return null;
    	}
    	SchemaRepository repo = repos.getRepository(schemaName);
    	if (repo == null) {
    		logger.warn("getDocManager; Repository is not active for schema {}", schemaName);
    		return null;
    	}
    	return repo.getDocumentManagement();
    }
    
	@GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocuments(@QueryParam("query") String query, 
    		@DefaultValue("1") @QueryParam("page") int page, @DefaultValue("100") @QueryParam("size") int size) {
		// add paginaton, pattern
		logger.trace("getDocuments.enter; query: {}, page: {}, size: {}", query, page, size);
		String schema = "default";
		DocumentManagement docMgr = getDocManager(schema);
    	try {
            Collection<String> uris = docMgr.getDocumentUris(query);
            uris = new ArrayList<>(uris);
            Collections.sort((List) uris);
            DocumentResource[] docs = new DocumentResource[size];
            long now = new java.util.Date().getTime();
            int start = page == 0 ? 0 : (page - 1) * size;
            if (start >= uris.size()) {
            	start = uris.size() - size;
            } 
            for (int i = 0; i < size && start + i < uris.size(); i++) {
            	String uri = ((List<String>) uris).get(i);
            	docs[i] = new DocumentResource(uri, now, "owner", "utf-8", 1000);
            }
            return Response.ok(docs).build();
    	} catch (Exception ex) {
    		logger.error("getDocuments.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    @GET
    @Path("/{uri}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) 
    public Response getDocumentContent(@PathParam("uri") String uri) {
		String schema = "default";
		DocumentManagement docMgr = getDocManager(schema);
    	try {
             String content = docMgr.getDocumentAsString(uri, null);
             return Response.ok(content).build();
    	} catch (Exception ex) {
    		logger.error("getDocumentContent.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }    

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON) 
	public Response postDocument(String uri, String content, String properties) {
		String schema = "default";
		DocumentManagement docMgr = getDocManager(schema);
    	try {
             Document doc = docMgr.storeDocumentFromString(uri, content, propsFromString(properties));
             DocumentResource dr = new DocumentResource(doc.getUri(), doc.getCreatedAt().getTime(), doc.getCreatedBy(), doc.getEncoding(), doc.getBytes());
             return Response.ok(dr).build();
    	} catch (Exception ex) {
    		logger.error("postDocument.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    @DELETE
    @Path("/{uri}")
    @Produces(MediaType.APPLICATION_JSON) 
	public Response deleteDocument(@PathParam("uri") String uri) {
		String schema = "default";
		DocumentManagement docMgr = getDocManager(schema);
    	try {
             docMgr.removeDocument(uri);
             return Response.ok(uri).build();
    	} catch (Exception ex) {
    		logger.error("deleteDocument.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    
}


