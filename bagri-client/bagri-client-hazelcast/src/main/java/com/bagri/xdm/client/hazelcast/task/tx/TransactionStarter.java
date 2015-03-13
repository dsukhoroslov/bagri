package com.bagri.xdm.client.hazelcast.task.tx;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_XDMBeginTransactionTask;

import java.util.concurrent.Callable;

public class TransactionStarter extends ClientAwareTask implements Callable<Long> {

	public TransactionStarter() {
		super();
	}

	public TransactionStarter(String clientId) {
		super(clientId);
	}

	@Override
	public int getId() {
		return cli_XDMBeginTransactionTask;
	}

	@Override
	public Long call() throws Exception {
		return null;
	}

}
