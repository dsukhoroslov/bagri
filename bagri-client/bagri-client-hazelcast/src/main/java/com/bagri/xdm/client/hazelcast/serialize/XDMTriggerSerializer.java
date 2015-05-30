package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Date;

import com.bagri.xdm.system.XDMTriggerDef;
import com.bagri.xdm.system.XDMTriggerDef.Scope;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMTriggerSerializer extends XDMEntitySerializer implements StreamSerializer<XDMTriggerDef> {

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMTrigger;
	}

	@Override
	public XDMTriggerDef read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMTriggerDef xTrigger = new XDMTriggerDef(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				Scope.valueOf(in.readUTF()),
				in.readBoolean());
		return xTrigger;
	}

	@Override
	public void write(ObjectDataOutput out, XDMTriggerDef xTrigger) throws IOException {
		
		super.writeEntity(out, xTrigger);
		out.writeUTF(xTrigger.getLibrary());
		out.writeUTF(xTrigger.getClassName());
		out.writeUTF(xTrigger.getScope().name());
		out.writeBoolean(xTrigger.isEnabled());
	}

}
