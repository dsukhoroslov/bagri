package com.bagri.xdm.client.hazelcast.task.tx;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_XDMBeginTransactionTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.bagri.xdm.common.XDMTransactionIsolation;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class TransactionStarter extends ClientAwareTask implements Callable<Long> {
	
	protected XDMTransactionIsolation txIsolation;

	public TransactionStarter() {
		super();
	}

	public TransactionStarter(String clientId, XDMTransactionIsolation txIsolation) {
		super(clientId);
		this.txIsolation = txIsolation;
	}

	@Override
	public int getId() {
		return cli_XDMBeginTransactionTask;
	}

	@Override
	public Long call() throws Exception {
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		txIsolation = XDMTransactionIsolation.valueOf(in.readUTF());
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(txIsolation.name());
	}
}
