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

public class QueryExecutor implements Callable<ResultCursor>, IdentifiedDataSerializable {

	protected String schemaName;
	protected String query;
	protected Map<Object, Object> bindings;
	protected Properties context;
	
	public QueryExecutor() {
		// for de-serialization
	}
	
	public QueryExecutor(String schemaName, String query, Map bindings, Properties context) {
		this.schemaName = schemaName;
		this.query = query;
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
		schemaName = in.readUTF();
		query = in.readUTF();
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
		out.writeUTF(schemaName);
		out.writeUTF(query);
		//out.writeObject(bindings);
		out.writeInt(bindings.size());
		for (Map.Entry<Object, Object> bind: bindings.entrySet()) {
			out.writeObject(bind.getKey());
			out.writeObject(bind.getValue());
		}
		out.writeObject(context);
	}
}
