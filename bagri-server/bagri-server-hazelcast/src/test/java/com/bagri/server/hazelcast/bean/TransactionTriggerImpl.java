package com.bagri.server.hazelcast.bean;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Transaction;
import com.bagri.core.server.api.TransactionTrigger;

public class TransactionTriggerImpl implements TransactionTrigger {

	@Override
	public void beforeBegin(Transaction tx, SchemaRepository repo) throws BagriException {
		System.out.println("beforeBegin; tx: " + tx); 
	}

	@Override
	public void afterBegin(Transaction tx, SchemaRepository repo) throws BagriException {
		System.out.println("afterBegin; tx: " + tx); 
	}

	@Override
	public void beforeCommit(Transaction tx, SchemaRepository repo) throws BagriException {
		System.out.println("beforeCommit; tx: " + tx); 
	}

	@Override
	public void afterCommit(Transaction tx, SchemaRepository repo) throws BagriException {
		System.out.println("afterCommit; tx: " + tx); 
	}

	@Override
	public void beforeRollback(Transaction tx, SchemaRepository repo) throws BagriException {
		System.out.println("beforeRollback; tx: " + tx); 
	}

	@Override
	public void afterRollback(Transaction tx, SchemaRepository repo) throws BagriException {
		System.out.println("afterRollback; tx: " + tx); 
	}

}
