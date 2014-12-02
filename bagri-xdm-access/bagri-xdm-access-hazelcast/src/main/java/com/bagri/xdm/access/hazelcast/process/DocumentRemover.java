package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_XDMDocumentRemover;

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
