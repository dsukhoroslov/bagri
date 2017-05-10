package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.system.TriggerAction;
import com.bagri.core.system.XQueryTrigger;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XQueryTriggerSerializer extends TriggerDefinitionSerializer implements StreamSerializer<XQueryTrigger> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMXQueryTrigger;
	}

	@Override
	@SuppressWarnings("unchecked")
	public XQueryTrigger read(ObjectDataInput in) throws IOException {
		Object[] fields = super.readTrigger(in);
		String module = in.readUTF();
		String function = in.readUTF();
		XQueryTrigger xTrigger = new XQueryTrigger((int) fields[0], (Date) fields[1], 
				(String) fields[2],	module, function, (String) fields[3], (Boolean) fields[4], 
				(Boolean) fields[5], (Integer) fields[6]);
		xTrigger.setActions((Collection<TriggerAction>) fields[7]);
		return xTrigger; 
	}

	@Override
	public void write(ObjectDataOutput out, XQueryTrigger xTrigger) throws IOException {
		super.writeTrigger(out, xTrigger);
		out.writeUTF(xTrigger.getModule());
		out.writeUTF(xTrigger.getFunction());
	}

}
