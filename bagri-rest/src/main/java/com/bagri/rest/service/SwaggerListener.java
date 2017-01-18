package com.bagri.rest.service;

import static com.bagri.rest.RestConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.system.Function;
import com.bagri.core.system.Parameter;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.jaxrs.config.SwaggerConfigLocator;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.PropertyBuilder;
import io.swagger.models.properties.StringProperty;
//import io.swagger.util.

@SwaggerDefinition()
public class SwaggerListener implements ReaderListener {
	
    private static final Logger logger = LoggerFactory.getLogger(SwaggerListener.class);
	
    private static List<RestMethod> restMethods = new ArrayList<>();
    
    public SwaggerListener() {
    	logger.debug("SwaggerListener.<init>");
    }
    
    public static void addFunction(String basePath, Function fn) {
    	restMethods.add(new RestMethod(basePath, fn));
    }
    
    public static void clearFunctions() {
    	restMethods.clear();
    }

	@Override
	public void beforeScan(Reader reader, Swagger swagger) {
		//
	}

	@Override
	public void afterScan(Reader reader, Swagger swagger) {
		logger.debug("afterScan.enter; methods to process: {}; swagger: {}", restMethods.size(), swagger);
		for (RestMethod rm: restMethods) {
			String base = rm.getBasePath();
			Function fn = rm.getFunction();
			logger.debug("afterScan; processing function: {}", fn);
			Map<String, List<String>> annotations = fn.getAnnotations();
			List<String> consumes = annotations.get(an_consumes); 
	    	List<String> produces = annotations.get(an_produces);
			List<String> paths = annotations.get(an_path);
			String fullPath = base;
			if (paths != null) {
	        	fullPath += paths.get(0);
			}
			Path path = swagger.getPath(fullPath);
			if (path == null) {
				path = new Path();
			}
	        for (String method: methods) {
	        	List<String> values = annotations.get("rest:" + method);
	        	if (values != null) {
	        		Operation op = new Operation();
	        		op.addScheme(Scheme.HTTP);
	        		op.addScheme(Scheme.HTTPS);
	        		op.setDescription(fn.getDescription());
	    	        if (consumes != null) {
	    	        	op.consumes(consumes);
	    	        }
	    	        if (produces != null) {
	            		op.produces(produces);
	    	        }
	    	        op.setOperationId(fn.getMethod());
	    	        op.setSummary(fn.getSignature());
	    	        op.addTag(base.substring(1));
	    	        
	    	        for (Parameter param: fn.getParameters()) {
	    	        	String pName = param.getName();
	    	        	io.swagger.models.parameters.Parameter pm = null;
	    	        	if (rm.isPathParameter(pName)) {
	    	        		pm = new PathParameter().type(param.getType()).required(true);
	    	        	} else {
	    	    			String aType = rm.getParamAnnotationType(pName);
	    	    			if (aType == null) {
	    	    				// this is for POST/PUT only!
	    	    				if (POST.equals(method) || PUT.equals(method)) {
	    	    	        		pm = new BodyParameter();
	    	    				}
	    	    			} else {
	    	    				switch (aType) {
	    	    					case apn_cookie: {
		    	    	        		pm = new CookieParameter().type(param.getType());
	    	    						break;
	    	    					}
	    	    					case apn_form: {
		    	    	        		pm = new FormParameter().type(param.getType());
	    	    						break;
	    	    					}
	    	    					case apn_header: {
		    	    	        		pm = new HeaderParameter().type(param.getType());
	    	    						break;
	    	    					}
	    	    					case apn_matrix: {
	    	    						// no MatrixParameter in Swagger! just to get something:
	    	    						pm = new PathParameter().description("This is a MatrixParameter, which is not supported by Swagger. " + 
	    	    								"Please add custom client code to pass the parameter properly.").items(new MapProperty(new StringProperty())).type(param.getType());
	    	    						//PropertyBuilder.build(type, format, args);
	    	    						break;
	    	    					}
	    	    					case apn_query: {
		    	    	        		pm = new QueryParameter().type(param.getType());
	    	    						break;
	    	    					}
	    	    				}
	    	    			}
	    	        	}
	    	        	if (pm != null) {
	    	        		pm.setName(pName);
	    	        		op.addParameter(pm);
	    	        	}
	    	        }
	    	        op.defaultResponse(new Response());
	    	        //io.swagger.models.properties.PropertyBuilder.
	        		path.set(method.toLowerCase(), op);
	        		logger.debug("afterScan; added op: {} for method {}", op, method);
		        }
			}
			swagger.path(fullPath, path);
			logger.debug("afterScan; set path: {}", path.getOperations()); 
	        if (consumes != null) {
	        	for (String consume: consumes) {
	        		swagger.addConsumes(consume);
	        	}
	        }
	        if (produces != null) {
	        	for (String produce: produces) {
	        		swagger.addProduces(produce);
	        	}
	        }
			swagger.tag(new Tag().name(base.substring(1)));
		}
		//String configIdKey = SwaggerContextService.CONFIG_ID_DEFAULT;
        //SwaggerConfigLocator.getInstance().putSwagger(configIdKey, swagger);
	}
	
	private static class RestMethod {
		
		private String basePath;
		private Function fn;
		
		RestMethod(String basePath, Function fn) {
			this.basePath = basePath;
			this.fn = fn;
		}
		
		public String getBasePath() {
			return basePath;
		}
		
		public Function getFunction() {
			return fn;
		}
	
	    public boolean isPathParameter(String pName) {
	    	List<String> pa = fn.getAnnotations().get(an_path);
	    	return (pa != null && pa.size() == 1 && pa.get(0).indexOf("{" + pName + "}") > 0);
	    }
	    
	    public String getParamAnnotationType(String pName) {
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
		
	}

}

