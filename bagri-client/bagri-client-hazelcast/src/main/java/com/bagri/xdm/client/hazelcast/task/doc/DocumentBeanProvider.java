package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentBeanTask;

import java.util.concurrent.Callable;

public class DocumentBeanProvider extends DocumentAwareTask implements Callable<Object> {
	
	public DocumentBeanProvider() {
		super();
	}
	
	public DocumentBeanProvider(String clientId, String uri) {
		super(clientId, 0, uri, null);
	}

	@Override
	public Object call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentBeanTask;
	}


}
