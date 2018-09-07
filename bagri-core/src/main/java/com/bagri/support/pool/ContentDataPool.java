package com.bagri.support.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContentDataPool {

	private final Map<String, String> stringPool = new ConcurrentHashMap<>();
	
	private static final ContentDataPool cdPool = new ContentDataPool();
	
	public static final ContentDataPool getDataPool() {
		return cdPool;
	}
	
	public String intern(final String str) {
        String old = stringPool.putIfAbsent(str, str);
        return (old == null) ? str : old;		
	}
	
}
