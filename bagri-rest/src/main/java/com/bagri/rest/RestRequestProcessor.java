package com.bagri.rest;

import static com.bagri.rest.RestConstants.*;
import static com.bagri.support.util.JSONUtils.*;
import static com.bagri.support.util.XQUtils.*;
import static com.bagri.support.util.XMLUtils.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;

import org.eclipse.jetty.io.EofException;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.BagriException;
import com.bagri.core.system.Function;
import com.bagri.core.system.Parameter;

public class RestRequestProcessor implements Inflector<ContainerRequestContext, Response> {
	 
    private static final Logger logger = LoggerFactory.getLogger(RestRequestProcessor.class);
    
    private static final String ns_http = "http://www.expath.org/http"; 
    private static final String en_response = "response"; 
    private static final String en_header = "header";
    private static final String an_message = "message";
    private static final String an_name = "name"; 
    private static final String an_status = "status"; 
    private static final String an_value = "value";
    
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
    	
    	String clientId = context.getCookies().get(bg_cookie).getValue();
    	SchemaRepository repo = rePro.getRepository(clientId);
		logger.debug("apply.enter; path: {}; params: {}; query: {}", context.getUriInfo().getPath(), 
				context.getUriInfo().getPathParameters(), context.getUriInfo().getQueryParameters());

		Map<String, Object> params = getParameters(context);
		logger.debug("apply; got params: {} for method: {}", params, fn); 

    	boolean empty = false;
		ResultCursor<XQItem> cursor = null;
		// props must be local variable!
		Properties props = getQueryProperties();
		logger.trace("apply; going to execute query: {}\n with params: {} and props: {}", query, params, props); 
		try {
			cursor = repo.getQueryManagement().executeQuery(query, params, props);
	    	empty = cursor.isEmpty();
		} catch (BagriException ex) {
			logger.error("apply.error: ", ex);
			return Response.serverError().entity(ex.getMessage()).build();
		}
	    logger.debug("apply; got cursor: {}", cursor);
	    
    	Response.ResponseBuilder response;
    	if (empty) {
	        response = Response.noContent();	    	
    	} else {
    		response = Response.ok();
	    	try {
	    		//empty = fillResponse(cursor, response);
		    	//if (!empty) {
		    		response.entity(getResultStream(cursor));
		    	//}
		    } catch (Exception ex) { //BagriException
				logger.error("apply.error: error processing response ", ex);
				response = Response.serverError().entity(ex.getMessage());
		    }
    	}
		logger.debug("apply; got response: {}", response);

		// check and process here bgdb:follow-rules("create/update/delete/..") annotations
		applyRestRules(empty, response);
	    
