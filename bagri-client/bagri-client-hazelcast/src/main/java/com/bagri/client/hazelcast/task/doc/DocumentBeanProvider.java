package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentBeanTask;

import java.util.Properties;

public class DocumentBeanProvider extends DocumentProcessor { 
	
	public DocumentBeanProvider() {
		super();
	}
	
	public DocumentBeanProvider(String clientId, long txId, String uri, Properties props) {
		super(clientId, txId, uri, props);
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentBeanTask;
	}


}
