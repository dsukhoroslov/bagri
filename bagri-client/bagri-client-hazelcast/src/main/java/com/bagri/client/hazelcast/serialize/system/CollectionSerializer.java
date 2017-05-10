package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.system.Collection;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class CollectionSerializer extends EntitySerializer implements StreamSerializer<Collection> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMCollection;
	}

	@Override
	public Collection read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		Collection xCollection = new Collection(
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
	public void write(ObjectDataOutput out, Collection xCollection) throws IOException {
		super.writeEntity(out, xCollection);
		out.writeInt(xCollection.getId());
		out.writeUTF(xCollection.getName());
		out.writeUTF(xCollection.getDocumentType());
		out.writeUTF(xCollection.getDescription());
		out.writeBoolean(xCollection.isEnabled());
	}




}
