package com.bagri.xquery.saxon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQStaticContext;

import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.om.StructuredQName;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xquery.api.XQCursor;
import com.bagri.xquery.api.XQProcessor;

public class BagriXQProcessorProxy extends SaxonXQProcessor implements XQProcessor {
	
	@Override
	public Iterator executeXCommand(String command, Map<QName, XQItemAccessor> bindings, XQStaticContext ctx) throws XQException {
		
		return executeXCommand(command, bindings, contextToMap(ctx));
	}

	@Override
	public Iterator executeXCommand(String command, Map<QName, XQItemAccessor> bindings, Map<String, Object> ctx) throws XQException {
		
    	logger.trace("executeXCommand.enter; command: {}", command);
    	XDMDocumentManagement dMgr = getXdmManager();
		return (Iterator) dMgr.executeXCommand(command, bindings, ctx);
	}

	@Override
	public Iterator executeXQuery(String query, XQStaticContext ctx) throws XQException {

		return executeXQuery(query, contextToMap(ctx));
	}

	@Override
	public Iterator executeXQuery(String query, Map<String, Object> ctx) throws XQException {

    	logger.trace("executeXQuery.enter; query: {}", query);
    	XDMDocumentManagement dMgr = getXdmManager();
    	GlobalParameterSet params = dqc.getParameters();
    	Map bindings = new HashMap<QName, Object>(params.getNumberOfKeys());
    	for (StructuredQName qName: params.getKeys()) {
    		bindings.put(qName, params.get(qName));
    	}
    	logger.trace("executeXQuery; bindings: {}", bindings);
		return (Iterator) dMgr.executeXQuery(query, bindings, ctx);
	}

	//@Override
	//public Collection<QName> prepareXQuery(String query, XQStaticContext ctx) throws XQException {
		// TODO Auto-generated method stub
	//	return null;
	//}

}
