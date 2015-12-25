package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ExecQueryTask;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.client.hazelcast.task.QueryAwareTask;

public class QueryExecutor extends QueryAwareTask implements Callable<ResultCursor> {

	public QueryExecutor() {
		super();
	}
	
	public QueryExecutor(String clientId, long txId, String query, Map bindings, Properties context) {
		super(clientId, txId, query, bindings, context);
	}

	@Override
	public ResultCursor call() throws Exception {
		return null;
	}
	
	@Override
	public int getId() {
		return cli_ExecQueryTask;
	}

}
