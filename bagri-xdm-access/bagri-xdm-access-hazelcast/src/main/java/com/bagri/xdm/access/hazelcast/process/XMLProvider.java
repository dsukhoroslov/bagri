package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_XMLProviderTask;

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
		return cli_XMLProviderTask;
	}

}
