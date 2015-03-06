package com.bagri.xdm.cache.hazelcast.impl;

import java.lang.reflect.Proxy;
import java.util.concurrent.BlockingQueue;

import com.bagri.common.stats.InvocationStatistics;
import com.bagri.common.stats.InvocationStatsHandler;
import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.stats.watch.StopWatch;
import com.bagri.xdm.api.XDMDocumentManagement;

public class DocumentManagementProxy extends InvocationStatsHandler {
	
	public static XDMDocumentManagement newDMProxy(XDMDocumentManagement docMgr, 
			BlockingQueue<StatisticsEvent> queue, StopWatch stopWatch) {

		Object proxy = Proxy.newProxyInstance(
				XDMDocumentManagement.class.getClassLoader(), 
				new Class[] {XDMDocumentManagement.class}, 
				new DocumentManagementProxy(docMgr, queue, stopWatch));

		return XDMDocumentManagement.class.cast(proxy);
	}
	
	private DocumentManagementProxy(XDMDocumentManagement docMgr, BlockingQueue<StatisticsEvent> queue,
			StopWatch stopWatch) {
		super(docMgr, queue);
		setStopWatch(stopWatch);
	}
	
	//protected void initStats(InvocationStatistics stats) {
	//	stats.initStats("getDocument");
	//	stats.initStats("getDocumentIds");
	//	stats.initStats("getDocumentAsString");
	//	stats.initStats("getDocumentAsSource");
	//	stats.initStats("getDocumentAsStream");
	//	stats.initStats("storeDocumentFromString");
	//	stats.initStats("storeDocumentFromSource");
	//	stats.initStats("storeDocumentFromStream");
	//	stats.initStats("removeDocument");
	//}
	
}
