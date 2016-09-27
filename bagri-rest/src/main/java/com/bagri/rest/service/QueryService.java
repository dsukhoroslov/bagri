package com.bagri.rest.service;

import java.io.IOException;
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

import org.glassfish.jersey.server.ChunkedOutput;

import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.SchemaRepository;

//@Singleton
@Path("/query")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON) 
public class QueryService extends RestService {
	
	public static final String splitter = "|:|";

    private QueryManagement getQueryManager() {
    	SchemaRepository repo = getRepository();
    	if (repo != null) {
        	return repo.getQueryManagement();
    	}
		return null;
    }
    
    @POST
	public ChunkedOutput<String> postQuery(final QueryParams params) {
		logger.debug("postQuery; got query: {}", params);
    	final ChunkedOutput<String> output = new ChunkedOutput<String>(String.class);
    	 
        new Thread() {
            public void run() {
        		QueryManagement queryMgr = getQueryManager();
                try {
            		ResultCursor cursor = queryMgr.executeQuery(params.query, params.params, params.props);
            		int cnt = 0;
                    while (cursor.next()) {
                    	if (cnt > 0) {
                            output.write(splitter);
                    	}
                    	String chunk = cursor.getItemAsString(null); 
                        logger.debug("postQuery; out: {}", chunk);
                        output.write(chunk);
                        cnt++;
                    }
                } catch (Exception ex) {
                	// XDMException, IOException. handle it somehow ?
            		logger.error("postQuery.error", ex);
            		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
                } finally {
                	try {
                		output.close();
                	} catch (IOException ex) {
                		//
                	}
                }
            }
        }.start();
 
        // the output will be probably returned even before
        // a first chunk is written by the new thread
        return output;
    }
 
    @POST
    @Path("/uris")
	public Response getURIs(final QueryParams params) {
		logger.debug("getURIs; got query: {}", params);
		QueryManagement queryMgr = getQueryManager();
    	try {
    		Collection<String> uris = queryMgr.getDocumentUris(params.query, params.params, params.props);
    		logger.debug("getURIs; returning URIs: {}", uris);
            return Response.ok(uris).build();
    	} catch (Exception ex) {
    		logger.error("getURIs.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
	
}
