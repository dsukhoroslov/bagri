package com.bagri.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/bagri")
public class SchemaResources {
	
    private static final transient Logger logger = LoggerFactory.getLogger(SchemaResources.class);
	
	private Map<String, Map<String, Object>> schemas;
	
	public SchemaResources() {
		schemas = new HashMap<>();
		schemas.put("default", createMap("default", "2014-09-27", "admin", "default schema for test and demo purpose"));
		schemas.put("TPoX", createMap("TPoX", "2014-04-21", "admin", "TPoX: schema for TPoX-related tests"));
		schemas.put("XMark", createMap("XMark", "2014-07-15", "admin", "XMark benchmark schema"));
	}
	
	private Map<String, Object> createMap(String name, String createdAt, String createdBy, String description) {
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("createdAt", createdAt);
		map.put("createdBy", createdBy);
		map.put("description", description);
		return map;
	}

    @GET
    @Path("schemas")
    @Produces(MediaType.APPLICATION_JSON) //TEXT_PLAIN)
    public String getSchemas() {
        return "{[" + 
        	"{\"name\": \"default\", \"createdAt\": \"2014-09-27\", \"createdBy\": \"admin\", \"description\": \"default schema for test and demo purpose\"}," +
       		"{\"name\": \"TPoX\", \"createdAt\": \"2014-04-21\", \"createdBy\": \"admin\", \"description\": \"TPoX: schema for TPoX-related tests\"}," +
       		"{\"name\": \"XMark\", \"createdAt\": \"2014-07-15\", \"createdBy\": \"admin\", \"description\": \"XMark benchmark schema\"}" +
       		"]}";
    }
    
    @GET
    @Path("schemas/{name}")
    @Produces(MediaType.APPLICATION_JSON) 
    public Response getSchema(@PathParam("name") String name) {

        Map<String, Object> schema = schemas.get(name);
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