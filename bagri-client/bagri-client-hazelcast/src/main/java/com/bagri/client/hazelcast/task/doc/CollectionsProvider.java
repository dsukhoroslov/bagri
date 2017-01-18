package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideCollectionsTask;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.ClientAwareTask;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class CollectionsProvider extends ClientAwareTask implements Callable<Collection<String>>, IdentifiedDataSerializable {
	
	public CollectionsProvider() {
		super();
	}
	
	public CollectionsProvider(String clientId) {
		super(clientId);
	}

	@Override
	public Collection<String> call() throws Exception {
		return null;
	}
	
	@Override
	public int getId() {
		return cli_ProvideCollectionsTask;
	}

}
