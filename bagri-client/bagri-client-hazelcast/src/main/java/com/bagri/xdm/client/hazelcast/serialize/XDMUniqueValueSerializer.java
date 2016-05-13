package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.xdm.domain.XDMUniqueValue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMUniqueValueSerializer implements StreamSerializer<XDMUniqueValue> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMUniqueValue;
	}

	@Override
	public XDMUniqueValue read(ObjectDataInput in) throws IOException {
		return new XDMUniqueValue(in.readLong(), in.readLong(), in.readLong());
	}

	@Override
	public void write(ObjectDataOutput out, XDMUniqueValue xValue) throws IOException {
		out.writeLong(xValue.getDocumentKey());
		out.writeLong(xValue.getTxStart());
		out.writeLong(xValue.getTxFinish());
	}




}
