package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_XDMRemoveDocumentTask;

import java.util.concurrent.Callable;

import com.bagri.xdm.domain.XDMDocument;

public class DocumentRemover extends DocumentAwareTask implements Callable<XDMDocument> {

	public DocumentRemover() {
		super();
	}

	public DocumentRemover(String clientId, long docId, long txId) {
		super(clientId, docId, txId);
	}

	@Override
	public int getId() {
		return cli_XDMRemoveDocumentTask;
	}

	@Override
	public XDMDocument call() throws Exception {
		return null;
	}

}
