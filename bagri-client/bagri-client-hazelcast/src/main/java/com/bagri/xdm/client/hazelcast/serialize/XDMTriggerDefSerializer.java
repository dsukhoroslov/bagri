package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.bagri.xdm.system.XDMTriggerAction;
import com.bagri.xdm.system.XDMTriggerDef;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMTriggerDefSerializer extends XDMEntitySerializer implements StreamSerializer<XDMTriggerDef> {

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMTriggerDef;
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
				in.readUTF(),
				in.readBoolean(),
				in.readBoolean());
		int size = in.readInt();
		List<XDMTriggerAction> actions = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			XDMTriggerAction xAction = in.readObject();
			actions.add(xAction);
		}
		xTrigger.setActions(actions);
		return xTrigger;
	}

	@Override
	public void write(ObjectDataOutput out, XDMTriggerDef xTrigger) throws IOException {
		
		super.writeEntity(out, xTrigger);
		out.writeUTF(xTrigger.getLibrary());
		out.writeUTF(xTrigger.getClassName());
		out.writeUTF(xTrigger.getDocType());
		out.writeBoolean(xTrigger.isSynchronous());
		out.writeBoolean(xTrigger.isEnabled());
		out.writeInt(xTrigger.getActions().size());
		for (XDMTriggerAction xAction: xTrigger.getActions()) {
			out.writeObject(xAction);
		}
	}

}
