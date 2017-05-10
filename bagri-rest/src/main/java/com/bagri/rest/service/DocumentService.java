package com.bagri.rest.service;

import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.system.DataFormat.df_json;
import static com.bagri.core.system.DataFormat.df_xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Document;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * JAX-RS documents resource, contains methods for documents management. Accessible on /docs path. 
 * 
 * @author Denis Sukhoroslov
 *
 */
@Path("/docs")
@Api(value = "documents")
public class DocumentService  extends RestService {
	
    
    private DocumentManagement getDocManager() {
    	SchemaRepository repo = getRepository();
    	if (repo != null) {
        	return repo.getDocumentManagement();
    	}
		return null;
    }
    
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "getDocuments: return Document uris corresponding to the specified query")
    public Response getDocuments(@QueryParam("query") String query, 
    		@DefaultValue("1") @QueryParam("page") int page, @DefaultValue("100") @QueryParam("size") int size) {
		// add paginaton, pattern
		logger.trace("getDocuments.enter; query: {}, page: {}, size: {}", query, page, size);
		DocumentManagement docMgr = getDocManager();
    	try {
            Collection<String> uris = docMgr.getDocumentUris(query, null);
            uris = new ArrayList<>(uris);
            Collections.sort((List) uris);
            DocumentBean[] docs = new DocumentBean[size];
            long now = new java.util.Date().getTime();
            int start = page == 0 ? 0 : (page - 1) * size;
            if (start >= uris.size()) {
            	start = uris.size() - size;
            } 
            for (int i = 0; i < size && start + i < uris.size(); i++) {
            	String uri = ((List<String>) uris).get(i);
            	docs[i] = new DocumentBean(uri, now, "owner", "utf-8", 1000);
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
	@ApiOperation(value = "getDocumentContent: return content of the specified Document")
    public Response getDocumentContent(@PathParam("uri") String uri, @Context HttpHeaders hh) {
		DocumentManagement docMgr = getDocManager();
    	try {
    		Properties props = new Properties();
    		if (MediaType.APPLICATION_JSON_TYPE.equals(hh.getAcceptableMediaTypes().get(0))) {
    	    	props.setProperty(pn_document_data_format, df_json);
    		} else if (MediaType.APPLICATION_XML_TYPE.equals(hh.getAcceptableMediaTypes().get(0))) {
    	    	props.setProperty(pn_document_data_format, df_xml);
    		}
            String content = docMgr.getDocumentAsString(uri, props);
            return Response.ok(content).build();
    	} catch (Exception ex) {
    		logger.error("getDocumentContent.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }    

    @POST
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.APPLICATION_JSON) 
	@ApiOperation(value = "postDocument: creates a new Document or new version if Document with provided uri already exists")
	public Response postDocument(final DocumentParams params) {
		DocumentManagement docMgr = getDocManager();
    	try {
    		logger.trace("postDocument; got params: {}", params);
            Document doc = docMgr.storeDocumentFromString(params.uri, params.content, params.props);
     		logger.trace("postDocument; got document: {}", doc);
            DocumentBean dr = new DocumentBean(doc.getUri(), doc.getCreatedAt().getTime(), doc.getCreatedBy(), doc.getEncoding(), doc.getBytes());
            return Response.created(UriBuilder.fromPath("/docs/" + dr.uri).build()).entity(dr).build();
    	} catch (Exception ex) {
    		logger.error("postDocument.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    @DELETE
    @Path("/{uri}")
    @Produces(MediaType.APPLICATION_JSON) 
	@ApiOperation(value = "deleteDocument: deletes Document by its uri")
	public Response deleteDocument(@PathParam("uri") String uri) {
		DocumentManagement docMgr = getDocManager();
    	try {
            docMgr.removeDocument(uri);
            return Response.ok(uri).build();
    	} catch (Exception ex) {
    		logger.error("deleteDocument.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    @PUT
    @Path("/{uri}")
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.APPLICATION_JSON) 
	@ApiOperation(value = "changeDocumentCollections: adds/removes Document to/from the specified Collections")
	public Response changeDocumentCollections(@PathParam("uri") String uri, CollectionParams params) {
		DocumentManagement docMgr = getDocManager();
    	try {
    		int cnt = 0;
    		if (params.add) {
    			cnt = docMgr.addDocumentToCollections(uri, params.collections);
    		} else {
    			cnt = docMgr.removeDocumentFromCollections(uri, params.collections);
    		}
            return Response.ok(cnt).build();
    	} catch (Exception ex) {
    		logger.error("changeDocumentCollections.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
}


