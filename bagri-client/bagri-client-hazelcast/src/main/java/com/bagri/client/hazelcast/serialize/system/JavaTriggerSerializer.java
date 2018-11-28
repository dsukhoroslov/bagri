package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.system.JavaTrigger;
import com.bagri.core.system.TriggerAction;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class JavaTriggerSerializer extends TriggerDefinitionSerializer implements StreamSerializer<JavaTrigger> {

	@Override
	public int getTypeId() {
		return DomainSerializationFactory.cli_JavaTrigger;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JavaTrigger read(ObjectDataInput in) throws IOException {
		Object[] fields = super.readTrigger(in);
		String library = in.readUTF();
		String className = in.readUTF();
		JavaTrigger xTrigger = new JavaTrigger((int) fields[0], (Date) fields[1], (String) fields[2],
				library, className, (Boolean) fields[3], (Boolean) fields[4], (String) fields[5]);
		xTrigger.setActions((Collection<TriggerAction>) fields[6]);
		return xTrigger; 
	}

	@Override
	public void write(ObjectDataOutput out, JavaTrigger xTrigger) throws IOException {
		super.writeTrigger(out, xTrigger);
		out.writeUTF(xTrigger.getLibrary());
		out.writeUTF(xTrigger.getClassName());
	}

}
