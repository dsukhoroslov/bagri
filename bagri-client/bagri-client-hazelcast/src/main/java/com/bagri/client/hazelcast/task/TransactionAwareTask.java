package com.bagri.client.hazelcast.task;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class TransactionAwareTask extends ClientAwareTask {

	protected long txId;
	
	public TransactionAwareTask() {
		super();
	}
	
	public TransactionAwareTask(String clientId, long txId) {
		super(clientId);
		this.txId = txId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		txId = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeLong(txId);
	}
	
}
