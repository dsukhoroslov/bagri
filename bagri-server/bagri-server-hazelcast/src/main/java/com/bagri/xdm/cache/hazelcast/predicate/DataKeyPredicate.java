package com.bagri.xdm.cache.hazelcast.predicate;

import com.bagri.xdm.common.DataKey;
import com.hazelcast.mapreduce.KeyPredicate;

public class DataKeyPredicate implements KeyPredicate<DataKey> {
	
	private int pathId;
	
	public DataKeyPredicate() {
		//
	}
	
	public DataKeyPredicate(int pathId) {
		this.pathId = pathId;
	}

	@Override
	public boolean evaluate(DataKey key) {
		return key.getPathId() == pathId;
	}

}
