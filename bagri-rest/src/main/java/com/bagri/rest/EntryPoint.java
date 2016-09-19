package com.bagri.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/bagri")
public class EntryPoint {

    @GET
    @Path("schemas")
    @Produces(MediaType.APPLICATION_JSON) //TEXT_PLAIN)
    public String test() {
        return "{\"Name\": \"Test Schema\"}";
    }

}