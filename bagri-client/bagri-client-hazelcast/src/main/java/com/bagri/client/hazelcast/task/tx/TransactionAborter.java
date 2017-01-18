package com.bagri.client.hazelcast.task.tx;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_RollbackTransactionTask;

import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.TransactionAwareTask;

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
