package com.bagri.client.hazelcast.task.query;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_ProvideQueryUrisTask;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.QueryAwareTask;
import com.bagri.core.api.ResultCursor;

public class QueryUrisProvider extends QueryAwareTask implements Callable<ResultCursor<String>> {
	
	public QueryUrisProvider() {
		super();
	}
	
	public QueryUrisProvider(String clientId, long txId, String query, Map<String, Object> params, Properties props) {
		super(clientId, txId, query, params, props);
	}

	@Override
	public int getId() {
		return cli_ProvideQueryUrisTask;
	}

	@Override
	public ResultCursor<String> call() throws Exception {
		return null;
	}

}
