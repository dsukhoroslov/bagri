package com.bagri.xquery.saxon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQStaticContext;

import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.om.StructuredQName;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xqj.BagriXQUtils;
import com.bagri.xquery.api.XQProcessor;

public class BagriXQProcessorProxy extends SaxonXQProcessor implements XQProcessor {
	
	@Override
	public Iterator executeXCommand(String command, Map<QName, XQItemAccessor> bindings, XQStaticContext ctx) throws XQException {
		
		return executeXCommand(command, bindings, contextToProps(ctx));
	}

	@Override
	public Iterator executeXCommand(String command, Map<QName, XQItemAccessor> bindings, Properties props) throws XQException {
		
    	//logger.trace("executeXCommand.enter; command: {}", command);
    	XDMDocumentManagement dMgr = getXdmManager();
    	try {
    		return (Iterator) dMgr.executeXCommand(command, bindings, props);
    	} catch (RuntimeException ex) {
    		throw new XQException(ex.getMessage());
    	}
	}

	@Override
	public Iterator executeXQuery(String query, XQStaticContext ctx) throws XQException {

		return executeXQuery(query, contextToProps(ctx));
	}

	@Override
	public Iterator executeXQuery(String query, Properties props) throws XQException {

    	//logger.trace("executeXQuery.enter; query: {}", query);
    	XDMDocumentManagement dMgr = getXdmManager();
    	GlobalParameterSet params = dqc.getParameters();
    	Map bindings = new HashMap<QName, Object>(params.getNumberOfKeys());
    	for (StructuredQName qName: params.getKeys()) {
    		QName vName = new QName(qName.getURI(), qName.getLocalPart(), qName.getPrefix()); 
    		bindings.put(vName, params.get(qName));
    	}
    	//logger.trace("executeXQuery; bindings: {}", bindings);
    	
    	try {
    		return (Iterator) dMgr.executeXQuery(query, bindings, props);
    	} catch (RuntimeException ex) {
    		throw new XQException(ex.getMessage());
    	}
	}

	//@Override
	//public Collection<QName> prepareXQuery(String query, XQStaticContext ctx) throws XQException {
	//	return null;
	//}

}
