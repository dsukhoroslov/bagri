package com.bagri.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthFilter implements ContainerRequestFilter {

    private static final transient Logger logger = LoggerFactory.getLogger(AuthFilter.class);
	
    @Inject
    protected RepositoryProvider repos;
	
    private boolean checkAuth(String clientId) {
    	return repos.getRepository(clientId) != null;
    }

    //private String doAuth() {
    //	return "test";
    //}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// TODO Auto-generated method stub
		logger.debug("filter; context: {}", requestContext);
		
		final SecurityContext securityContext = requestContext.getSecurityContext();
		if (securityContext != null) {
			logger.debug("filter; auth scheme: {}; secure: {}", securityContext.getAuthenticationScheme(), securityContext.isSecure());
			String path = requestContext.getUriInfo().getPath();
			if ("access/login".equals(path)) {
				// just check https
				//if (securityContext.isSecure()) {
					//requestContext.getCookies().put("bg-auth", new Cookie("bg-auth", token));
				//} else {
		        //    requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
		        //            .entity("User not authenticated.").build());
				//}
			} else {
				Cookie cc = requestContext.getCookies().get("bg-auth");
				if (cc == null || !checkAuth(cc.getValue())) {
		            requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
		                    .entity("User cannot access the resource.").build());
				}
			}
		} else {
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                    .entity("No security context provided.").build());
		}
	} 

}
