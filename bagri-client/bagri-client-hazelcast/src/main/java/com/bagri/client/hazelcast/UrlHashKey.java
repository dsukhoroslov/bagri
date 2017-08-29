package com.bagri.client.hazelcast;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_UrlHashKey;

import java.io.IOException;

import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class UrlHashKey implements IdentifiedDataSerializable, PartitionAware<Integer> {
	
	private String url;
	
	public UrlHashKey() {
		//
	}
	
	public UrlHashKey(String url) {
		this.url = url;
	}

	@Override
	public Integer getPartitionKey() {
		return url.hashCode();
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_UrlHashKey;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		url = in.readUTF();		
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(url);
	}

}
