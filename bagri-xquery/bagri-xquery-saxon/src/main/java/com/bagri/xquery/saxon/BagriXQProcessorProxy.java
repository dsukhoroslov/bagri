package com.bagri.xquery.saxon;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQStaticContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xquery.api.XQProcessor;

public class BagriXQProcessorProxy extends SaxonXQProcessor implements XQProcessor {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriXQProcessorProxy.class);

	@Override
	public Collection<QName> prepareXQuery(String query, XQStaticContext ctx)
			throws XQException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator evaluateXQuery(String query, XQStaticContext ctx)
			throws XQException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bindVariable(QName varName, Object var) throws XQException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unbindVariable(QName varName) throws XQException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object processCommand(String command,
			Map<QName, XQItemAccessor> bindings, XQStaticContext ctx)
			throws XQException {
		// TODO Auto-generated method stub
		return null;
	}


}
