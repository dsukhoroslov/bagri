package com.bagri.server.hazelcast.task.tx;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.TransactionManagement;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class TransactionStarter extends com.bagri.client.hazelcast.task.tx.TransactionStarter {

	private transient TransactionManagement txMgr;
    
	@Autowired
	@Override
	public void setRepository(SchemaRepository repo) {
		super.setRepository(repo);
		this.txMgr = repo.getTxManagement();
	}
	
    @Override
	public Long call() throws Exception {
    	((SchemaRepositoryImpl) repo).setClientId(clientId);
		return txMgr.beginTransaction(txIsolation);
	}
	
}
