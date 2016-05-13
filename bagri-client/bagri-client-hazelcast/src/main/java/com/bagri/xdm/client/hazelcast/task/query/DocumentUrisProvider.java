package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentUrisTask;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.xdm.client.hazelcast.task.QueryAwareTask;

public class DocumentUrisProvider extends QueryAwareTask implements Callable<Collection<String>> {
	
	public DocumentUrisProvider() {
		super();
	}
	
	public DocumentUrisProvider(String clientId, long txId, String query, Map params, Properties props) {
		super(clientId, txId, query, params, props);
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentUrisTask;
	}

	@Override
	public Collection<String> call() throws Exception {
		return null;
	}

}
