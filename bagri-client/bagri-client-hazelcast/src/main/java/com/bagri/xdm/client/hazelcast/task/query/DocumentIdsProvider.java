package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_DocumentIdsProviderTask;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;

public class DocumentIdsProvider extends ResultBuilder implements Callable<Collection<Long>> {
	
	public DocumentIdsProvider() {
		super();
	}
	
	public DocumentIdsProvider(ExpressionContainer exp) {
		super(exp);
	}

	@Override
	public int getId() {
		return cli_DocumentIdsProviderTask;
	}

	@Override
	public Collection<Long> call() throws Exception {
		return null;
	}

}
