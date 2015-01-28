package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.cache.common.XDMDocumentManagementServer;
import com.bagri.xdm.cache.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.cache.hazelcast.util.HazelcastUtils;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentBuilder extends com.bagri.xdm.client.hazelcast.task.doc.DocumentBuilder {
	
	private static final transient Logger logger = LoggerFactory.getLogger(DocumentBuilder.class);
    
	private transient XDMDocumentManagementServer xdmManager;
    
    @Autowired
	public void setXdmManager(XDMDocumentManagementServer xdmManager) {
		this.xdmManager = xdmManager;
		logger.debug("setXdmManager; got manager: {}", xdmManager); 
	}
    
	@Override
	public Collection<String> call() throws Exception {
		
		if (xdmManager == null) {
			ApplicationContext ctx = HazelcastUtils.findContext();
			xdmManager = ctx.getBean(DocumentManagementImpl.class); 
		}
		
        return xdmManager.buildDocument(docIds, template, params);
    }
    
	
}
