package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideQueryUrisTask;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.xdm.client.hazelcast.task.QueryAwareTask;

public class QueryUrisProvider extends QueryAwareTask implements Callable<Collection<String>> {
	
	public QueryUrisProvider() {
		super();
	}
	
	public QueryUrisProvider(String clientId, long txId, String query, Map params, Properties props) {
		super(clientId, txId, query, params, props);
	}

	@Override
	public int getId() {
		return cli_ProvideQueryUrisTask;
	}

	@Override
	public Collection<String> call() throws Exception {
		return null;
	}

}
