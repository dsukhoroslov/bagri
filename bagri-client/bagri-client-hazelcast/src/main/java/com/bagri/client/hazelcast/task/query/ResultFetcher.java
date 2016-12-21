package com.bagri.client.hazelcast.task.query;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_FetchResultsTask;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.ClientAwareTask;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ResultFetcher extends ClientAwareTask implements Callable<Boolean>, IdentifiedDataSerializable {
	
	protected String clientId;
	
	public ResultFetcher() {
		// de-ser
	}
	
	public ResultFetcher(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public Boolean call() throws Exception {
		return null;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_FetchResultsTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		clientId = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(clientId);
	}

}
