package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentMapTask;

import java.util.Map;
import java.util.concurrent.Callable;

public class DocumentMapProvider extends DocumentAwareTask implements Callable<Map<String, Object>> {
	
	public DocumentMapProvider() {
		super();
	}
	
	public DocumentMapProvider(String clientId, String uri) {
		super(clientId, 0, uri, null);
	}

	@Override
	public Map<String, Object> call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentMapTask;
	}


}
