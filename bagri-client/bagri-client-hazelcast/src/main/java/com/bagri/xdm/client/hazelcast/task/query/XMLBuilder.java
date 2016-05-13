package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_BuildQueryXMLTask;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.bagri.common.query.ExpressionContainer;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class XMLBuilder extends ResultBuilder implements Callable<Collection<String>> {
	
	protected String template;
	protected Map<String, String> params = new HashMap<String, String>();
	
	public XMLBuilder() {
		super();
	}
	
	public XMLBuilder(String clientId, long txId, ExpressionContainer exp, String template, Map<String, String> params) {
		super(clientId, txId, exp);
		this.template = template;
		if (params != null) {
			this.params.putAll(params);
		}
	}
	
	@Override
	public int getId() {
		return cli_BuildQueryXMLTask;
	}

	@Override
	public Collection<String> call() throws Exception {
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		template = in.readUTF();
		params = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(template);
		out.writeObject(params);
	}
	
}
