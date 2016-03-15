package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProvideDocumentBeanTask;

import java.util.concurrent.Callable;

import com.bagri.xdm.common.XDMDocumentId;

public class DocumentBeanProvider extends DocumentAwareTask implements Callable<Object> {
	
	public DocumentBeanProvider() {
		super();
	}
	
	public DocumentBeanProvider(String clientId, XDMDocumentId docId) {
		super(clientId, 0, docId, null);
	}

	@Override
	public Object call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentBeanTask;
	}


}
