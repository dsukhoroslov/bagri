package com.bagri.rest;

import static com.bagri.xquery.api.XQUtils.getAtomicValue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
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
    		if (isPathParameter(pm.getName())) {
        		List<String> vals = context.getUriInfo().getPathParameters().get(pm.getName());
        		if (vals != null) {
        			// resolve cardinality..
        			params.put(pm.getName(), getAtomicValue(pm.getType(), vals.get(0)));
        		}    			
    		} else {
        		List<String> atns = getParamAnnotations("rest:query-param", pm.getName());
    			if (atns != null) {
    	    		List<String> vals = context.getUriInfo().getQueryParameters().get(pm.getName());
    				//logger.info("apply; query values: {}; ", vals);
    	    		if (vals != null) {
    	    			// resolve cardinality..
    	    			params.put(pm.getName(), getAtomicValue(pm.getType(), vals.get(0)));
    	    		} else {
    	    			setNotFoundParameter(params, atns, pm);
    	    		}
    			} else {
    				atns = getParamAnnotations("rest:form-param", pm.getName());
        			if (atns != null) {
        				// TODO: handle them properly..
        				// content type must be application/x-www-form-urlencoded
						boolean found = false;
    					String body = getBody(context);
    					if (body != null) {
            				logger.info("apply; form body: {}; ", body);
    						String[] parts = body.split("&");
    						for (String part: parts) {
    							int pos = part.indexOf("=");
    							String name = part.substring(0, pos);
    							if (name.equals(pm.getName())) {
    	        	    			params.put(pm.getName(), getAtomicValue(pm.getType(), part.substring(pos) + 1));
    	        	    			found = true;
    	        	    			break;
    							}
    						}
        				}
						if (!found) {
	    	    			setNotFoundParameter(params, atns, pm);
						}
        			} else {
        				atns = getParamAnnotations("rest:header-param", pm.getName());
        				if (atns != null) {
            				logger.info("apply; header annotations: {}", atns);
            	    		String val = context.getHeaderString(atns.get(0));
        					if (val != null) {
        						params.put(pm.getName(), getAtomicValue(pm.getType(), val));
            	    		} else {
            	    			setNotFoundParameter(params, atns, pm);
        					}
        				} else {
            				atns = getParamAnnotations("rest:cookie-param", pm.getName());
            				if (atns != null) {
                				logger.info("apply; cookie annotations: {}", atns);
                	    		Cookie val = context.getCookies().get(atns.get(0));
            					if (val != null) {
            						params.put(pm.getName(), getAtomicValue(pm.getType(), val.getValue()));
                	    		} else {
                	    			setNotFoundParameter(params, atns, pm);
            					}
            				} else {
                				atns = getParamAnnotations("rest:matrix-param", pm.getName());
                				if (atns != null) {
                    				logger.info("apply; matrix annotations: {}", atns);
                    				// does not work in Jersey: context.getUriInfo().getPathSegments();
                					//List<String> vals = getMatrixParams(context.getUriInfo().getPath());
            						boolean found = false;
            						String[] parts = context.getUriInfo().getPath().split("&");
            						for (String part: parts) {
            							int pos = part.indexOf("=");
            							String name = part.substring(0, pos);
            							if (name.equals(pm.getName())) {
            	        	    			params.put(pm.getName(), getAtomicValue(pm.getType(), part.substring(pos) + 1));
            	        	    			found = true;
            	        	    			break;
            							}
            						}
                					if (!found) {
                    	    			setNotFoundParameter(params, atns, pm);
                					}
                				} else {
                					String body = getBody(context);
                					if (context != null) {
                    	    			params.put(pm.getName(), getAtomicValue(pm.getType(), body));
                    				}
                				}
            				}
        				}
        			}
    			}
    		}
    		List<String> vals = context.getUriInfo().getPathParameters().get(pm.getName());
    		if (vals != null) {
    			// resolve cardinality..
    			params.put(pm.getName(), getAtomicValue(pm.getType(), vals.get(0)));
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
    
    private List<String> getParamAnnotations(String aName, String pName) {
    	List<String> pa = fn.getAnnotations().get(aName);
    	if (pa != null && pa.size() > 0) {
    		if (pName.equals(pa.get(0))) {
    			return pa;
    		} else if (pa.size() > 1 && ("{$" + pName + "}").equals(pa.get(1))) {
    			return pa;
    		}
    	}
    	return null;
    }
    
    private List<String> getMatrixParams(String path) {
    	List<String> pairs = new ArrayList<>();
    	for (String part: path.split(";")) {
    		int pos = part.indexOf("="); 
    		if (pos > 0) {
    			pairs.add(part.substring(0, pos));
    			pairs.add(part.substring(pos + 1));
    		}
    	}
    	return pairs;
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
