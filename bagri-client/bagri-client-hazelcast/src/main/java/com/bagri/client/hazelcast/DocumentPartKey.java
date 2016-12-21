package com.bagri.client.hazelcast;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_DocumentKey;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;

import com.bagri.core.DocumentKey;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DocumentPartKey extends DocumentKey implements IdentifiedDataSerializable, PartitionAware<Integer> {
	
	public DocumentPartKey() {
		super();
	}

	public DocumentPartKey(long key) {
		super(key);
	}

	public DocumentPartKey(int hash, int revision, int version) {
		super(hash, revision, version);
	}

	@Override
	public Integer getPartitionKey() {
		return getHash();
	}
	
	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_DocumentKey;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		key = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(key);
	}

}
