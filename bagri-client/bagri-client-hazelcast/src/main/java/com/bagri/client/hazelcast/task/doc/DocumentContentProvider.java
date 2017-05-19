package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentContentTask;

import java.util.Properties;

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
