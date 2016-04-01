package com.bagri.xquery.saxon;

import static com.bagri.xdm.common.XDMConstants.bg_schema;

import javax.xml.transform.Source;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;

public class ResourceImpl implements Resource {
	
	private Source docSrc;
	
	public ResourceImpl(Source src) {
		this.docSrc = src;
	}

	@Override
	public String getResourceURI() {
		return docSrc.getSystemId();
	}

	@Override
	public Item getItem(XPathContext context) throws XPathException {
		return context.getConfiguration().buildDocument(docSrc); 
	}

	@Override
	public String getContentType() {
		return "application+xml"; // + json...
	}

}
