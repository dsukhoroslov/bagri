package com.bagri.server.hazelcast.impl;

import java.util.HashMap;
import java.util.Map;

public class ContentDataPool {

	private final Map<String, String> stringPool = new HashMap<>();
	
	private static final ContentDataPool cdPool = new ContentDataPool();
	
	public static final ContentDataPool getDataPool() {
		return cdPool;
	}
	
	public String intern(final String str) {
        String old = stringPool.putIfAbsent(str, str);
        return (old == null) ? str : old;		
	}
	
}
