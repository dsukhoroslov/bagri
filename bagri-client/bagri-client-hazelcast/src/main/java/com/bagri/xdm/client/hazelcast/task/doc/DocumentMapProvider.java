package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProvideDocumentMapTask;

import java.util.Map;
import java.util.concurrent.Callable;

import com.bagri.xdm.common.XDMDocumentId;

public class DocumentMapProvider extends DocumentAwareTask implements Callable<Map<String, Object>> {
	
	public DocumentMapProvider() {
		super();
	}
	
	public DocumentMapProvider(String clientId, XDMDocumentId docId) {
		super(clientId, 0, docId, null);
	}

	@Override
	public Map<String, Object> call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentMapTask;
	}


}
