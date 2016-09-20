package com.bagri.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.system.Schema;

@Path("/bagri")
public class SchemaResources {
	
    private static final transient Logger logger = LoggerFactory.getLogger(SchemaResources.class);

    //@Inject
    private RepositoryProvider repos;
    
	public SchemaResources() {
		repos = new RepositoryProvider();
	}
	
    @GET
    @Path("schemas")
    @Produces(MediaType.APPLICATION_JSON) //TEXT_PLAIN)
    public Collection<String> getSchemas() {
        return repos.getSchemaNames();
    }
    
    @GET
    @Path("schemas/{name}")
    @Produces(MediaType.APPLICATION_XML) 
    public Response getSchema(@PathParam("name") String name) {

        Schema schema = repos.getSchema(name);
        if (schema != null) {
        	try {
        		return Response.ok(schema).build(); //return xdm schema directly? Response.ok(xdmSchema).build();
        	} catch (Exception ex) {
        		logger.error("getSchema.error", ex);
        		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        	}
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }    

}