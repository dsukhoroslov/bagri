package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_GetDocumentTask;

import java.util.concurrent.Callable;

import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;

public class DocumentProvider extends DocumentAwareTask implements Callable<XDMDocument> {
	
	public DocumentProvider() {
		super();
	}
	
	public DocumentProvider(String clientId, XDMDocumentId docId) {
		super(clientId, 0, docId, null);
	}

	@Override
	public XDMDocument call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_GetDocumentTask;
	}



}
