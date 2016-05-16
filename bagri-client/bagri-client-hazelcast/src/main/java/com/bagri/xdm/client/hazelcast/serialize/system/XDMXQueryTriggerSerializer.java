package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.system.XDMTriggerAction;
import com.bagri.xdm.system.XDMXQueryTrigger;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMXQueryTriggerSerializer extends XDMTriggerDefSerializer implements StreamSerializer<XDMXQueryTrigger> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMXQueryTrigger;
	}

	@Override
	@SuppressWarnings("unchecked")
	public XDMXQueryTrigger read(ObjectDataInput in) throws IOException {
		Object[] fields = super.readTrigger(in);
		String module = in.readUTF();
		String function = in.readUTF();
		XDMXQueryTrigger xTrigger = new XDMXQueryTrigger((int) fields[0], (Date) fields[1], 
				(String) fields[2],	module, function, (String) fields[3], (Boolean) fields[4], 
				(Boolean) fields[5], (Integer) fields[6]);
		xTrigger.setActions((Collection<XDMTriggerAction>) fields[7]);
		return xTrigger; 
	}

	@Override
	public void write(ObjectDataOutput out, XDMXQueryTrigger xTrigger) throws IOException {
		super.writeTrigger(out, xTrigger);
		out.writeUTF(xTrigger.getModule());
		out.writeUTF(xTrigger.getFunction());
	}

}
