package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_ResultCollection;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCollection;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ResultCollectionImpl extends ResultCollection implements IdentifiedDataSerializable {

    private final static Logger logger = LoggerFactory.getLogger(ResultCollectionImpl.class);

	public ResultCollectionImpl() {
		super();
	}

	public ResultCollectionImpl(int size) {
		super(size);
	}

	public ResultCollectionImpl(Collection<Object> results) {
		super(results);
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_ResultCollection;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		//results = in.readObject();
		int size = in.readInt();
		results = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			int mSize = in.readInt();
			Map<String, String> map = new HashMap<>(mSize);
			for (int j=0; j < mSize; j++) {
				String key = in.readUTF();
				String value = in.readUTF();
				map.put(key, value);
			}
			results.add(map);
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		//out.writeObject(results);
		out.writeInt(results.size());
		for (int i=0; i < results.size(); i++) {
			Map<String, String> map = (Map<String, String>) results.get(i);
			out.writeInt(map.size());
			for (Map.Entry<String, String> entry: map.entrySet()) {
				out.writeUTF(entry.getKey());
				out.writeUTF(entry.getValue());
			}
		}
	}

}
