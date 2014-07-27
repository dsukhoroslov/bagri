package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_XDMExecQueryTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.xml.xquery.XQStaticContext;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class QueryExecutor implements Callable<Object>, IdentifiedDataSerializable {

	protected String query;
	protected Map bindings;
	protected Properties context;
	
	public QueryExecutor() {
		// for de-serialization
	}
	
	public QueryExecutor(String query, Map bindings, Properties context) {
		this.query = query;
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
		
		return cli_XDMExecQueryTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		query = in.readUTF();
		bindings = in.readObject();
		context = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(query);
		out.writeObject(bindings);
		out.writeObject(context);
	}
}
