package com.bagri.xdm.access.coherence.process;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

public class DocumentCreator extends AbstractProcessor implements PortableObject {
	
	protected String uri;
	protected String xml;
	
	public DocumentCreator() {
	}
	
	public DocumentCreator(String uri, String xml) {
		this();
		this.uri = uri;
		this.xml = xml;
	}

	@Override
	public Object process(Entry entry) {

		//XDMDocumentManagerServer xdm = ((SpringAwareCacheFactory) CacheFactory.getConfigurableCacheFactory()).getSpringBean("xdmManager", XDMDocumentManagerServer.class);
		//if (xdm == null) {
		//	throw new IllegalStateException("XDM Server Context is not ready");
		//}
		return null; //xdm.createDocument(entry, uri, xml);
	}
	
	@Override
	public void readExternal(PofReader in) throws IOException {
		uri = in.readString(0);
		xml = in.readString(1);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeString(0, uri);
		out.writeString(1, xml);
	}

}
