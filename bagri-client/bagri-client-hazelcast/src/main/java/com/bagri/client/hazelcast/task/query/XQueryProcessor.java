package com.bagri.client.hazelcast.task.query;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProcessXQCommandTask;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.core.model.QueryResult;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XQueryProcessor implements EntryProcessor<Long, QueryResult>, EntryBackupProcessor<Long, QueryResult>,
	IdentifiedDataSerializable {

	protected boolean isQuery;
	protected String schemaName;
	protected String command;
	protected Map<Object, Object> bindings;
	protected Properties context;
	
	public XQueryProcessor() {
		// for de-serialization
	}
	
	public XQueryProcessor(boolean isQuery, String schemaName, String command, 
			Map bindings, Properties context) {
		this.isQuery = isQuery;
		this.schemaName = schemaName;
		this.command = command;
		this.bindings = bindings;
		this.context = context;
	}

	
	@Override
	public void processBackup(Entry<Long, QueryResult> entry) {
		// TODO Auto-generated method stub
	}

	@Override
	public Object process(Entry<Long, QueryResult> entry) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntryBackupProcessor<Long, QueryResult> getBackupProcessor() {
		return this;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_ProcessXQCommandTask;
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
