package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProvideDocumentContentTask;

import java.util.concurrent.Callable;

import com.bagri.xdm.common.XDMDocumentId;

public class DocumentContentProvider extends DocumentAwareTask implements Callable<String> {
	
	public DocumentContentProvider() {
		super();
	}
	
	public DocumentContentProvider(XDMDocumentId docId, String clientId) {
		super(docId, clientId, 0);
	}

	@Override
	public String call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentContentTask;
	}

}
