package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_XQFixedCursor;

import java.io.IOException;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class FixedCursor extends ResultCursor {

	private Object value = null;
	
	public FixedCursor() {
		// for de-serializer
	}

	public FixedCursor(String clientId, int batchSize, Object value) {
		super(clientId, batchSize, null, 1);
		this.value = value;
	}
	
	public void deserialize(HazelcastInstance hzi) {
		position = 0;
	}

	public int serialize(HazelcastInstance hzi) {
		return 1;
	}
	
	@Override
	public boolean hasNext() {
		return value != null;
	}	
	
	
	@Override
	public Object next() {
		Object result = value;
		if (value != null) {
			position++;
			value = null;
		}
		return result;
	}

	@Override
	public void close(boolean destroy) {
		// no-op
	}
	
	@Override
	public int getId() {
		return cli_XQFixedCursor;
	}

	@Override
	public String toString() {
		return "FixedCursor [clientId=" + getClientId() + ", queueSize=" + queueSize + 
			", position=" + position + ", batchSize=" + batchSize + ", value=" + value + "]";
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		value = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeObject(value);
	}
	
	
}
