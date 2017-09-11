package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_DocumentAccessor;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.api.impl.DocumentAccessorBase;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DocumentAccessorImpl extends DocumentAccessorBase implements IdentifiedDataSerializable {
	
	private Map<String, Object> headers = new HashMap<>();
	
	public DocumentAccessorImpl() {
		//
	}

	@Override
	public String getUri() {
		return (String) headers.get(HDR_URI);
	}

	@Override
	public int getVersion() {
		return (Integer) headers.get(HDR_VERSION);
	}

	@Override
	public Map<String, Object> getHeaders() {
		return headers;
	}

	@Override
	public <T> T getHeader(String hName) {
		return (T) headers.get(hName);
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_DocumentAccessor;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		for (int i=0; i < size; i++) {
			headers.put(in.readUTF(), in.readObject());
		}
		content = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(headers.size());
		for (Map.Entry<String, Object> entry: headers.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeObject(entry.getValue());
		}
		out.writeObject(content);
	}

}
