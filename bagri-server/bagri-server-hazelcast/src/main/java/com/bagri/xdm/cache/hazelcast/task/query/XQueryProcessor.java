package com.bagri.xdm.cache.hazelcast.task.query;

import java.util.Map.Entry;

import com.bagri.xdm.domain.QueryResult;

public class XQueryProcessor extends com.bagri.xdm.client.hazelcast.task.query.XQueryProcessor {

	@Override
	public void processBackup(Entry<Long, QueryResult> entry) {
		// TODO Auto-generated method stub
	}

	@Override
	public Object process(Entry<Long, QueryResult> entry) {
		if (entry.getValue() != null) {
			entry.getValue().getResults();
		}
		return null;
	}

	
}
