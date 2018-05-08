package com.bagri.client.hazelcast.task.query;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_ExecQueryTask;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.QueryAwareTask;
import com.bagri.core.api.ResultCursor;

public class QueryExecutor<T> extends QueryAwareTask implements Callable<ResultCursor<T>> {

	public QueryExecutor() {
		super();
	}
	
	public QueryExecutor(String clientId, long txId, String query, Map<String, Object> params, Properties context) {
		super(clientId, txId, query, params, context);
	}

	@Override
	public ResultCursor<T> call() throws Exception {
		return null;
	}
	
	@Override
	public int getId() {
		return cli_ExecQueryTask;
	}

}
