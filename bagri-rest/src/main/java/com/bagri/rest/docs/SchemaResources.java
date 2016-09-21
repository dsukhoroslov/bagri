package com.bagri.rest.docs;

import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.system.Schema;

//@Path("/schemas")
public class SchemaResources {
	
    private static final transient Logger logger = LoggerFactory.getLogger(SchemaResources.class);

    @Inject
    private RepositoryProvider repos;
    
	@GET
    @Path("/schemas")
    @Produces(MediaType.APPLICATION_JSON) //TEXT_PLAIN)
    public Collection<String> getSchemas() {
        return repos.getSchemaNames();
    }
    
    @GET
    @Path("/schemas/{name}")
    @Produces(MediaType.APPLICATION_JSON) 
    public Response getSchemaAsJSON(@PathParam("name") String name) {
    	return getSchema(name);
    }    

    @GET
    @Path("/schemas/{name}")
    @Produces(MediaType.APPLICATION_XML) 
    public Response getSchemaAsXML(@PathParam("name") String name) {
    	return getSchema(name);
    }    

    private Response getSchema(String name) {
        Schema schema = repos.getSchema(name);
        if (schema != null) {
        	try {
        		return Response.ok(schema).build(); 
        	} catch (Exception ex) {
        		logger.error("getSchema.error", ex);
        		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        	}
        }
        return Response.status(Response.Status.NOT_FOUND).entity("No Schema found for name: " + name).build();
    }
    
}