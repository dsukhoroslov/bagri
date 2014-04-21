package com.bagri.xdm.process.hazelcast;

import java.util.AbstractMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.process.hazelcast.util.HazelcastUtils;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentRemover extends com.bagri.xdm.access.hazelcast.process.DocumentRemover {

    private static final transient Logger logger = LoggerFactory.getLogger(DocumentRemover.class);
    
	private XDMDocumentManagerServer xdmManager;
    
    @Autowired
	public void setXdmManager(XDMDocumentManagerServer xdmManager) {
		this.xdmManager = xdmManager;
		logger.debug("setXdmManager; got manager: {}", xdmManager); 
	}
    
    @Override
	public XDMDocument call() throws Exception {
		logger.trace("process.enter; entry: {}", docId);

		if (xdmManager == null) {
			ApplicationContext ctx = HazelcastUtils.findContext();
			xdmManager = ctx.getBean(HazelcastDocumentServer.class); 
		}
		
		xdmManager.deleteDocument(new AbstractMap.SimpleEntry(docId, null));
		return null;
	}


}
