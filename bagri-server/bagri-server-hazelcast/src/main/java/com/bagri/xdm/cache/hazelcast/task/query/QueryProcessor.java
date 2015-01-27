package com.bagri.xdm.cache.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProcessQueryTask;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.common.query.PathExpression;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMElements;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class QueryProcessor implements EntryProcessor<XDMDataKey, XDMElements>, IdentifiedDataSerializable { 
	
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
	public Object process(Entry<XDMDataKey, XDMElements> entry) {
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
