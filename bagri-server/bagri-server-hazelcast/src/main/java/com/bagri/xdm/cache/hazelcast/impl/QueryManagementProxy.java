package com.bagri.xdm.cache.hazelcast.impl;

import java.lang.reflect.Proxy;
import java.util.concurrent.BlockingQueue;

import com.bagri.common.stats.InvocationEvent;
import com.bagri.common.stats.InvocationStatistics;
import com.bagri.common.stats.InvocationStatsHandler;
import com.bagri.common.stats.StopWatch;
import com.bagri.xdm.api.XDMQueryManagement;

public class QueryManagementProxy extends InvocationStatsHandler {
	
	public static XDMQueryManagement newQMProxy(XDMQueryManagement queryMgr, 
			BlockingQueue<InvocationEvent> queue, StopWatch stopWatch) {

		Object proxy = Proxy.newProxyInstance(
				XDMQueryManagement.class.getClassLoader(), 
				new Class[] {XDMQueryManagement.class}, 
				new QueryManagementProxy(queryMgr, queue, stopWatch));

		return XDMQueryManagement.class.cast(proxy);
	}
	
	private QueryManagementProxy(XDMQueryManagement queryMgr, BlockingQueue<InvocationEvent> queue, 
			StopWatch stopWatch) {
		super(queryMgr, queue);
		setStopWatch(stopWatch);
	}
	
	//protected void initStats(InvocationStatistics stats) {
	//	stats.initStats("getDocumentIDs");
	//	stats.initStats("getDocumentURIs");
	//	stats.initStats("getXML");
	//	stats.initStats("executeXCommand");
	//	stats.initStats("executeXQuery");
	//}
	

}
