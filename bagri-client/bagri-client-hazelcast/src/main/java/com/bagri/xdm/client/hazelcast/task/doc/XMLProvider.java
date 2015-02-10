package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProvideDocumentXMLTask;

import java.util.concurrent.Callable;

public class XMLProvider extends DocumentAwareTask implements Callable<String> {
	
	public XMLProvider() {
		super();
	}
	
	public XMLProvider(long docId) {
		super(docId);
	}

	@Override
	public String call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentXMLTask;
	}

}
