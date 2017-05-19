package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_GetDocumentTask;

import java.util.Properties;

public class DocumentProvider extends DocumentProcessor { 
	
	public DocumentProvider() {
		super();
	}
	
	public DocumentProvider(String clientId, long txId, String uri, Properties props) {
		super(clientId, txId, uri, props);
	}

	@Override
	public int getId() {
		return cli_GetDocumentTask;
	}



}
