package com.bagri.server.hazelcast.task.schema;

import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_DenitSchemaTask;
import static com.bagri.server.hazelcast.util.SpringContextHolder.*;
import static com.bagri.server.hazelcast.util.HazelcastUtils.findSchemaInstance;

import java.util.concurrent.Callable;

import org.springframework.context.ConfigurableApplicationContext;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaDenitiator extends SchemaProcessingTask implements Callable<Boolean> { 
	
	public SchemaDenitiator() {
		super();
	}

	public SchemaDenitiator(String schemaName) {
		super(schemaName);
	}

	@Override
	public Boolean call() throws Exception {
		//return schemaManager.denitSchema(schemaName);
		
    	logger.trace("call.enter; schema: {}", schemaName);
    	boolean result = false;
		// get hzInstance and close it...
		HazelcastInstance hz = findSchemaInstance(schemaName);
		if (hz != null) {
			ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) getContext(schemaName);
			ctx.close();
			result = true;
		}
    	logger.trace("call.exit; schema {} deactivated: {}", schemaName, result);
		return result;
	}

	@Override
	public int getId() {
		return cli_DenitSchemaTask;
	}

}
