package com.bagri.client.hazelcast.task.query;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_ProcessQueryTask;
import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.HashMap;
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
		if (readOnly) {
			return null;
		}
		return this;
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_ProcessQueryTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		readOnly = in.readBoolean();
		query = in.readUTF();
		int size = in.readInt();
		params = new HashMap<>();
		for (int i=0; i < size; i++) {
			params.put(in.readUTF(), in.readObject());
		}
		props = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeBoolean(readOnly);
		out.writeUTF(query);
		out.writeInt(params.size());
		for (Map.Entry<String, Object> entry: params.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeObject(entry.getValue());
		}
		out.writeObject(props);
	}
	
}
