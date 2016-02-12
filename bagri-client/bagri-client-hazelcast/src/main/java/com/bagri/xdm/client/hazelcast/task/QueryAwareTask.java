package com.bagri.xdm.client.hazelcast.task;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class QueryAwareTask extends TransactionAwareTask {

	protected String query;
	protected Map<QName, Object> params;
	protected Properties context;
	
	public QueryAwareTask() {
		super();
	}
	
	public QueryAwareTask(String clientId, long txId, String query, Map<QName, Object> params, Properties context) {
		super(clientId, txId);
		this.query = query;
		this.params = params;
		this.context = context;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		query = in.readUTF();
		//params = in.readObject();
		int size = in.readInt();
		params = new HashMap<>(size);
		for (int i=0; i < size; i++) {
			params.put((QName) in.readObject(), in.readObject());
		}
		context = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(query);
		//out.writeObject(params);
		out.writeInt(params.size());
		for (Map.Entry<QName, Object> param: params.entrySet()) {
			out.writeObject(param.getKey());
			out.writeObject(param.getValue());
		}
		out.writeObject(context);
	}

}