package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_XDMExecCommandTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class CommandExecutor implements Callable<Object>, IdentifiedDataSerializable {

	protected String command;
	protected Map bindings;
	protected Properties context;
	
	public CommandExecutor() {
		// for de-serialization
	}
	
	public CommandExecutor(String command, Map bindings, Properties context) {
		this.command = command;
		this.bindings = bindings;
		this.context = context;
	}

	@Override
	public Object call() throws Exception {
		
		return null;
	}
	
	@Override
	public int getFactoryId() {
		
		return factoryId;
	}
	
	@Override
	public int getId() {
		
		return cli_XDMExecCommandTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		command = in.readUTF();
		bindings = in.readObject();
		context = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(command);
		out.writeObject(bindings);
		out.writeObject(context);
	}

}