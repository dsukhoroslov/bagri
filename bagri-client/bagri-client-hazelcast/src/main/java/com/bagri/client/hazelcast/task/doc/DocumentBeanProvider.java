package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentBeanTask;

import java.util.Properties;
import java.util.concurrent.Callable;

public class DocumentBeanProvider extends DocumentProcessor { //AwareTask implements Callable<Object> {
	
	public DocumentBeanProvider() {
		super();
	}
	
	public DocumentBeanProvider(String clientId, String uri, Properties props) {
		super(clientId, 0, uri, props);
	}

	//@Override
	//public Object call() throws Exception {
	//	return null;
	//}

	@Override
	public int getId() {
		return cli_ProvideDocumentBeanTask;
	}


}
