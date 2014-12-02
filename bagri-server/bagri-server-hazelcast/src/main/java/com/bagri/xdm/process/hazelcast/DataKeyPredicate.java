package com.bagri.xdm.process.hazelcast;

import com.bagri.xdm.common.XDMDataKey;
import com.hazelcast.mapreduce.KeyPredicate;

public class DataKeyPredicate implements KeyPredicate<XDMDataKey> {
	
	private int pathId;
	
	public DataKeyPredicate() {
		//
	}
	
	public DataKeyPredicate(int pathId) {
		this.pathId = pathId;
	}

	@Override
	public boolean evaluate(XDMDataKey key) {
		return key.getPathId() == pathId;
	}

}
