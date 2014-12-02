package com.bagri.xdm.process.coherence;

import java.util.Set;

import com.bagri.xdm.access.api.XDMDocumentManagementServer;
import com.bagri.xdm.process.coherence.factory.SpringAwareCacheFactory;
import com.tangosol.net.CacheFactory;

public class DocumentBuilder extends com.bagri.xdm.access.coherence.process.DocumentBuilder {
	
    @Override
    public Object aggregate(Set entries) {
        //logger.trace("aggregate.enter; entries: {}", entries.size());
		XDMDocumentManagementServer xdm = ((SpringAwareCacheFactory) CacheFactory.getConfigurableCacheFactory()).getSpringBean("xdmManager", XDMDocumentManagementServer.class);
		if (xdm == null) {
			throw new IllegalStateException("XDM Server Context is not ready");
		}
		//return xdm.buildDocument(docType, template, params, entries);
		return xdm.buildDocument(entries, template, params);
    }
    
}
