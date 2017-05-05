package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentMapTask;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

public class DocumentMapProvider extends DocumentProcessor { //DocumentAwareTask implements Callable<Map<String, Object>> {
	
	public DocumentMapProvider() {
		super();
	}
	
	public DocumentMapProvider(String clientId, String uri, Properties props) {
		super(clientId, 0, uri, props);
	}

	//@Override
	//public Map<String, Object> call() throws Exception {
	//	return null;
	//}

	@Override
	public int getId() {
		return cli_ProvideDocumentMapTask;
	}


}
