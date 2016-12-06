package com.bagri.rest.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.rest.RestRequestProcessor;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.SwaggerDefinition.Scheme;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;

@SwaggerDefinition()
public class SwaggerListener implements ReaderListener {
	
    private static final Logger logger = LoggerFactory.getLogger(SwaggerListener.class);
	
    private static List<RestRequestProcessor> processors = new ArrayList<>();
    
    public SwaggerListener() {
    	logger.debug("SwaggerListener.<init>");
    }
    
    public static void addRequestProcessor(RestRequestProcessor pro) {
    	processors.add(pro);
    }

	@Override
	public void beforeScan(Reader reader, Swagger swagger) {
		//
	}

	@Override
	public void afterScan(Reader reader, Swagger swagger) {
		for (RestRequestProcessor pro: processors) {
			//reader.
			logger.debug("afterScan; processing: {}", pro);
			ModelImpl impl = new ModelImpl();
			//impl.setTitle("title");
			//impl.
			//swagger.addDefinition(pro., model);
		}
	}

}
