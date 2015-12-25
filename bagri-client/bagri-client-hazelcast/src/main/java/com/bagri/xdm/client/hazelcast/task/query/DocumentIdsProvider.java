package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProvideDocumentIdsTask;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.common.query.ExpressionContainer;
import com.bagri.xdm.common.XDMDocumentId;

public class DocumentIdsProvider extends ResultBuilder implements Callable<Collection<XDMDocumentId>> {
	
	protected String query;
	protected Map bindings;
	protected Properties props;
	
	public DocumentIdsProvider() {
		super();
	}
	
	public DocumentIdsProvider(String clientId, long txId, String query, Map bindings, Properties props) {
		super(clientId, txId, null);
		this.query = query;
		this.bindings = bindings;
		this.props = props;
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
