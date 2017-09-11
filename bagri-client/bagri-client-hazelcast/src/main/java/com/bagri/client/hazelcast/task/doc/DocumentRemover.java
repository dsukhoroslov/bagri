package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_RemoveDocumentTask;

import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.core.api.DocumentAccessor;

public class DocumentRemover extends DocumentAwareTask implements Callable<DocumentAccessor> {

	public DocumentRemover() {
		super();
	}

	public DocumentRemover(String clientId, long txId, String uri, Properties props) {
		super(clientId, txId, uri, props);
	}

	@Override
	public int getId() {
		return cli_RemoveDocumentTask;
	}

	@Override
	public DocumentAccessor call() throws Exception {
		return null;
	}

}
