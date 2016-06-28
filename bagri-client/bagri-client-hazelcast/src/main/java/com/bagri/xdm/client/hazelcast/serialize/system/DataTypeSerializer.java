package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.system.Cardinality;
import com.bagri.xdm.system.DataType;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class DataTypeSerializer implements StreamSerializer<DataType> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMType;
	}

	@Override
	public DataType read(ObjectDataInput in) throws IOException {
		return new DataType(in.readUTF(),	Cardinality.values()[in.readInt()]);
	}

	@Override
	public void write(ObjectDataOutput out, DataType xType) throws IOException {
		out.writeUTF(xType.getType());
		out.writeInt(xType.getCardinality().ordinal());
	}


}
