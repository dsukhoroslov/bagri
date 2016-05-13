package com.bagri.xdm.client.hazelcast.task.tx;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_CommitTransactionTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.bagri.xdm.client.hazelcast.task.TransactionAwareTask;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class TransactionCommiter extends TransactionAwareTask implements Callable<Boolean> {

	public TransactionCommiter() {
		super();
	}

	public TransactionCommiter(String clientId, long txId) {
		super(clientId, txId);
	}

	@Override
	public int getId() {
		return cli_CommitTransactionTask;
	}

	@Override
	public Boolean call() throws Exception {
		return null;
	}


}
