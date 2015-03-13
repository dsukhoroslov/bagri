package com.bagri.xdm.client.hazelcast.task.tx;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_XDMCommitTransactionTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class TransactionCommiter extends ClientAwareTask implements Callable<Boolean> {

	protected long txId;
	
	public TransactionCommiter() {
		super();
	}

	public TransactionCommiter(String clientId, long txId) {
		super(clientId);
		this.txId = txId;
	}

	@Override
	public int getId() {
		return cli_XDMCommitTransactionTask;
	}

	@Override
	public Boolean call() throws Exception {
		return null;
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
