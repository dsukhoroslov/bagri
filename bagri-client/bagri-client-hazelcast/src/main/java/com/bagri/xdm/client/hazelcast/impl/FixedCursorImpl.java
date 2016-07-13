package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_XQFixedCursor;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.Iterator;

import com.bagri.xdm.api.impl.ResultCursorBase;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class FixedCursorImpl extends ResultCursorBase implements IdentifiedDataSerializable {

	private Object value = null;
	
	public FixedCursorImpl() {
		this(null);
	}

	public FixedCursorImpl(Iterator<Object> iter) {
		super(iter);
	}
	
	public void deserialize(HazelcastInstance hzi) {
		position = 0;
	}

	public int serialize(HazelcastInstance hzi) {
		return 1;
	}
	
	protected Object getCurrent() {
		return value;
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}	
	
	@Override
	public Object next() {
		value = iter.next(); 
		return value;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_XQFixedCursor;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		//super.readData(in);
		//value = in.readObject();
		iter = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		//super.writeData(out);
		//out.writeObject(value);
		out.writeObject(iter);
	}

	@Override
	public String toString() {
		return "FixedCursorImpl [" + //clientId=" + getClientId() + //", queueSize=" + queueSize + 
			", position=" + position + "]"; //", batchSize=" + batchSize + ", value=" + value + "]";
	}

}
