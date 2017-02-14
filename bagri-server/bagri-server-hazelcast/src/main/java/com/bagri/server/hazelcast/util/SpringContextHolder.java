package com.bagri.server.hazelcast.util;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;

public class SpringContextHolder {
	
	public static final String schema_context = "appContext";
	
	private static final ConcurrentHashMap<String, Object> context = new ConcurrentHashMap<String, Object>();
	
	private SpringContextHolder() {
		//
	}
	
	private static String getFullName(String schemaName, String contextName) {
		return schemaName + "." + contextName;
	}

	public static boolean containsContext(String schemaName, String contextName) {
		return context.containsKey(getFullName(schemaName, contextName));
	}
	
	public static ApplicationContext getContext(String schemaName) {
		return (ApplicationContext) getContext(schemaName, schema_context);
	}
	
	public static Object getContext(String schemaName, String contextName) {
		return context.get(getFullName(schemaName, contextName));
	}
	
	public static void setContext(String schemaName, Object value) {
		context.put(getFullName(schemaName, schema_context), value);
	}

	public static void setAbsentContext(String schemaName, Object value) {
		context.putIfAbsent(getFullName(schemaName, schema_context), value);
	}

}
