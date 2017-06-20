package com.bagri.client.hazelcast.task.query;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProcessQueryTask;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.model.QueryResult;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class QueryProcessor implements EntryProcessor<Long, QueryResult>, EntryBackupProcessor<Long, QueryResult>, IdentifiedDataSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected boolean readOnly;
	protected String query;
	protected Map<String, Object> params;
	protected Properties props;
	
	public QueryProcessor() {
		// for de-serialization
	}
	
	public QueryProcessor(boolean readOnly, String query, Map<String, Object> params, Properties props) {
		this.readOnly = readOnly;
		this.query = query;
		this.params = params;
		this.props = props;
	}

	@Override
	public void processBackup(Entry<Long, QueryResult> entry) {
	}

	@Override
	public ResultCursor process(Entry<Long, QueryResult> entry) {
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
		return cli_ProcessQueryTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		readOnly = in.readBoolean();
		query = in.readUTF();
		params = in.readObject();
		props = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeBoolean(readOnly);
		out.writeUTF(query);
		out.writeObject(params);
		out.writeObject(props);
	}
	
}
