package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.xdm.system.XDMCardinality;
import com.bagri.xdm.system.XDMParameter;
import com.bagri.xdm.system.XDMType;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMParameterSerializer implements StreamSerializer<XDMParameter> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMParameter;
	}

	@Override
	public XDMParameter read(ObjectDataInput in) throws IOException {
		XDMParameter xParam = new XDMParameter(
				in.readUTF(),
				in.readUTF(),
				XDMCardinality.values()[in.readInt()]);
		return xParam;
	}

	@Override
	public void write(ObjectDataOutput out, XDMParameter xParam) throws IOException {
		out.writeUTF(xParam.getName());
		out.writeUTF(xParam.getType());
		out.writeInt(xParam.getCardinality().ordinal());
	}

}

