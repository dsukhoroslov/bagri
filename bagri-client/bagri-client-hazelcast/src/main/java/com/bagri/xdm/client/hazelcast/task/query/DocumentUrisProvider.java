package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProvideDocumentUrisTask;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.bagri.common.query.ExpressionContainer;

public class DocumentUrisProvider extends ResultBuilder implements Callable<Collection<String>> {
	
	public DocumentUrisProvider() {
		super();
	}
	
	public DocumentUrisProvider(ExpressionContainer exp, long txId) {
		super(exp, txId);
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentUrisTask;
	}

	@Override
	public Collection<String> call() throws Exception {
		return null;
	}


}
