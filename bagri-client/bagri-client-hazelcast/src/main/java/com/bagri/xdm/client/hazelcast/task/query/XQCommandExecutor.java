package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ExecXQCommandTask;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XQCommandExecutor implements Callable<ResultCursor>, IdentifiedDataSerializable {

	protected boolean isQuery;
	protected String schemaName;
	protected String command;
	protected Map<Object, Object> bindings;
	protected Properties context;
	
	public XQCommandExecutor() {
		// for de-serialization
	}
	
	public XQCommandExecutor(boolean isQuery, String schemaName, String command, 
			Map bindings, Properties context) {
		this.isQuery = isQuery;
		this.schemaName = schemaName;
		this.command = command;
		this.bindings = bindings;
		this.context = context;
	}

	@Override
	public ResultCursor call() throws Exception {
		return null;
	}
	
	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	@Override
	public int getId() {
		return cli_ExecXQCommandTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		isQuery = in.readBoolean();
		schemaName = in.readUTF();
		command = in.readUTF();
		//bindings = in.readObject();
		int size = in.readInt();
		bindings = new HashMap<Object, Object>(size);
		for (int i=0; i < size; i++) {
			bindings.put(in.readObject(), in.readObject());
		}
		context = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeBoolean(isQuery);
		out.writeUTF(schemaName);
		out.writeUTF(command);
		//out.writeObject(bindings);
		out.writeInt(bindings.size());
		for (Map.Entry<Object, Object> bind: bindings.entrySet()) {
			out.writeObject(bind.getKey());
			out.writeObject(bind.getValue());
		}
		out.writeObject(context);
	}
}
