package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.system.TriggerAction;
import com.bagri.core.system.TriggerAction.Order;
import com.bagri.core.system.TriggerAction.Scope;
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
		return DomainSerializationFactory.cli_TriggerAction;
	}

	@Override
	public TriggerAction read(ObjectDataInput in) throws IOException {
		TriggerAction xAction = new TriggerAction(
				in.readInt(),
				Order.values()[in.readInt()],
				Scope.values()[in.readInt()]);
		return xAction;
	}

	@Override
	public void write(ObjectDataOutput out, TriggerAction xAction) throws IOException {
		out.writeInt(xAction.getIndex());
		out.writeInt(xAction.getOrder().ordinal());
		out.writeInt(xAction.getScope().ordinal());
	}


}
