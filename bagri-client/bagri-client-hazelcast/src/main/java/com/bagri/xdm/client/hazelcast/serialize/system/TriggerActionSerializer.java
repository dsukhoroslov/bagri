package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.system.TriggerAction;
import com.bagri.xdm.system.TriggerAction.Order;
import com.bagri.xdm.system.TriggerAction.Scope;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class TriggerActionSerializer implements StreamSerializer<TriggerAction> {

	@Override
	public void destroy() {
		// no-op
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMTriggerAction;
	}

	@Override
	public TriggerAction read(ObjectDataInput in) throws IOException {
		TriggerAction xAction = new TriggerAction(
				Order.values()[in.readInt()],
				Scope.values()[in.readInt()]);
		return xAction;
	}

	@Override
	public void write(ObjectDataOutput out, TriggerAction xAction) throws IOException {
		out.writeInt(xAction.getOrder().ordinal());
		out.writeInt(xAction.getScope().ordinal());
	}


}
