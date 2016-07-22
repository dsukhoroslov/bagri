package com.bagri.xdm.cache.hazelcast.task.query;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.cache.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.xdm.client.hazelcast.impl.QueuedCursorImpl;
import com.bagri.xquery.api.XQProcessor;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class ResultFetcher extends com.bagri.xdm.client.hazelcast.task.query.ResultFetcher {

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
