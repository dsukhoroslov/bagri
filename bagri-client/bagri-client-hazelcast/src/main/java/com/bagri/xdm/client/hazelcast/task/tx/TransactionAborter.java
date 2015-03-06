package com.bagri.xdm.client.hazelcast.task.tx;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_XDMRollbackTransactionTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class TransactionAborter extends ClientAwareTask implements Callable<Boolean> {
	
	protected String txId;
	
	public TransactionAborter() {
		super();
	}

	public TransactionAborter(String clientId, String txId) {
		super(clientId);
		this.txId = txId;
	}

	@Override
	public int getId() {
		return cli_XDMRollbackTransactionTask;
	}

	@Override
	public Boolean call() throws Exception {
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		txId = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(txId);
	}

	
}
