package com.bagri.rest.service;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.bagri.xdm.api.SchemaRepository;

//@Singleton
@Path("/access")
public class AccessService extends RestService {

	@POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(final LoginParams params) {
		logger.info("login.enter; got params: {}", params);
	    SchemaRepository repo = repos.connect(params.schemaName, params.userName, params.password);
	    if (repo != null) {
			logger.trace("login.exit; returning client: {}", repo.getClientId());
		    NewCookie cookie = new NewCookie(bg_cookie, repo.getClientId());
		    return Response.ok("OK").cookie(cookie).build();
	    } else {
		    return Response.status(Status.UNAUTHORIZED).entity("Wrong credentials").build();
	    }
    }
	
	@POST
    @Path("/logout")
    @Produces(MediaType.TEXT_PLAIN)
	public Response logout() {
		logger.trace("logout.enter; cookie: {}", bgAuth);
		repos.disconnect(bgAuth.getValue());
        NewCookie newCookie = new NewCookie(bgAuth, null, 0, false);
        return Response.ok("OK").cookie(newCookie).build();
	}

}
