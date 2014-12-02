package com.bagri.xdm.process.coherence;

import com.bagri.xdm.access.api.XDMDocumentManagementServer;
import com.bagri.xdm.process.coherence.factory.SpringAwareCacheFactory;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.InvocableMap.Entry;

public class DocumentCreator extends com.bagri.xdm.access.coherence.process.DocumentCreator {
	
    //private static final transient Logger logger = LoggerFactory.getLogger(XDMDocumentProcessor.class);
    
	@Override
	public Object process(Entry entry) {

		//logger.trace("process.enter; entry: {}", entry);
		XDMDocumentManagementServer xdm = ((SpringAwareCacheFactory) CacheFactory.getConfigurableCacheFactory()).getSpringBean("xdmManager", XDMDocumentManagementServer.class);
		if (xdm == null) {
			throw new IllegalStateException("XDM Server Context is not ready");
		}
		return xdm.createDocument(entry, uri, xml);
	}
	
}
