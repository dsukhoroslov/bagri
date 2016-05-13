package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.xdm.domain.XDMCounter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMCounterSerializer implements StreamSerializer<XDMCounter> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMCounter;
	}

	@Override
	public XDMCounter read(ObjectDataInput in) throws IOException {
		
		return new XDMCounter(
				in.readBoolean(),
				in.readInt(),
				in.readInt(),
				in.readInt());
	}

	@Override
	public void write(ObjectDataOutput out, XDMCounter xCnt) throws IOException {
		
		out.writeBoolean(xCnt.isCommit());
		out.writeInt(xCnt.getCreated());
		out.writeInt(xCnt.getUpdated());
		out.writeInt(xCnt.getDeleted());
	}

}
