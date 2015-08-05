package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_DenitSchemaTask;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.bagri.xdm.cache.hazelcast.util.SpringContextHolder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
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
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
			ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) 
					SpringContextHolder.getContext(schemaName, "appContext");
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
