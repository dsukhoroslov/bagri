package com.bagri.server.hazelcast.task.trigger;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_CreateTriggerTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.system.TriggerDefinition;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class TriggerCreator implements Callable<Boolean>, IdentifiedDataSerializable { 
	
	private static final transient Logger logger = LoggerFactory.getLogger(TriggerCreator.class);
	
	private TriggerDefinition trigger;
	private transient SchemaRepositoryImpl xdmRepo;
    
	public TriggerCreator() {
		//
	}
	
	public TriggerCreator(TriggerDefinition trigger) {
		this.trigger = trigger;
	}

    @Autowired
	public void setXDMRepository(SchemaRepositoryImpl xdmRepo) {
		this.xdmRepo = xdmRepo;
	}
	
	@Override
	public Boolean call() throws Exception {
		logger.trace("call.enter");
		long stamp = System.currentTimeMillis();
		boolean result = xdmRepo.addSchemaTrigger(trigger);
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("call.exit; returning: {}; time taken: {}", result, stamp);
		return result;
	}
	
	@Override
	public int getId() {
		return cli_CreateTriggerTask;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		trigger = in.readObject();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(trigger);
	}


}
