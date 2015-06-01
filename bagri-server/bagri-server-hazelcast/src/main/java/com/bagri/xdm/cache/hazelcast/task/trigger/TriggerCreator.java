package com.bagri.xdm.cache.hazelcast.task.trigger;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_CreateTriggerTask;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.system.XDMTriggerDef;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class TriggerCreator implements Callable<Boolean>, IdentifiedDataSerializable { 
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	private XDMTriggerDef trigger;
	private transient RepositoryImpl xdmRepo;
    
	public TriggerCreator() {
		//
	}
	
	public TriggerCreator(XDMTriggerDef trigger) {
		this.trigger = trigger;
	}

    @Autowired
	public void setXDMRepository(RepositoryImpl xdmRepo) {
		this.xdmRepo = xdmRepo;
		logger.trace("setXDMRepository; got Repository: {}", xdmRepo); 
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
