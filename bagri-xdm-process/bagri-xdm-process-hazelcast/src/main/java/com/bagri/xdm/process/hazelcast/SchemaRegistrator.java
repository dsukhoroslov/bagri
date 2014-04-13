package com.bagri.xdm.process.hazelcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaRegistrator extends com.bagri.xdm.access.hazelcast.process.SchemaRegistrator {

	//private static final long serialVersionUID = -5042940226363419863L;

	private static final transient Logger logger = LoggerFactory.getLogger(SchemaRegistrator.class);
    
	private transient XDMSchemaDictionary schemaManager;
	
    @Autowired
	public void setSchemaManager(XDMSchemaDictionary schemaManager) {
		this.schemaManager = schemaManager;
		logger.debug("setSchemaManager; got manager: {}", schemaManager); 
	}
	
	@Override
	public Integer call() throws Exception {
		logger.trace("call.enter; schemaManager: {} ", schemaManager);

		schemaManager.registerSchema(schema);
		Integer result = 1;
		
		logger.trace("process.exit; returning: {}", result);
		return result;
	}
	


}
