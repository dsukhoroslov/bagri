package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_FixedCollection;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCollection;
import com.bagri.core.api.SchemaRepository;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class FixedCollectionImpl implements ResultCollection, IdentifiedDataSerializable {
	
    private final static Logger logger = LoggerFactory.getLogger(FixedCollectionImpl.class);

	private ArrayList<Object> results;
	
	public FixedCollectionImpl() {
		//
	}

	public FixedCollectionImpl(int size) {
		results = new ArrayList<>(size);
	}
	
	public FixedCollectionImpl(Collection<Object> results) {
		this(results.size());
		this.results.addAll(results);
	}
	
	//@Override
	//public void init(SchemaRepository repo) {
		// no-op
	//}
	
	@Override
	public void close() throws Exception {
		results.clear();		
	}

	@Override
	public boolean add(Object result) {
		return results.add(result);
	}
	
	@Override
	public Iterator<Object> iterator() {
		return results.iterator();
	}
	
	@Override
	public int size() {
		return results.size();
	}
	
	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_FixedCollection;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		//results = in.readObject();
		int size = in.readInt();
		results = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			results.add(in.readObject());
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		//out.writeObject(results);
		out.writeInt(results.size());
		for (Object result: results) {
			out.writeObject(result);
		}
	}

	
}
