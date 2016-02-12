package com.bagri.xdm.client.hazelcast.task.tx;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_RollbackTransactionTask;

import java.util.concurrent.Callable;

import com.bagri.xdm.client.hazelcast.task.TransactionAwareTask;

public class TransactionAborter extends TransactionAwareTask implements Callable<Boolean> {
	
	public TransactionAborter() {
		super();
	}

	public TransactionAborter(String clientId, long txId) {
		super(clientId, txId);
	}

	@Override
	public int getId() {
		return cli_RollbackTransactionTask;
	}

	@Override
	public Boolean call() throws Exception {
		return null;
	}

}
