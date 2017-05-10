package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.system.Cardinality;
import com.bagri.core.system.DataType;
import com.bagri.core.system.Parameter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class ParameterSerializer implements StreamSerializer<Parameter> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMParameter;
	}

	@Override
	public Parameter read(ObjectDataInput in) throws IOException {
		Parameter xParam = new Parameter(
				in.readUTF(),
				in.readUTF(),
				Cardinality.values()[in.readInt()]);
		return xParam;
	}

	@Override
	public void write(ObjectDataOutput out, Parameter xParam) throws IOException {
		out.writeUTF(xParam.getName());
		out.writeUTF(xParam.getType());
		out.writeInt(xParam.getCardinality().ordinal());
	}

}

