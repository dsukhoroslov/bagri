package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.AbstractMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.cache.hazelcast.util.HazelcastUtils;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentRemover extends com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover {

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
