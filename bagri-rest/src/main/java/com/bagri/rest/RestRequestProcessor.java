package com.bagri.rest;

import static com.bagri.xquery.api.XQUtils.getAtomicValue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

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
    	
    	String clientId = context.getCookies().get(RestService.bg_cookie).getValue();
    	SchemaRepository repo = rePro.getRepository(clientId);
    	Map<String, Object> params = new HashMap<>(fn.getParameters().size());
		logger.debug("apply.enter; path: {}; params: {}; query: {}", context.getUriInfo().getPath(), 
				context.getUriInfo().getPathParameters(), context.getUriInfo().getQueryParameters());
    	for (Parameter pm: fn.getParameters()) {
			// TODO: resolve cardinality properly!
    		if (isPathParameter(pm.getName())) {
        		List<String> vals = context.getUriInfo().getPathParameters().get(pm.getName());
        		if (vals != null) {
        			params.put(pm.getName(), getAtomicValue(pm.getType(), vals.get(0)));
        		}    			
    		} else {
    			String aType = getParamAnnotationType(pm.getName());
    			if (aType == null) {
    				// this is for POST/PUT only!
    				if ("POST".equals(context.getMethod()) || "PUT".equals(context.getMethod())) {
    					String body = getBody(context);
    					if (body != null) {
    						params.put(pm.getName(), getAtomicValue(pm.getType(), body));
    					}
    				}
    			} else {
        			boolean found = false;
        			List<String> atns = Collections.emptyList();
    				switch (aType) {
    					case "rest:cookie-param": {
            	    		Cookie val = context.getCookies().get(pm.getType());
        					if (val != null) {
        						params.put(pm.getName(), getAtomicValue(pm.getType(), val.getValue()));
            	    			found = true;
        					}
    					}
    					case "rest:form-param": {
            				// content type must be application/x-www-form-urlencoded
        					String body = getBody(context);
        					if (body != null) {
                				//logger.info("apply; form body: {}; ", body);
        						String val = getParamValue(body, "&", pm.getName());
        						if (val != null) {
       	        	    			params.put(pm.getName(), getAtomicValue(pm.getType(), val));
       	        	    			found = true;
        						}
            				}
        					break;
    					}
    					case "rest:header-param": {
            	    		String val = context.getHeaderString(pm.getName()); //atns.get(0)); !!!
        					if (val != null) {
        						params.put(pm.getName(), getAtomicValue(pm.getType(), val));
            	    			found = true;
        					}
        					break;
    					}
    					case "rest:matrix-param": {
            				// does not work in Jersey: context.getUriInfo().getPathSegments();
    						String val = getParamValue(context.getUriInfo().getPath(), "&", pm.getName());
    						if (val != null) {
   	        	    			params.put(pm.getName(), getAtomicValue(pm.getType(), val));
   	        	    			found = true;
    						}
    					}
    					case "rest:query-param": {
    	    	    		List<String> vals = context.getUriInfo().getQueryParameters().get(pm.getName());
    	    	    		if (vals != null) {
    	    	    			params.put(pm.getName(), getAtomicValue(pm.getType(), vals.get(0)));
    	    	    			found = true;
    	    	    		}
    	    	    		break;
    					}
    				}
    				if (!found) {
    	    			setNotFoundParameter(params, atns, pm);
    				}
    			}
    		}
    	}
    	logger.debug("apply; got params: {}", params); 
		Properties props = new Properties();
		try {
			final ResultCursor cursor = repo.getQueryManagement().executeQuery(query, params, props);
	    	logger.debug("apply.exit; got cursor: {}", cursor);
	    	StreamingOutput stream = new StreamingOutput() {
	            @Override
	            public void write(OutputStream os) throws IOException, WebApplicationException {
	                try (Writer writer = new BufferedWriter(new OutputStreamWriter(os))) {
		                while (cursor.next()) {
		                	String chunk = cursor.getItemAsString(null); 
		                    logger.trace("write; out: {}", chunk);
		                    writer.write(chunk + "\n");
			                writer.flush();
		                }
	                } catch (XDMException ex) {
	        			logger.error("apply.error: error getting result from cursor ", ex);
	        			// how to handle it properly?? throw WebAppEx?
	                } 
	            }
	        };
	        return Response.ok(stream).build();	    	
		} catch (XDMException ex) {
			logger.error("apply.error: ", ex);
			return Response.serverError().entity(ex.getMessage()).build();
		}
	}

    private boolean isPathParameter(String pName) {
    	List<String> pa = fn.getAnnotations().get("rest:path");
    	return (pa != null && pa.size() == 1 && pa.get(0).indexOf("{" + pName + "}") > 0);
    }
    
    private String getParamAnnotationType(String pName) {
		String xpName = "{$" + pName + "}";
    	for (Map.Entry<String, List<String>> ant: fn.getAnnotations().entrySet()) {
    		for (String val: ant.getValue()) {
    			if (pName.equals(val) || xpName.equals(val)) {
    				return ant.getKey();
    			}
    		}
    	}
    	return null;
    }

    private String getParamValue(String s, String d, String p) {
		String[] parts = s.split(d);
		for (String part: parts) {
			int pos = part.indexOf("=");
			if (pos > 0) {
				String name = part.substring(0, pos);
				if (name.equals(p)) {
	    			return part.substring(pos + 1);
				}
			}
		}
    	return null;
    }
    
    private String getBody(ContainerRequestContext context) {
		if (context.hasEntity() && ("POST".equals(context.getMethod()) || "PUT".equals(context.getMethod()))) {
		    java.util.Scanner s = new java.util.Scanner(context.getEntityStream()).useDelimiter("\\A");
			return s.next();
		}
    	return null;
    }
    
    private void setNotFoundParameter(Map<String, Object> params, List<String> atns, Parameter pm) {
		// handle default values
		if (atns.size() > 2) {
			params.put(pm.getName(), getAtomicValue(pm.getType(), atns.get(2)));
		} else if (pm.getCardinality().isOptional()) {
			// pass empty value..
			params.put(pm.getName(), null);
		}
    }
    
}
