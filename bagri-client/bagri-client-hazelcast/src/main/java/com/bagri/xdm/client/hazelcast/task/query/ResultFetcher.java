package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_FetchResultsTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.bagri.xdm.client.hazelcast.task.ClientAwareTask;
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
		// TODO Auto-generated method stub
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
