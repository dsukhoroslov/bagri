package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_XDMDocumentRemover;

import java.util.concurrent.Callable;

import com.bagri.xdm.domain.XDMDocument;

public class DocumentRemover extends DocumentAwareTask implements Callable<XDMDocument> {

	public DocumentRemover() {
		super();
	}

	public DocumentRemover(long docId) {
		super(docId);
	}

	@Override
	public int getId() {
		return cli_XDMDocumentRemover;
	}

	@Override
	public XDMDocument call() throws Exception {
		return null;
	}

}
