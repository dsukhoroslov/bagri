package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.model.UniqueValue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class UniqueValueSerializer implements StreamSerializer<UniqueValue> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMUniqueValue;
	}

	@Override
	public UniqueValue read(ObjectDataInput in) throws IOException {
		return new UniqueValue(in.readLong(), in.readLong(), in.readLong());
	}

	@Override
	public void write(ObjectDataOutput out, UniqueValue xValue) throws IOException {
		out.writeLong(xValue.getDocumentKey());
		out.writeLong(xValue.getTxStart());
		out.writeLong(xValue.getTxFinish());
	}




}
