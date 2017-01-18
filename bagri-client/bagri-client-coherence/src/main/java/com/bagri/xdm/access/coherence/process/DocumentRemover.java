package com.bagri.xdm.access.coherence.process;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

@SuppressWarnings("serial")
public class DocumentRemover extends AbstractProcessor implements PortableObject {
	
    //private static final transient Logger logger = LoggerFactory.getLogger(XDMDocumentRemover.class);

	@Override
	public Object process(Entry entry) {
		
		//logger.trace("process.enter; entry: {}", entry);
		//XDMDocumentManagerServer xdm = ((SpringAwareCacheFactory) CacheFactory.getConfigurableCacheFactory()).getSpringBean("xdmManager", XDMDocumentManagerServer.class);
		//if (xdm == null) {
		//	throw new IllegalStateException("XDM Server Context is not ready");
		//}
		//xdm.deleteDocument(entry);
		return null;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		// nothing to read
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		// nothing to write
	}
}
