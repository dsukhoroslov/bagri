package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.system.Module;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class ModuleSerializer extends EntitySerializer implements StreamSerializer<Module> {

	@Override
	public int getTypeId() {
		return DomainSerializationFactory.cli_Module;
	}

	@Override
	public Module read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		Module xModule = new Module(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean());
		return xModule;
	}

	@Override
	public void write(ObjectDataOutput out, Module xModule) throws IOException {
		
		super.writeEntity(out, xModule);
		out.writeUTF(xModule.getName());
		out.writeUTF(xModule.getFileName());
		out.writeUTF(xModule.getDescription());
		out.writeUTF(xModule.getPrefix());
		out.writeUTF(xModule.getNamespace());
		out.writeUTF(xModule.getBody());
		out.writeBoolean(xModule.isEnabled());
	}

}
