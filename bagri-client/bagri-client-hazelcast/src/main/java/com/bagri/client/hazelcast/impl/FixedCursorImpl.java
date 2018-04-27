package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_FixedCursor;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.bagri.core.api.impl.ResultCursorBase;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class FixedCursorImpl<T> extends ResultCursorBase<T> implements IdentifiedDataSerializable {

    protected List<T> results;
	
	public FixedCursorImpl() {
		//
	}

	public FixedCursorImpl(int size) {
		this.results = new ArrayList<>(size);
	}
	
	public FixedCursorImpl(Collection<T> results) {
		if (results != null) {
			this.results = new ArrayList<>(results);
		}
	}

	@Override
	public boolean add(T result) {
		return results.add(result);
	}
	
	@Override
	public void close() throws Exception {
		//logger.trace("close; results: {}", results);
		results.clear();
	}

	@Override
	public void finish() {
		// do nothing
	}
	
	@Override
	public List<T> getList() {
		return results;
	}

	@Override
	public boolean isAsynch() {
		return false;
	}
	
	@Override
	public boolean isEmpty() {
		return results.isEmpty(); 
	}

	@Override
	public Iterator<T> iterator() {
		return results.iterator();
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_FixedCursor;
	}
	
	@Override
	public int size() {
		return results.size();
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		this.results = new ArrayList<>(size);
		readResults(in, size);
	}
	
	protected void readResults(ObjectDataInput in, int size) throws IOException {
		for (int i=0; i < size; i++) {
			results.add((T) in.readObject());
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(results.size());
		writeResults(out);
	}
	
	protected void writeResults(ObjectDataOutput out) throws IOException {
		for (Object result: results) {
			out.writeObject(result);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [results=" + results.size() + "]"; 
	}

}

