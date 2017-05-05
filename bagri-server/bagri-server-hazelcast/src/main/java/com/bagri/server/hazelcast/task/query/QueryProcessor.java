package com.bagri.server.hazelcast.task.query;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProcessQueryTask;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.core.DataKey;
import com.bagri.core.model.Elements;
import com.bagri.core.query.PathExpression;
import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

@SuppressWarnings("serial")
public class QueryProcessor implements EntryProcessor<DataKey, Elements>, ReadOnly, IdentifiedDataSerializable { 
	
	//private static final transient Logger logger = LoggerFactory.getLogger(QueryProcessor.class);
	//private Set<String> threads = new HashSet<String>();
	
	private PathExpression pex;
	private Object value;
	
	public QueryProcessor() {
		// for de-serialization
	}
	
	public QueryProcessor(PathExpression pex, Object value) {
		this.pex = pex;
		this.value = value;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	@Override
	public int getId() {
		return cli_ProcessQueryTask;
	}

	@Override
	public EntryBackupProcessor getBackupProcessor() {
		return null;
	}

	@Override
	public Object process(Entry<DataKey, Elements> entry) {
		if (entry.getValue().apply(pex, value)) {
			return true;
		}
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		pex = in.readObject();
		value = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(pex);
		out.writeObject(value);
	}

}
