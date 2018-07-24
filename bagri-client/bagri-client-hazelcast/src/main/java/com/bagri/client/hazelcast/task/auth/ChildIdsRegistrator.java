package com.bagri.client.hazelcast.task.auth;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_RegisterChildIdsTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.ClientAwareTask;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class ChildIdsRegistrator extends ClientAwareTask implements Callable<Void> {
	
	protected String[] childIds;
	
	public ChildIdsRegistrator() {
		// de-ser
	}
	
	public ChildIdsRegistrator(String clientId, String[] childIds) {
		super(clientId);
		this.childIds = childIds;
	}

	@Override
	public Void call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_RegisterChildIdsTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		childIds = in.readUTFArray();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTFArray(childIds);
	}


}
