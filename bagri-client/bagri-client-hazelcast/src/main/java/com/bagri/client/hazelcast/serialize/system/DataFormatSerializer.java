package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.system.DataFormat;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class DataFormatSerializer extends EntitySerializer implements StreamSerializer<DataFormat> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMDataFormat;
	}

	@Override
	public DataFormat read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		return new DataFormat(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				(Collection<String>) in.readObject(),
				in.readUTF(),
				in.readBoolean(),
				(Properties) in.readObject());
	}

	@Override
	public void write(ObjectDataOutput out, DataFormat xFormat)	throws IOException {
		super.writeEntity(out, xFormat);
		out.writeUTF(xFormat.getName());
		out.writeUTF(xFormat.getDescription());
		out.writeUTF(xFormat.getType());
		out.writeObject(xFormat.getExtensions());
		out.writeUTF(xFormat.getHandlerClass());
		out.writeBoolean(xFormat.isEnabled());
		out.writeObject(xFormat.getProperties());
	}


}
