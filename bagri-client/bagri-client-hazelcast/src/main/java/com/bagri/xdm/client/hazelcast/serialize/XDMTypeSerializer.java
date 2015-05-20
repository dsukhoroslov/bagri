package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.xdm.system.XDMCardinality;
import com.bagri.xdm.system.XDMType;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMTypeSerializer implements StreamSerializer<XDMType> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMType;
	}

	@Override
	public XDMType read(ObjectDataInput in) throws IOException {
		XDMType xType = new XDMType(in.readUTF(),
				XDMCardinality.valueOf(in.readUTF()));
		return xType;
	}

	@Override
	public void write(ObjectDataOutput out, XDMType xType) throws IOException {
		out.writeUTF(xType.getType());
		out.writeUTF(xType.getCardinality().name());
	}


}
