package com.bagri.xdm.client.hazelcast.task;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class QueryAwareTask extends TransactionAwareTask {

	protected String query;
	protected Map<Object, Object> bindings;
	protected Properties context;
	
	public QueryAwareTask() {
		super();
	}
	
	public QueryAwareTask(String clientId, long txId, String query, Map bindings, Properties context) {
		this.query = query;
		this.bindings = bindings;
		this.context = context;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
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
		super.writeData(out);
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