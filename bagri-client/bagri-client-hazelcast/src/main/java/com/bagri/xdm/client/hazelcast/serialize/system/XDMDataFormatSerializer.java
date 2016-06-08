package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.system.XDMDataFormat;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMDataFormatSerializer extends XDMEntitySerializer implements StreamSerializer<XDMDataFormat> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMDataFormat;
	}

	@Override
	public XDMDataFormat read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		return new XDMDataFormat(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				(Collection<String>) in.readObject(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean(),
				(Properties) in.readObject());
	}

	@Override
	public void write(ObjectDataOutput out, XDMDataFormat xFormat)	throws IOException {
		super.writeEntity(out, xFormat);
		out.writeUTF(xFormat.getName());
		out.writeUTF(xFormat.getDescription());
		out.writeObject(xFormat.getExtensions());
		out.writeUTF(xFormat.getParserClass());
		out.writeUTF(xFormat.getBuilderClass());
		out.writeBoolean(xFormat.isEnabled());
		out.writeObject(xFormat.getProperties());
	}


}
