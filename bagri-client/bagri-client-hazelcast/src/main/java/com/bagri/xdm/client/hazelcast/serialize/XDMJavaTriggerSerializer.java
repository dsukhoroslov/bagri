package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import com.bagri.xdm.system.XDMJavaTrigger;
import com.bagri.xdm.system.XDMTriggerAction;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMJavaTriggerSerializer extends XDMTriggerDefSerializer implements StreamSerializer<XDMJavaTrigger> {

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMJavaTrigger;
	}

	@Override
	@SuppressWarnings("unchecked")
	public XDMJavaTrigger read(ObjectDataInput in) throws IOException {
		Object[] fields = super.readTrigger(in);
		String library = in.readUTF();
		String className = in.readUTF();
		XDMJavaTrigger xTrigger = new XDMJavaTrigger((int) fields[0], (Date) fields[1], (String) fields[2],
				library, className, (String) fields[3], (Boolean) fields[4], (Boolean) fields[5]);
		xTrigger.setActions((Collection<XDMTriggerAction>) fields[6]);
		return xTrigger; 
	}

	@Override
	public void write(ObjectDataOutput out, XDMJavaTrigger xTrigger) throws IOException {
		super.writeTrigger(out, xTrigger);
		out.writeUTF(xTrigger.getLibrary());
		out.writeUTF(xTrigger.getClassName());
	}

}
