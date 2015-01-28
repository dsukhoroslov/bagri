package com.bagri.xdm.cache.hazelcast.impl;

import java.lang.reflect.Proxy;

import com.bagri.common.manage.InvocationStatistics;
import com.bagri.common.manage.InvocationStatsHandler;
import com.bagri.xdm.api.XDMQueryManagement;

public class QueryManagementProxy extends InvocationStatsHandler {
	
	public static XDMQueryManagement newQMProxy(XDMQueryManagement queryMgr, 
			InvocationStatistics queryStats) {

		Object proxy = Proxy.newProxyInstance(
				XDMQueryManagement.class.getClassLoader(), 
				new Class[] {XDMQueryManagement.class}, 
				new QueryManagementProxy(queryMgr, queryStats));

		return XDMQueryManagement.class.cast(proxy);
	}
	
	private QueryManagementProxy(XDMQueryManagement queryMgr, InvocationStatistics queryStats) {
		super(queryMgr, queryStats);
	}
	
	protected void initStats(InvocationStatistics stats) {
		stats.initStats("getDocumentIDs");
		stats.initStats("getDocumentURIs");
		stats.initStats("getXML");
		stats.initStats("executeXCommand");
		stats.initStats("executeXQuery");
	}
	

}
