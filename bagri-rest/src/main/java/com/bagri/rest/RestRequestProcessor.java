package com.bagri.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQItem;

import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.XDMException;

public class RestRequestProcessor implements Inflector<ContainerRequestContext, Response> {
	 
    private static final transient Logger logger = LoggerFactory.getLogger(RestRequestProcessor.class);
	
	private String query;
	private SchemaRepository repo;
	
	public RestRequestProcessor(String query, SchemaRepository repo) {
		this.query = query;
		this.repo = repo;
	}

    @Override
    public Response apply(ContainerRequestContext containerRequestContext) {
    	
		//XQDataFactory xqFactory = xqp.getXQDataFactory();
		//XQItem item = xqFactory.createItemFromNode(xDoc, xqFactory.createDocumentType());
		//xqp.bindVariable("doc", item);
    	
    	logger.debug("apply.enter; got context: {}", containerRequestContext); 
    	
    	Map<String, Object> params = new HashMap<>();
		Properties props = new Properties();
		try {
			ResultCursor cursor = repo.getQueryManagement().executeQuery(query, params, props);
	    	logger.debug("apply.exit; got cursor: {}", cursor); 
			return Response.ok().entity(cursor).build();
		} catch (XDMException ex) {
			logger.error("apply.error: ", ex);
			return Response.serverError().entity(ex.getMessage()).build();
		}
    	
	}
    
}
