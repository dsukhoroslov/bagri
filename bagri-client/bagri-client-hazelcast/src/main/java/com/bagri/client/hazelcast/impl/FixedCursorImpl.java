package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_FixedCursor;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.impl.ResultCursorBase;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class FixedCursorImpl extends ResultCursorBase implements IdentifiedDataSerializable {

	private Object value = null;
	private Iterator<Object> iter;
	private List<Object> values = new ArrayList<>();
	
	public FixedCursorImpl() {
		//
	}

	public FixedCursorImpl(List<Object> values) {
		setValues(values);
	}
	
	@Override
	public void close() throws Exception {
		values = null;
		value = null;
		iter = null;
	}

	protected Object getCurrent() {
		return value;
	}
	
	@Override
	public List<Object> getList() throws BagriException {
		return values;
	}

	@Override
	public boolean isFixed() {
		return true;
	}
	
	@Override
	public boolean next() {
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
		return cli_FixedCursor;
	}
	
	private void setValues(List<Object> values) {
		if (values != null) {
			this.values.addAll(values);
		}
		iter = this.values.iterator();
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		setValues((List) in.readObject());
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(values);
	}

	@Override
	public String toString() {
		return "FixedCursorImpl [values=" + values.size() + "]"; 
	}

}