        return response.build();
	}
    
    private Map<String, Object> getParameters(ContainerRequestContext context) {
    	Map<String, Object> params = new HashMap<>(fn.getParameters().size());
    	for (Parameter pm: fn.getParameters()) {
    		logger.trace("getParameters; processing param: {}", pm);
			// TODO: resolve cardinality properly!
    		if (isPathParameter(fn, pm.getName())) {
        		List<String> vals = context.getUriInfo().getPathParameters().get(pm.getName());
        		if (vals != null) {
        			params.put(pm.getName(), getAtomicValue(pm.getType(), vals.get(0)));
        		}    			
    		} else {
    			String aType = getParamAnnotationType(fn, pm.getName());
        		logger.trace("getParameters; param annotation: {}", aType);
    			if (aType == null) {
    				// this is for POST/PUT only!
    				if (POST.equals(context.getMethod()) || PUT.equals(context.getMethod())) {
    					String body = getBody(context);
    					if (body != null) {
    						//params.put(pm.getName(), getAtomicValue(pm.getType(), body));
    						params.put(pm.getName(), extractBodyValue(context, pm, body));
    					}
    				}
    			} else {
        			boolean found = false;
    				switch (aType) {
    					case apn_cookie: {
            	    		Cookie val = context.getCookies().get(pm.getType());
        					if (val != null) {
        						params.put(pm.getName(), getAtomicValue(pm.getType(), val.getValue()));
            	    			found = true;
        					}
        					break;
    					}
    					case apn_form: {
            				// content type must be application/x-www-form-urlencoded
        					String body = getBody(context);
        					if (body != null) { // it'll be null because of the getBody implementation!?
                				//logger.info("apply; form body: {}; ", body);
        						String val = getParamValue(body, "&", pm.getName());
        						if (val != null) {
       	        	    			params.put(pm.getName(), getAtomicValue(pm.getType(), val));
       	        	    			found = true;
        						}
            				}
        					break;
    					}
    					case apn_header: {
            	    		String val = context.getHeaderString(pm.getName()); //atns.get(0)); !!!
        					if (val != null) {
        						params.put(pm.getName(), getAtomicValue(pm.getType(), val));
            	    			found = true;
        					}
        					break;
    					}
    					case apn_matrix: {
            				// does not work in Jersey: context.getUriInfo().getPathSegments();
    						String val = getParamValue(context.getUriInfo().getPath(), "&", pm.getName());
    						if (val != null) {
   	        	    			params.put(pm.getName(), getAtomicValue(pm.getType(), val));
   	        	    			found = true;
    						}
    						break;
    					}
    					case apn_query: {
    	    	    		List<String> vals = context.getUriInfo().getQueryParameters().get(pm.getName());
    	    	    		if (vals != null) {
    	    	    			if (pm.getCardinality().isMultiple()) {
    	    	    				params.put(pm.getName(), getSequenceValue(pm.getType(), vals));
    	    	    			} else {
    	    	    				params.put(pm.getName(), getAtomicValue(pm.getType(), vals.get(0)));
    	    	    			}
    	    	    			found = true;
    	    	    		}
    	    	    		break;
    					}
    				}
    				if (!found) {
    	    			setNotFoundParam(params, aType, pm);
    				}
    			}
    		}
    	}
    	return params;
    }

    public static boolean isPathParameter(Function fn, String pName) {
    	List<String> pa = fn.getFlatAnnotations(an_path);
    	if (pa != null && pa.size() == 1) {
    		String pb = pa.get(0);
    		return (pb.indexOf("{" + pName + "}") > 0);
    	}
    	return false; 
    }
    
    public static String getParamAnnotationType(Function fn, String pName) {
		String xpName = "{$" + pName + "}";
    	for (Map.Entry<String, List<List<String>>> ant: fn.getAnnotations().entrySet()) {
    		for (List<String> values: ant.getValue()) {
    			for (String val: values) {
	    			//if (pName.equals(val) || xpName.equals(val)) {
	    			if (xpName.equals(val)) {
	    				return ant.getKey();
	    			}
    			}
    		}
    	}
    	return null;
    }

    private String getBody(ContainerRequestContext context) {
		if (context.hasEntity() && (POST.equals(context.getMethod()) || PUT.equals(context.getMethod()))) {
		    java.util.Scanner s = new java.util.Scanner(context.getEntityStream()).useDelimiter("\\A");
		    String result = s.next();
		    s.close();
	    	return result;
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
    
    private List<Object> getSequenceValue(String type, List<String> values) {
    	List<Object> list = new ArrayList<>();
    	if (values.size() > 1) {
    		for (String value: values) {
    			list.add(getAtomicValue(type, value));
    		}
    	} else if (values.size() > 0){
    		String[] vals = values.get(0).split(",");
    		for (String value: vals) {
    			list.add(getAtomicValue(type, value));
    		}
    	}
    	return list;
    }
    
    private Object extractBodyValue(ContainerRequestContext context, Parameter pm, String content) { //throws IOException {
		logger.trace("extractBodyValue.enter; got param: {}; content: {}; mediaType: {}", pm, content, context.getMediaType());
    	if (isBaseType(pm.getType())) {
			if (pm.getCardinality().isMultiple()) {
				List<String> values = new ArrayList<>(1);
				values.add(content);
				return getSequenceValue(pm.getType(), values);
			} else {
				return getAtomicValue(pm.getType(), content);
			}
    	} else if (pm.getType().startsWith("map(")) {
    		if (isSubtypeOf(context, "json")) {
    			return mapFromJSON(content);
    		}
    		if (isSubtypeOf(context, "xml")) {
    			return mapFromXML(content);
    		}
    	} else if (pm.getType().startsWith("document-node(")) {
    		try {
    			return textToDocument(content);
    		} catch (IOException ex) {
    			logger.error("", ex);
    			return null;
    		}
    	} else if (pm.getType().startsWith("item(")) {
    		return content; 
    	}
    	return content;
    }
    
    private boolean isSubtypeOf(ContainerRequestContext context, String subtype) {
    	return context.getMediaType().getSubtype().endsWith(subtype);
    }
    
    private void setNotFoundParam(Map<String, Object> params, String pType, Parameter pm) {
		// handle default values
    	List<List<String>> atns = fn.getAnnotations().get(pType);
    	if (atns != null) {
    		String pName = pm.getName();
    		String xpName = "{$" + pm.getName() + "}";
       		for (List<String> values: atns) {
       			if (values.size() > 2) {
       				if (pName.equals(values.get(0)) || xpName.equals(values.get(1))) {
       					params.put(pm.getName(), getAtomicValue(pm.getType(), values.get(2)));
       		    		return;
       				}
        		}
        	}
    	}
		
    	if (pm.getCardinality().isOptional()) {
			// pass empty value..
			params.put(pm.getName(), null);
		}
    }
    
    private boolean fillResponse(ResultCursor<XQItem> cursor, Response.ResponseBuilder response) throws BagriException {
    	Node node = null;
    	String message = null;
    	int status = Response.Status.OK.getStatusCode(); 
    	boolean empty = cursor.isEmpty();
    	if (!empty) {
    		XQItem item = cursor.iterator().next();
	    	try {
	    		node = item.getNode();
	       		logger.debug("fillResponse; got node: {}", node);
	    	} catch (XQException ex) {
	       		logger.debug("fillResponse; got non-xml content, skipping");
	    	}
    	}

    	if (node != null) {
    		logger.trace("fillResponse; uri: {}; name: {}; type: {}", node.getNamespaceURI(), node.getNodeName(), node.getNodeType());
    		Element elt = (Element) node;
    		NodeList nodes = elt.getElementsByTagNameNS(ns_http, en_response);
    		if (nodes.getLength() > 0) {
    			elt = (Element) nodes.item(0);
    			String sts = elt.getAttribute(an_status);
		    	if (!sts.isEmpty()) {
		    		status = Integer.parseInt(sts);
		    	}
	    		message = elt.getAttribute(an_message);
		    	// set Response headers..
		    	NodeList children = elt.getElementsByTagNameNS(ns_http, en_header);
		    	for (int i=0; i < children.getLength(); i++) {
		    		elt = (Element) children.item(i);
	    			String name = elt.getAttribute(an_name);
	    			String value = elt.getAttribute(an_value);
	    			if (!name.isEmpty() && !value.isEmpty()) {
	    				response.header(name, value);
	    			}
		    	}
	   			// move cursor one position further
	   			empty = cursor.isEmpty();
   			} else {
   				logger.debug("fillResponse; non-standard response structure: {}", elt);
   			}
    	}
    	
        response.status(status);
    	if (empty) {
    		response.entity(message);
    	}
    	return empty;
    }

	private void applyRestRules(boolean empty, Response.ResponseBuilder response) {
    	List<String> ra = fn.getFlatAnnotations(apn_rest_rules);
    	if (ra != null && !ra.isEmpty()) {
    		logger.debug("applyRestRules; got rules annotations: {}", ra);
    		String rType = ra.get(0);
    		switch (rType) {
    			case arv_create:
    			case arv_update:
    			case arv_delete:
    	    		// TODO: implement it..
    		}
    	}
	}

    private StreamingOutput getResultStream(final ResultCursor<XQItem> result) {
    	String start = "";
    	String delim = "\n";
    	String end = "";
    	List<String> ra = fn.getFlatAnnotations(apn_rest_chunk_type);
    	// must be zero or one!
    	if (!ra.isEmpty()) {
    		String type = ra.get(0);
    		if ("json".equalsIgnoreCase(type)) {
    			start = "[";
    			delim = ",\n";
    			end = "]";
    		}
    		// think about other types/delims..
    	}
    	final String first = start;
    	final String delimiter = delim;
    	final String last = end;
    	
   	    return new StreamingOutput() {
   	        @Override
   	        public void write(OutputStream os) throws IOException, WebApplicationException {
   	        	// fixed cursor will be complete from the very beginning
            	int idx = 0;
   	        	Iterator<XQItem> iter = result.iterator();
                logger.trace("write; got result: {} with iter: {}", result, iter);
   	            try (Writer writer = new BufferedWriter(new OutputStreamWriter(os))) {
   	            	writer.write(first);
   	   	        	while (true) {
   	   	        		if (iter.hasNext()) {
   	   	        			XQItem item = iter.next();
   	   		            	if (idx > 0) {
   	   		            		writer.write(delimiter);
   	   		            	}
   	   		            	String chunk = item.getAtomicValue(); // get as string ?
   	   		                logger.trace("write; idx: {}; chunk: {}", idx, chunk);
   	   		                writer.write(chunk);
   	   			            writer.flush();
   	   			            idx++;
   	   	        		} else {
   	   		                logger.trace("write; no next; idx: {}", idx);
   	   	        			if (result.isComplete()) {
   	   	   		                logger.trace("write; no next, got complete; {}", idx);
	   	   	        			// doublecheck to avoid concurrent update in asynch cursor
	   	   	        			if (!iter.hasNext()) {
	   	   	   		                logger.trace("write; complete, but no next; {}", idx);
	   	   	        				break;
	   	   	        			}
	   	   	        		} else {
	   	    	        		try {
	   	    	        			Thread.sleep(10);
	   	    	        		} catch (InterruptedException e) {
	   	   	   		                logger.info("write; interrupted at {} chunk", idx);
	   	    	        			break;
	   	    	        		}
	   	   	        		}
   	   	        		}
   	   	        	}
   	            	writer.write(last);
   	            	writer.flush();
	                logger.debug("write; written {} chunks", idx);
   	            } catch (EofException ex) {
   	            	logger.info("write; client has terminated connection at {} chunk, out of {} total chunks", idx, result.size());
   	            } catch (Exception ex) {
   	            	logger.error("write.error: error getting result from cursor ", ex);
           			// how to handle it properly?? throw WebAppEx?
                } finally {
                   	try {
   						result.close();
   					} catch (Exception ex) {
   		            	logger.error("write.error: closing cursor ", ex);
   					}
                }
            }
        };
    }
    
    private Properties getQueryProperties() {
    	Properties props = new Properties();
    	List<List<String>> paa = fn.getAnnotations().get(apn_properties);
    	if (paa != null) {
    		for (List<String> properties: paa) {
    			for (String property: properties) {
    				int pos = property.indexOf("=");
    				if (pos > 0) {
    					props.setProperty(property.substring(0, pos), property.substring(pos + 1));
    				}
    			}
    		}
    	}
    	
    	paa = fn.getAnnotations().get(apn_property);
    	if (paa != null) {
    		for (List<String> properties: paa) {
    			if (properties.size() == 2) {
					props.setProperty(properties.get(0), properties.get(1));
    			}
    		}
    	}
		logger.debug("getQueryProperties; resolved props: {}", props);
    	return props;
    }
    
}
