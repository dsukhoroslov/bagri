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
		return DataSerializationFactoryImpl.cli_XDMTriggerAction;
	}

	@Override
	public XDMTriggerAction read(ObjectDataInput in) throws IOException {
		XDMTriggerAction xAction = new XDMTriggerAction(
				Action.values()[in.readInt()],
				Scope.values()[in.readInt()]);
		return xAction;
	}

	@Override
	public void write(ObjectDataOutput out, XDMTriggerAction xAction) throws IOException {
		out.writeInt(xAction.getAction().ordinal());
		out.writeInt(xAction.getScope().ordinal());
	}


}
