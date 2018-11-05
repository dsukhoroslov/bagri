package com.bagri.server.hazelcast.task.query;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.client.hazelcast.impl.QueuedCursorImpl;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class ResultFetcher extends com.bagri.client.hazelcast.task.query.ResultFetcher {

    @Autowired
    @Override
	public void setRepository(SchemaRepository repo) {
		super.setRepository(repo);
	}

    @Override
	public Boolean call() throws Exception {
    	XQProcessor xqp = ((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
    	//ResultCursor cursor = xqp.getResults();
    	//int cnt = ((QueuedCursorImpl) cursor).serialize(((SchemaRepositoryImpl) repo).getHzInstance());
    	return false; //cnt > 0;
    }
    
}
