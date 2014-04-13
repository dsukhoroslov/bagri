package com.bagri.xdm.process.hazelcast;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.process.hazelcast.util.HazelcastUtils;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentBuilder extends com.bagri.xdm.access.hazelcast.process.DocumentBuilder {
	
	private static final transient Logger logger = LoggerFactory.getLogger(DocumentBuilder.class);
    
	private transient XDMDocumentManagerServer xdmManager;
    
    @Autowired
	public void setXdmManager(XDMDocumentManagerServer xdmManager) {
		this.xdmManager = xdmManager;
		logger.debug("setXdmManager; got manager: {}", xdmManager); 
	}
    
	@Override
	public Collection<String> call() throws Exception {
		
		if (xdmManager == null) {
			ApplicationContext ctx = HazelcastUtils.findContext();
			xdmManager = ctx.getBean(HazelcastDocumentServer.class); 
		}
		
        return xdmManager.buildDocument(docType, template, params, docIds);
    }
    
	
}
