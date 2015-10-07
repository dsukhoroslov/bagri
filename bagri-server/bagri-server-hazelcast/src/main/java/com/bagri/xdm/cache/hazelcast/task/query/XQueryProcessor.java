package com.bagri.xdm.cache.hazelcast.task.query;

import java.util.Map.Entry;

import com.bagri.xdm.domain.XDMResults;

public class XQueryProcessor extends com.bagri.xdm.client.hazelcast.task.query.XQueryProcessor {

	@Override
	public void processBackup(Entry<Long, XDMResults> entry) {
		// TODO Auto-generated method stub
	}

	@Override
	public Object process(Entry<Long, XDMResults> entry) {
		if (entry.getValue() != null) {
			entry.getValue().getResults();
		}
		return null;
	}

	
}
