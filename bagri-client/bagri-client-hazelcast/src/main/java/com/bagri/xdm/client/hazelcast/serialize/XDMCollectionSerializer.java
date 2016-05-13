package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Date;

import com.bagri.xdm.system.XDMCollection;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMCollectionSerializer extends XDMEntitySerializer implements StreamSerializer<XDMCollection> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMCollection;
	}

	@Override
	public XDMCollection read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMCollection xCollection = new XDMCollection(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readInt(),
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean());
		return xCollection;
	}

	@Override
	public void write(ObjectDataOutput out, XDMCollection xCollection) throws IOException {
		super.writeEntity(out, xCollection);
		out.writeInt(xCollection.getId());
		out.writeUTF(xCollection.getName());
		out.writeUTF(xCollection.getDocumentType());
		out.writeUTF(xCollection.getDescription());
		out.writeBoolean(xCollection.isEnabled());
	}




}
