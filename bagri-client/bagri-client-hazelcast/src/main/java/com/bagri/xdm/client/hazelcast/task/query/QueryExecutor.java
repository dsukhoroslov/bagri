package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ExecQueryTask;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.xdm.api.impl.ResultCursorBase;
import com.bagri.xdm.client.hazelcast.task.QueryAwareTask;

public class QueryExecutor extends QueryAwareTask implements Callable<ResultCursorBase> {

	public QueryExecutor() {
		super();
	}
	
	public QueryExecutor(String clientId, long txId, String query, Map<String, Object> params, Properties context) {
		super(clientId, txId, query, params, context);
	}

	@Override
	public ResultCursorBase call() throws Exception {
		return null;
	}
	
	@Override
	public int getId() {
		return cli_ExecQueryTask;
	}

}
