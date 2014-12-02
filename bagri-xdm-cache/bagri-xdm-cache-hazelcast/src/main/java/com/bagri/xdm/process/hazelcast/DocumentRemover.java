package com.bagri.xdm.process.hazelcast;

import java.util.AbstractMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.access.api.XDMDocumentManagementServer;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.process.hazelcast.util.HazelcastUtils;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentRemover extends com.bagri.xdm.access.hazelcast.process.DocumentRemover {

    private static final transient Logger logger = LoggerFactory.getLogger(DocumentRemover.class);
    
	private XDMDocumentManagement xdmProxy;
    
    @Autowired
	public void setXdmProxy(XDMDocumentManagement xdmProxy) {
		this.xdmProxy = xdmProxy;
		logger.trace("setXdmProxy; got proxy: {}", xdmProxy); 
	}
    
    @Override
	public XDMDocument call() throws Exception {
		logger.trace("call.enter; docId: {}", docId);

		//if (xdmManager == null) {
		//	ApplicationContext ctx = HazelcastUtils.findContext();
		//	xdmManager = ctx.getBean(DocumentManagementServer.class); 
		//}
		
		xdmProxy.removeDocument(docId);
		return null;
	}


}
