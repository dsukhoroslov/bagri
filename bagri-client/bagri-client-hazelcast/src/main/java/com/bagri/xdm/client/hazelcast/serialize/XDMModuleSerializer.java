package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Date;

import com.bagri.xdm.system.XDMModule;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMModuleSerializer extends XDMEntitySerializer implements StreamSerializer<XDMModule> {

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMModule;
	}

	@Override
	public XDMModule read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMModule xModule = new XDMModule(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean());
		return xModule;
	}

	@Override
	public void write(ObjectDataOutput out, XDMModule xModule) throws IOException {
		
		super.writeEntity(out, xModule);
		out.writeUTF(xModule.getName());
		out.writeUTF(xModule.getFileName());
		out.writeUTF(xModule.getDescription());
		out.writeUTF(xModule.getNamespace());
		out.writeUTF(xModule.getBody());
		out.writeBoolean(xModule.isEnabled());
	}



}
