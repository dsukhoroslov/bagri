package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.mt_xml;

import javax.xml.transform.Source;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;

public class ResourceImpl implements Resource {

	private final Source docSrc;
	
	public ResourceImpl(Source src) {
		this.docSrc = src;
	}

	@Override
	public String getResourceURI() {
		return docSrc.getSystemId();
	}

	@Override
	public Item getItem(XPathContext context) throws XPathException {
		return context.getConfiguration().buildDocumentTree(docSrc).getRootNode();
	}

	@Override
	public String getContentType() {
		return mt_xml;
	}

}
