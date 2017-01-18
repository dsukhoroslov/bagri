package com.bagri.server.hazelcast.predicate;

import com.bagri.core.DataKey;
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
