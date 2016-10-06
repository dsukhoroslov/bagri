package com.bagri.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQItem;

import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.rest.service.RestService;
import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.system.Function;
import com.bagri.xdm.system.Parameter;

public class RestRequestProcessor implements Inflector<ContainerRequestContext, Response> {
	 
    private static final transient Logger logger = LoggerFactory.getLogger(RestRequestProcessor.class);
	
    private Function fn;
	private String query;
	private RepositoryProvider rePro;
	
	public RestRequestProcessor(Function fn, String query, RepositoryProvider rePro) {
		this.fn = fn;
		this.query = query;
		this.rePro = rePro;
	}

    @Override
    public Response apply(ContainerRequestContext context) {
    	
		//XQDataFactory xqFactory = xqp.getXQDataFactory();
		//XQItem item = xqFactory.createItemFromNode(xDoc, xqFactory.createDocumentType());
		//xqp.bindVariable("doc", item);
    	
    	String clientId = context.getCookies().get(RestService.bg_cookie).getValue();
    	SchemaRepository repo = rePro.getRepository(clientId);
    	Map<String, Object> params = new HashMap<>(fn.getParameters().size());
    	for (Parameter pm: fn.getParameters()) {
    		List<String> vals = context.getUriInfo().getPathParameters().get(pm.getName());
    		if (vals != null) {
    			// resolve cardinality..
    			params.put(pm.getName(), vals.get(0));
    		}
    	}
    	logger.debug("apply.enter; got params: {}", params); 
		Properties props = new Properties();
		try {
			ResultCursor cursor = repo.getQueryManagement().executeQuery(query, params, props);
	    	logger.debug("apply.exit; got cursor: {}", cursor);
	    	List<String> result = new ArrayList<>();
            while (cursor.next()) {
            	//if (cnt > 0) {
                //    output.write(splitter);
            	//}
            	String chunk = cursor.getItemAsString(null); 
                logger.trace("postQuery; out: {}", chunk);
                result.add(chunk);
                //cnt++;
            }
			return Response.ok().entity(result).build();
		} catch (XDMException ex) {
			logger.error("apply.error: ", ex);
			return Response.serverError().entity(ex.getMessage()).build();
		}
    	
	}
    
}
