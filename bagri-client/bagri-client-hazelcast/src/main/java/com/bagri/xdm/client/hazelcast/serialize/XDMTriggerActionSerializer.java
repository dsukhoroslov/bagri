package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.xdm.system.XDMTriggerAction;
import com.bagri.xdm.system.XDMTriggerAction.Action;
import com.bagri.xdm.system.XDMTriggerAction.Scope;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMTriggerActionSerializer implements StreamSerializer<XDMTriggerAction> {

	@Override
	public void destroy() {
		// no-op
	}

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMTriggerAction;
	}

	@Override
	public XDMTriggerAction read(ObjectDataInput in) throws IOException {
		XDMTriggerAction xAction = new XDMTriggerAction(
				Action.valueOf(in.readUTF()),
				Scope.valueOf(in.readUTF()));
		return xAction;
	}

	@Override
	public void write(ObjectDataOutput out, XDMTriggerAction xAction) throws IOException {
		out.writeUTF(xAction.getAction().name());
		out.writeUTF(xAction.getScope().name());
	}


}
