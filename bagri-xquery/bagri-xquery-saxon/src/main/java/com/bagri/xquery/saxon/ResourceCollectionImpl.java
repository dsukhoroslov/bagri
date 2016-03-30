package com.bagri.xquery.saxon;

import java.util.Iterator;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.trans.XPathException;

public class ResourceCollectionImpl implements ResourceCollection {

	@Override
	public String getCollectionURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<String> getResourceURIs(XPathContext context) throws XPathException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<? extends Resource> getResources(XPathContext context) throws XPathException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStable(XPathContext context) {
		// TODO Auto-generated method stub
		return false;
	}

}
