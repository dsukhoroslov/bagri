package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_RemoveDocumentTask;

import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.DocumentDistributionStrategy;

public class DocumentRemover extends DocumentAwareTask implements Callable<DocumentAccessor> {

	public DocumentRemover() {
		super();
	}

	public DocumentRemover(String clientId, long txId, Properties props, String uri, DocumentDistributionStrategy distributor) {
		super(clientId, txId, props, uri, distributor);
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
