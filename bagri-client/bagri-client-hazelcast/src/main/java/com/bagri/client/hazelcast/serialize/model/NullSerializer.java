package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.model.Null;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class NullSerializer implements StreamSerializer<Null> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMNull;
	}

	@Override
	public Null read(ObjectDataInput in) throws IOException {
		return Null._null;
	}

	@Override
	public void write(ObjectDataOutput out, Null nul) throws IOException {
	}

}
