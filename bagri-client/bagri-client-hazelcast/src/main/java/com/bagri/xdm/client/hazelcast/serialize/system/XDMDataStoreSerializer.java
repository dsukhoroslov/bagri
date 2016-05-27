package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.system.XDMDataStore;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMDataStoreSerializer extends XDMEntitySerializer implements StreamSerializer<XDMDataStore> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMDataStore;
	}

	@Override
	public XDMDataStore read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		return new XDMDataStore(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean(),
				(Properties) in.readObject());
	}

	@Override
	public void write(ObjectDataOutput out, XDMDataStore xStore)	throws IOException {
		super.writeEntity(out, xStore);
		out.writeUTF(xStore.getName());
		out.writeUTF(xStore.getLibrary());
		out.writeUTF(xStore.getDescription());
		out.writeUTF(xStore.getStoreClass());
		out.writeBoolean(xStore.isEnabled());
		out.writeObject(xStore.getProperties());
	}

}
