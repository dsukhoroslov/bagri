package com.bagri.rest.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.rest.RestRequestProcessor;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
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
		logger.debug("afterScan; reader: {}; swagger: {}; processors: {}", reader, swagger, processors);
		for (RestRequestProcessor pro: processors) {
			//swagger.addDefinition(pro., model);
		}
	}

}
