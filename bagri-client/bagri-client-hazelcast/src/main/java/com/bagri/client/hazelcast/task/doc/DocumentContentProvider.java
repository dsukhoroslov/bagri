package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_ProvideDocumentContentTask;

import java.util.Properties;

@SuppressWarnings("serial")
public class DocumentContentProvider extends DocumentProcessor { 
	
	public DocumentContentProvider() {
		super();
	}
	
	public DocumentContentProvider(String clientId, long txId, String uri, Properties props) {
		super(clientId, txId, uri, props);
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentContentTask;
	}

}
