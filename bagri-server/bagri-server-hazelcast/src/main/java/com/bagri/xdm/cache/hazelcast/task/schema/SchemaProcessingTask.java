package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class SchemaProcessingTask implements IdentifiedDataSerializable { 
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	protected String schemaName;
	protected HazelcastInstance hzInstance;
	
	public SchemaProcessingTask() {
		//
	}
	
	public SchemaProcessingTask(String schemaName) {
		// this();
		this.schemaName = schemaName;
	}

	@Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		schemaName = in.readUTF();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(schemaName);
	}

}
