package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.cmd_get_document;
import static com.bagri.core.Constants.cmd_remove_cln_documents;
import static com.bagri.core.Constants.cmd_remove_document;
import static com.bagri.core.Constants.cmd_store_document;
import static com.bagri.core.Constants.pn_query_command;
import static com.bagri.support.util.XQUtils.context2Props;
import static com.bagri.support.util.XQUtils.getXQException;
import static com.bagri.xquery.saxon.SaxonUtils.itemToXQItem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQStaticContext;

import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.impl.ResultCursorBase;
import com.bagri.core.model.Query;
import com.bagri.core.xquery.api.XQProcessor;

import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;

public class XQProcessorClient extends XQProcessorImpl implements XQProcessor {
	
	@Override
    public void cancelExecution() throws XQException {
		try {
			getQueryManagement().cancelExecution();
		} catch (BagriException ex) {
    		throw getXQException(ex);
		}
    }
	
	@Override
	public Iterator<Object> executeXCommand(String command, Map<String, Object> params, XQStaticContext ctx) throws XQException {
		
		return executeXCommand(command, params, collectProperties(ctx));
	}
	
	@Override
	public Iterator<Object> executeXCommand(String command, Map<String, Object> params, Properties props) throws XQException {
		
    	//logger.trace("executeXCommand.enter; command: {}", command);
    	try {
    		Object result;
			if (command.equals(cmd_store_document)) {
				String content = ((XQItem) params.remove("content")).getAtomicValue();
				String uri = getDocumentUri(params);
				result = getDocumentManagement().storeDocumentFromString(uri, content, fillProperties(params, props));
			} else if (command.equals(cmd_get_document)) {
				String uri = getDocumentUri(params);
				result = getDocumentManagement().getDocumentAsString(uri, props);
			} else if (command.equals(cmd_remove_document)) {
				String uri = getDocumentUri(params);
				getDocumentManagement().removeDocument(uri);
				result = new Integer(1); 
			} else if (command.equals(cmd_remove_cln_documents)) {
				String collection = (String) params.get("collection");
				getDocumentManagement().removeCollectionDocuments(collection);
				result = new Integer(0); 
			} else {
				props = ensureProperty(props, pn_query_command, "true");
		    	QueryManagement qMgr = getQueryManagement();
	    		ResultCursorBase cursor = (ResultCursorBase) qMgr.executeQuery(command, params, props);
				result = new Integer(1); 
		    	//return cursor; 
			}
			return Collections.singletonList(result).iterator();
    	} catch (BagriException ex) {
    		throw getXQException(ex);
    	}
	}

	@Override
	public Iterator<Object> executeXQuery(String query, Properties props) throws XQException {
		// implement it? what for..?
   		throw new XQException("Not implemented on the client side. Use another executeXQuery method taking XQStaticContext as a parameter instead");
	}

	@Override
	public ResultCursor executeXQuery(String query, XQStaticContext ctx) throws XQException {
		Properties props = collectProperties(ctx);
		props = ensureProperty(props, pn_query_command, "false");
    	try {
        	Map<String, Object> params = getXQItemParams();
    		return getQueryManagement().executeQuery(query, params, props);
    	} catch (XPathException | BagriException ex) {
    		throw getXQException(ex);
    	}
	}

	@Override
    public Collection<String> prepareXQuery(String query, XQStaticContext ctx) throws XQException {
    	QueryManagement qMgr = getQueryManagement();
    	Collection<String> names = qMgr.prepareQuery(query);
    	if (names != null) {
    		return names;
    	}
    	return super.prepareXQuery(query, ctx);
	}

	@Override
    public Query getCurrentQuery(final String query) throws XQException {
		// throw ex?
		return null;
	}
    
	@Override
	public ResultCursor getResults() {
		// throw ex?
		return null;
	}

	@Override
	public void setResults(ResultCursor cursor) {
		// no-op 
		// throw ex?
	}

	private Properties collectProperties(XQStaticContext ctx) throws XQException {
		Properties props = context2Props(ctx);
		for (String name: properties.stringPropertyNames()) {
			if (!props.containsKey(name)) {
				props.setProperty(name, properties.getProperty(name));
			}
		}
		return props;
	}
	
	private Properties ensureProperty(Properties props, String key, String value) {
		if (props == null) {
			props = new Properties();
		}
		props.setProperty(key, value);
		return props;
	}
	
	private Properties fillProperties(Map<String, Object> params, Properties props) {
		if (props == null) {
			props = new Properties();
		}
		for (Map.Entry<String, Object> e: params.entrySet()) {
			props.put(e.getKey(), e.getValue());
		}
		return props;
	}

	private String getDocumentUri(Map<String, Object> params) throws XQException {
		XQItem uri = (XQItem) params.remove("uri");
		if (uri == null) {
			throw new XQException("No document uri passed");
		}
		return uri.getAtomicValue();
	}
	
	private Map<String, Object> getXQItemParams() throws XQException, XPathException {
    	GlobalParameterSet paramSet = dqc.getParameters();
    	Map<String, Object> params = new HashMap<>(paramSet.getNumberOfKeys());
    	for (StructuredQName qName: paramSet.getKeys()) {
    		String pName = qName.getClarkName(); 
    		params.put(pName, itemToXQItem((Item) paramSet.get(qName), this.getXQDataFactory()));
    	}
		return params;
	}

	@Override
	public boolean isQueryReadOnly(String query, Properties props) throws XQException {
		return isQueryReadOnly(query);
	}

}
