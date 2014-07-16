package com.bagri.xdm.process.hazelcast;

import java.util.AbstractMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.process.hazelcast.util.HazelcastUtils;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCreator extends com.bagri.xdm.access.hazelcast.process.DocumentCreator {

	//private static final long serialVersionUID = -5042940226363419863L;

	private static final transient Logger logger = LoggerFactory.getLogger(DocumentCreator.class);
    
	private transient XDMDocumentManagerServer xdmManager;
	
	@Required
    @Autowired
    //@Qualifier("xdmManager")
	public void setXdmManager(XDMDocumentManagerServer xdmManager) {
		this.xdmManager = xdmManager;
		logger.debug("setXdmManager; got manager: {}", xdmManager); 
	}
	
	@Override
	public XDMDocument call() throws Exception {
		logger.trace("call.enter; xdm: {} ", xdmManager);

		// a workaround for bug in Hazelcast 3.2: https://github.com/hazelcast/hazelcast/issues/2251
		if (xdmManager == null) {
			ApplicationContext ctx = HazelcastUtils.findContext();
			xdmManager = ctx.getBean(HazelcastDocumentServer.class); 
		}
		
		XDMDocument doc = xdmManager.createDocument(new AbstractMap.SimpleEntry(uri, null), docId, xml);
		
		logger.trace("process.exit; returning: {}", doc);
		return doc;
	}
	
}
