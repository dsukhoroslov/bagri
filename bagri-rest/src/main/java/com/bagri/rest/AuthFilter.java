package com.bagri.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthFilter implements ContainerRequestFilter {

    private static final transient Logger logger = LoggerFactory.getLogger(AuthFilter.class);
	
	//@Override
	//public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	//	logger.info("init; got config: {}", filterConfig);
	//}

	//@Override
	//public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	//		throws IOException, ServletException {
		// TODO Auto-generated method stub
	//	logger.debug("doFilter; request: {}, response: {}, chanin: {}", request, response, chain);
	//}

	//@Override
	//public void destroy() {
		// TODO Auto-generated method stub
	//	logger.trace("destroy;");
	//}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// TODO Auto-generated method stub
		logger.debug("filter; context: {}", requestContext);
		
		final SecurityContext securityContext = requestContext.getSecurityContext();
		if (securityContext == null ||
                !securityContext.isUserInRole("privileged")) {

            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("User cannot access the resource.").build());
		}		
	}

}
