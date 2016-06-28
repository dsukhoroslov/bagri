package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_RemoveDocumentTask;

import java.util.concurrent.Callable;

import com.bagri.xdm.domain.Document;

public class DocumentRemover extends DocumentAwareTask implements Callable<Document> {

	public DocumentRemover() {
		super();
	}

	public DocumentRemover(String clientId, long txId, String uri) {
		super(clientId, txId, uri, null);
	}

	@Override
	public int getId() {
		return cli_RemoveDocumentTask;
	}

	@Override
	public Document call() throws Exception {
		return null;
	}

}
