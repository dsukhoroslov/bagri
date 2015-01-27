package com.bagri.xdm.cache.hazelcast.util;

import java.util.HashMap;
import java.util.Map;

public class SpringContextHolder {
	
	private static final Map<String, Object> context = new HashMap<String, Object>();
	
	private SpringContextHolder() {
		//
	}
	
	private static String getFullName(String schemaName, String contextName) {
		return schemaName + "." + contextName;
	}

	public static boolean containsContext(String schemaName, String contextName) {
		synchronized (context) {
			return context.containsKey(getFullName(schemaName, contextName));
		}
	}
	
	public static Object getContext(String schemaName, String contextName) {
		synchronized (context) {
			return context.get(getFullName(schemaName, contextName));
		}
	}
	
	public static void setContext(String schemaName, String contextName, Object value) {
		synchronized (context) {
			context.put(getFullName(schemaName, contextName), value);
		}
	}

	public static void setAbsentContext(String schemaName, String contextName, Object value) {
		synchronized (context) {
			String name = getFullName(schemaName, contextName);
			if (!context.containsKey(name)) {
				context.put(name, value);
			}
		}
	}
}
