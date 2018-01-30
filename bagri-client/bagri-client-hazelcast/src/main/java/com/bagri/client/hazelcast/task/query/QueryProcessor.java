package com.bagri.client.hazelcast.task.query;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_ProcessQueryTask;
import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.client.hazelcast.task.QueryAwareTask;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.model.QueryResult;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class QueryProcessor extends QueryAwareTask implements EntryProcessor<Long, QueryResult>, EntryBackupProcessor<Long, QueryResult> { //, IdentifiedDataSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected boolean readOnly;
	
	public QueryProcessor() {
		super();
	}
	
	public QueryProcessor(String clientId, long txId, String query, Map<String, Object> params, Properties context, boolean readOnly) {
		super(clientId, txId, query, params, context);
		this.readOnly = readOnly;
	}

	@Override
	public void processBackup(Entry<Long, QueryResult> entry) {
		// do it on server side..
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
		super.readData(in);
		readOnly = in.readBoolean();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(readOnly);
	}
	
}
