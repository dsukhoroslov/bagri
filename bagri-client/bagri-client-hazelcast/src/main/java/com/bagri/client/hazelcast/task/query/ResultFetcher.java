package com.bagri.client.hazelcast.task.query;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_FetchResultsTask;

import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.ClientAwareTask;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ResultFetcher extends ClientAwareTask implements Callable<Boolean>, IdentifiedDataSerializable {
	
	public ResultFetcher() {
		// de-ser
	}
	
	public ResultFetcher(String clientId) {
		super(clientId);
	}

	@Override
	public Boolean call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_FetchResultsTask;
	}

}
