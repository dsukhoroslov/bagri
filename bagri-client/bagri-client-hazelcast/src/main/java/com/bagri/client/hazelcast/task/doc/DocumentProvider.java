package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_GetDocumentTask;

import java.util.Properties;

import com.bagri.core.api.DocumentDistributionStrategy;

public class DocumentProvider extends DocumentProcessor { 
	
	public DocumentProvider() {
		super();
	}
	
	public DocumentProvider(String clientId, long txId, Properties props, String uri, DocumentDistributionStrategy distributor) {
		super(clientId, txId, props, uri, distributor);
	}

	@Override
	public int getId() {
		return cli_GetDocumentTask;
	}

}
