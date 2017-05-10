package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.system.DataStore;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class DataStoreSerializer extends EntitySerializer implements StreamSerializer<DataStore> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMDataStore;
	}

	@Override
	public DataStore read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		return new DataStore(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean(),
				(Properties) in.readObject());
	}

	@Override
	public void write(ObjectDataOutput out, DataStore xStore)	throws IOException {
		super.writeEntity(out, xStore);
		out.writeUTF(xStore.getName());
		out.writeUTF(xStore.getDescription());
		out.writeUTF(xStore.getStoreClass());
		out.writeBoolean(xStore.isEnabled());
		out.writeObject(xStore.getProperties());
	}

}
