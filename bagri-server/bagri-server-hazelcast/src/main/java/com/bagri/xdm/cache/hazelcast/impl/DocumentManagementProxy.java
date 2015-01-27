package com.bagri.xdm.cache.hazelcast.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.manage.InvocationStatistics;
import com.bagri.xdm.api.XDMDocumentManagement;

public class DocumentManagementProxy implements InvocationHandler {
	
	private static final transient Logger logger = LoggerFactory.getLogger(DocumentManagementProxy.class);
	
	private XDMDocumentManagement docMgr;
	private InvocationStatistics docStats;
	
	public static XDMDocumentManagement newDMProxy(XDMDocumentManagement docMgr, 
			InvocationStatistics docStats) {

		Object proxy = Proxy.newProxyInstance(
				XDMDocumentManagement.class.getClassLoader(), 
				new Class[] {XDMDocumentManagement.class}, 
				new DocumentManagementProxy(docMgr, docStats));

		return XDMDocumentManagement.class.cast(proxy);
	}
	
	private DocumentManagementProxy(XDMDocumentManagement docMgr, InvocationStatistics docStats) {
		this.docMgr = docMgr;
		this.docStats = docStats;
		initialize();
	}
	
	private void initialize() {
		docStats.initStats("getDocument");
		docStats.initStats("getDocumentId");
		docStats.initStats("getDocumentAsString");

		// + createDocument ??
		docStats.initStats("storeDocument");
		docStats.initStats("removeDocument");
		
		docStats.initStats("getDocumentIDs");
		docStats.initStats("getDocumentURIs");
		docStats.initStats("getXML");

		docStats.initStats("executeXCommand");
		docStats.initStats("executeXQuery");
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        Object result = null;

        Throwable ex = null;
        boolean failed = false;
        long stamp = System.currentTimeMillis(); //nanoTime();
        //getLogger().enter(name);
        try {
            result = method.invoke(docMgr, args);
        } catch (InvocationTargetException ite) {
            failed = true;
            if (ite.getCause() != null) {
                ex = ite.getCause();
            } else {
                ex = ite;
            }
        } catch (Throwable t) {
            failed = true;
            ex = t;
        }
        //stamp = System.nanoTime() - stamp;
        stamp = System.currentTimeMillis() - stamp;
        docStats.updateStats(name, !failed, stamp);
        if (failed) {
            throw ex;
        }
        return result;
	}
	
}
