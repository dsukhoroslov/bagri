package com.bagri.rest.service;

import static com.bagri.rest.RestConstants.bg_cookie;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.bagri.core.api.SchemaRepository;
import com.bagri.rest.BagriRestServer;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/access")
@Api(value = "access")
public class AccessService extends RestService {

	@Inject
    private BagriRestServer server;
    
	@POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "login: creates new connection to Bagri REST server; requires HTTPS protocol")
    public Response login(@ApiParam(name = "params", value = "set of login parameters in JSON format", 
    	example = "{\"schema\": \"default\", \"user\": \"guest\", \"password\": \"xxxxxxxx\"}") final LoginParams params) {
		logger.debug("login.enter; got params: {}", params);
		if (repos.getSchema(params.schemaName) == null) {
		    return Response.status(Status.NOT_FOUND).entity("Unknown schema provided").build();
		} else {
			try {
			    SchemaRepository repo = repos.connect(params.schemaName, params.userName, params.password);
			    if (repo != null) {
				    NewCookie cookie = new NewCookie(bg_cookie, repo.getClientId());
				    server.reload(params.schemaName, false);
					logger.trace("login.exit; returning client: {}", repo.getClientId());
				    return Response.ok("OK").cookie(cookie).build();
			    } else {
				    return Response.status(Status.GONE).entity("Schema is not active").build();
			    }
			} catch (Exception ex) { // "Wrong credentials" ?
			    return Response.serverError().entity(ex.getMessage()).build();
			}
		}
    }
	
	@POST
    @Path("/logout")
    @Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "logout: disconnects current user from REST server")
	public Response logout() {
		logger.trace("logout.enter; cookie: {}", bgAuth);
		repos.disconnect(getClientId());
        NewCookie cookie = new NewCookie(bgAuth, null, 0, false);
        return Response.ok("OK").cookie(cookie).build();
		// may be we should notify server about disconnection?
        // in order to re-check activeSchemas..?
	}

}
