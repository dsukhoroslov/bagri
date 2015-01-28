package com.bagri.xdm.cache.hazelcast.impl;

import java.lang.reflect.Proxy;

import com.bagri.common.manage.InvocationStatistics;
import com.bagri.common.manage.InvocationStatsHandler;
import com.bagri.xdm.api.XDMDocumentManagement;

public class DocumentManagementProxy extends InvocationStatsHandler {
	
	public static XDMDocumentManagement newDMProxy(XDMDocumentManagement docMgr, 
			InvocationStatistics docStats) {

		Object proxy = Proxy.newProxyInstance(
				XDMDocumentManagement.class.getClassLoader(), 
				new Class[] {XDMDocumentManagement.class}, 
				new DocumentManagementProxy(docMgr, docStats));

		return XDMDocumentManagement.class.cast(proxy);
	}
	
	private DocumentManagementProxy(XDMDocumentManagement docMgr, InvocationStatistics docStats) {
		super(docMgr, docStats);
	}
	
	protected void initStats(InvocationStatistics stats) {
		stats.initStats("getDocument");
		stats.initStats("getDocumentIds");
		stats.initStats("getDocumentAsString");
		stats.initStats("getDocumentAsSource");
		stats.initStats("getDocumentAsStream");
		stats.initStats("storeDocumentFromString");
		stats.initStats("storeDocumentFromSource");
		stats.initStats("storeDocumentFromStream");
		stats.initStats("removeDocument");
	}
	
}
