package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.system.Resource;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class ResourceSerializer extends EntitySerializer implements StreamSerializer<Resource> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMResource;
	}

	@Override
	public Resource read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		Resource xResource = new Resource(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean());
		return xResource;
	}

	@Override
	public void write(ObjectDataOutput out, Resource xResource) throws IOException {
		super.writeEntity(out, xResource);
		out.writeUTF(xResource.getName());
		out.writeUTF(xResource.getPath());
		out.writeUTF(xResource.getDescription());
		out.writeUTF(xResource.getModule());
		out.writeBoolean(xResource.isEnabled());
	}

}
