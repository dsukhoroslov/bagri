package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bagri.xdm.system.XDMTriggerAction;
import com.bagri.xdm.system.XDMTriggerDef;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class XDMTriggerDefSerializer extends XDMEntitySerializer { 

	public Object[] readTrigger(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		Object[] trigger = new Object[8];
		trigger[0] = entity[0];
		trigger[1] = entity[1];
		trigger[2] = entity[2];
		trigger[3] = in.readUTF();
		trigger[4] = in.readBoolean();
		trigger[5] = in.readBoolean();
		trigger[6] = in.readInt();
		int size = in.readInt();
		List<XDMTriggerAction> actions = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			XDMTriggerAction xAction = in.readObject();
			actions.add(xAction);
		}
		trigger[7] = actions;
		return trigger;
	}

	public void writeTrigger(ObjectDataOutput out, XDMTriggerDef xTrigger) throws IOException {
		
		super.writeEntity(out, xTrigger);
		out.writeUTF(xTrigger.getDocType());
		out.writeBoolean(xTrigger.isSynchronous());
		out.writeBoolean(xTrigger.isEnabled());
		out.writeInt(xTrigger.getIndex());
		out.writeInt(xTrigger.getActions().size());
		for (XDMTriggerAction xAction: xTrigger.getActions()) {
			out.writeObject(xAction);
		}
	}

}
