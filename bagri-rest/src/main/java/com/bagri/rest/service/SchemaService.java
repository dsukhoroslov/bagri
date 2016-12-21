package com.bagri.rest.service;

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

import com.bagri.core.system.Schema;
import com.bagri.rest.RepositoryProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/schemas")
@Api(value = "schemas")
public class SchemaService extends RestService {
	
	@GET
    @Produces(MediaType.APPLICATION_JSON) //TEXT_PLAIN)
	@ApiOperation(value = "getSchemas: return Schema names accessible in the current session")
    public Collection<String> getSchemas() {
		logger.trace("getSchemas.enter");
        return repos.getSchemaNames();
    }
    
    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON) 
	@ApiOperation(value = "getSchemaAsJSON: return Schema settings in JSON format")
    public Response getSchemaAsJSON(@PathParam("name") String name) {
    	return getSchema(name);
    }    

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_XML) 
	@ApiOperation(value = "getSchemaAsXML: return Schema settings in XML format")
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