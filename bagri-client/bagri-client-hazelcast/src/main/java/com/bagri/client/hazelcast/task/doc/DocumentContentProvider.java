package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentContentTask;

import java.util.Properties;
import java.util.concurrent.Callable;

public class DocumentContentProvider extends DocumentAwareTask implements Callable<String> {
	
	public DocumentContentProvider() {
		super();
	}
	
	public DocumentContentProvider(String clientId, String uri, Properties props) {
		super(clientId, 0, uri, props);
	}

	@Override
	public String call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentContentTask;
	}

}
