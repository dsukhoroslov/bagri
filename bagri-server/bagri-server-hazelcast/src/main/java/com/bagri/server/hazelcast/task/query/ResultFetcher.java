package com.bagri.server.hazelcast.task.query;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.client.hazelcast.impl.QueuedCursorImpl;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class ResultFetcher extends com.bagri.client.hazelcast.task.query.ResultFetcher {

	private transient SchemaRepositoryImpl repo;

    @Autowired
	public void setRepository(SchemaRepositoryImpl repo) {
		this.repo = repo;
	}

    @Override
	public Boolean call() throws Exception {
    	XQProcessor xqp = repo.getXQProcessor(clientId);
    	ResultCursor cursor = xqp.getResults();
    	int cnt = ((QueuedCursorImpl) cursor).serialize(repo.getHzInstance());
    	return cnt > 0;
    }
    
}
