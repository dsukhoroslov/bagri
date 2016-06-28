package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_GetDocumentTask;

import java.util.concurrent.Callable;

import com.bagri.xdm.domain.Document;

public class DocumentProvider extends DocumentAwareTask implements Callable<Document> {
	
	public DocumentProvider() {
		super();
	}
	
	public DocumentProvider(String clientId, String uri) {
		super(clientId, 0, uri, null);
	}

	@Override
	public Document call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_GetDocumentTask;
	}



}
