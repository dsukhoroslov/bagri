package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentMapTask;

import java.util.Properties;

public class DocumentMapProvider extends DocumentProcessor { 
	
	public DocumentMapProvider() {
		super();
	}
	
	public DocumentMapProvider(String clientId, long txId, String uri, Properties props) {
		super(clientId, txId, uri, props);
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentMapTask;
	}


}
