package com.bagri.xquery.saxon;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQStaticContext;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xquery.api.XQCursor;
import com.bagri.xquery.api.XQProcessor;

public class BagriXQProcessor extends SaxonXQProcessor implements XQProcessor {
	
    private BagriCollectionResolver bcr;
    
    public BagriXQProcessor() {
    	super();
        //BagriSourceResolver resolver = new BagriSourceResolver(null);
        //config.registerExternalObjectModel(resolver);
    }

    @Override
    public void setXdmManager(XDMDocumentManagement mgr) {
    	super.setXdmManager(mgr);
    	bcr = new BagriCollectionResolver(mgr);
        config.setCollectionURIResolver(bcr);
        BagriSourceResolver resolver = new BagriSourceResolver(mgr);
        config.setSourceResolver(resolver);
        config.registerExternalObjectModel(resolver);
    }

	@Override
	public Iterator executeXCommand(String command, Map<QName, XQItemAccessor> bindings, XQStaticContext ctx) throws XQException {
		
		return executeXCommand(command, bindings, (Map<String, Object>) null);
	}
	
	@Override
	public Iterator executeXCommand(String command, Map<QName, XQItemAccessor> bindings, Map<String, Object> ctx) throws XQException {
		
	    XDMDocumentManagement dMgr = getXdmManager();
	    
		if (command.startsWith("storeDocument")) {
			if (bindings.size() == 0) {
				throw new XQException("document not provided");
			}
			
			XQItemAccessor item;
			if (bindings.size() > 1) {
				QName dName = new QName("doc");
				item = bindings.get(dName);
				if (item == null) {
					throw new XQException("document not provided");
				}
			} else {
				item = bindings.entrySet().iterator().next().getValue();
			}
			String xml = item.getItemAsString(null);
			// validate document ?
			XDMDocument doc = dMgr.storeDocument(xml);
			return Collections.singletonList(doc).iterator();
		} else if (command.startsWith("removeDocument")) {

			if (bindings.size() == 0) {
				throw new XQException("document uri not provided");
			}
			
			XQItemAccessor item;
			if (bindings.size() > 1) {
				QName dName = new QName("uri");
				item = bindings.get(dName);
				if (item == null) {
					throw new XQException("document uri not provided");
				}
			} else {
				item = bindings.entrySet().iterator().next().getValue();
			}
			String uri = item.getAtomicValue();
			dMgr.removeDocument(uri);
			return null; 
		} else {
			throw new XQException("unknown command: " + command);
		}
	}
	
	private Iterator execQuery(String query) throws XQException {
        try {
	        final XQueryExpression exp = sqc.compileQuery(query);
	        if (logger.isTraceEnabled()) {
	        	logger.trace("executeXQuery; query: \n{}", explainQuery(exp));
	        }
	        if (bcr != null) {
	        	bcr.setExpression(exp);
	        }
	        SequenceIterator<Item> itr = exp.iterator(dqc);
	        return new BagriSequenceIterator(xqFactory, itr); // iterToList(itr);
        } catch (XPathException ex) {
        	logger.error("executeXQuery.error: ", ex);
        	throw new XQException(ex.getMessage());
        }
	}
    
	@SuppressWarnings("unchecked")
	@Override
    public Iterator executeXQuery(String query, XQStaticContext ctx) throws XQException {

        setStaticContext(sqc, ctx);
        return execQuery(query);
    }
    
	@Override
    public Iterator executeXQuery(String query, Map<String, Object> ctx) throws XQException {

		setStaticContext(sqc, ctx);
        return execQuery(query);
	}
	
}

