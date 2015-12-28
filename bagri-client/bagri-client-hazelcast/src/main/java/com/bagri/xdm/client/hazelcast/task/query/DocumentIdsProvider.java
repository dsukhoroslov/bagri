package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProvideDocumentIdsTask;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.xdm.client.hazelcast.task.QueryAwareTask;
import com.bagri.xdm.common.XDMDocumentId;

public class DocumentIdsProvider extends QueryAwareTask implements Callable<Collection<XDMDocumentId>> {
	
	public DocumentIdsProvider() {
		super();
	}
	
	public DocumentIdsProvider(String clientId, long txId, String query, Map params, Properties props) {
		super(clientId, txId, query, params, props);
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentIdsTask;
	}

	@Override
	public Collection<XDMDocumentId> call() throws Exception {
		return null;
	}

}
