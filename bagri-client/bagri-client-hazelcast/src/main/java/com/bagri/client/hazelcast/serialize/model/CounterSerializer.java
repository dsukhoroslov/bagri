package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.model.Counter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class CounterSerializer implements StreamSerializer<Counter> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMCounter;
	}

	@Override
	public Counter read(ObjectDataInput in) throws IOException {
		
		return new Counter(
				in.readBoolean(),
				in.readInt(),
				in.readInt(),
				in.readInt());
	}

	@Override
	public void write(ObjectDataOutput out, Counter xCnt) throws IOException {
		
		out.writeBoolean(xCnt.isCommit());
		out.writeInt(xCnt.getCreated());
		out.writeInt(xCnt.getUpdated());
		out.writeInt(xCnt.getDeleted());
	}

}
