package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_XQFixedCursor;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.impl.ResultCursorBase;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class FixedCursorImpl extends ResultCursorBase implements IdentifiedDataSerializable {

	private Object value = null;
	private List<Object> values;
	private Iterator<Object> iter;
	
	public FixedCursorImpl() {
		this(null);
	}

	public FixedCursorImpl(List<Object> values) {
		//super(iter);
		this.values = values;
		iter = this.values.iterator();
	}
	
	@Override
	public void close() throws Exception {
		if (values.size() > 1) {
			// non-abstract impl
			values.clear();
		}
		values = null;
		value = null;
		iter = null;
	}

	public void deserialize(HazelcastInstance hzi) {
		position = 0;
	}

	public int serialize(HazelcastInstance hzi) {
		return values.size();
	}
	
	protected Object getCurrent() {
		return value;
	}
	
	@Override
	public List<?> getList() throws XDMException {
		return values;
	}

	@Override
	public boolean getNext() {
		boolean result = iter.hasNext();
		if (result) {
			value = iter.next();
		} else {
			value = null;
		}
		return result;
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
		values = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(values);
	}

	@Override
	public String toString() {
		return "FixedCursorImpl [position=" + position + ", values=" + values.size() + "]"; 
	}

}
