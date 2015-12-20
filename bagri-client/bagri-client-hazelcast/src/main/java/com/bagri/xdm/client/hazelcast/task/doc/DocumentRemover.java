package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_RemoveDocumentTask;

import java.util.concurrent.Callable;

import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;

public class DocumentRemover extends DocumentAwareTask implements Callable<XDMDocument> {

	public DocumentRemover() {
		super();
	}

	public DocumentRemover(XDMDocumentId docId, String clientId, long txId) {
		super(docId, clientId, txId);
	}

	@Override
	public int getId() {
		return cli_RemoveDocumentTask;
	}

	@Override
	public XDMDocument call() throws Exception {
		return null;
	}

}
