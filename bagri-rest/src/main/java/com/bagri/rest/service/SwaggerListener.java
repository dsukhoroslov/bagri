package com.bagri.rest.service;

import static com.bagri.rest.RestConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.system.Function;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;

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

	@Override
	public void beforeScan(Reader reader, Swagger swagger) {
		//
	}

	@Override
	public void afterScan(Reader reader, Swagger swagger) {
		logger.debug("afterScan; methods to process: {}", restMethods.size());
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
			Path path  = new Path();
	        for (String method: methods) {
	        	List<String> values = annotations.get("rest:" + method);
	        	if (values != null) {
	        		Operation op = new Operation();
	        		op.addScheme(Scheme.HTTP);
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
	        		path.set(method.toLowerCase(), op);
	        		logger.debug("afterScan; added op: {} for method {}", op, method);
					swagger.path(fullPath, path);
		        }
			}
	        if (consumes != null) {
	        	swagger.consumes(consumes);
	        }
	        if (produces != null) {
        		swagger.produces(produces);
	        }
			swagger.tag(new Tag().name(base.substring(1)));
		}
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
		
	}

}
