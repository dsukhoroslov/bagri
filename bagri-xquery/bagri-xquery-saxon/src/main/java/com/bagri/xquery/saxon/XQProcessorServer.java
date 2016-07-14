package com.bagri.xquery.saxon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQStaticContext;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.impl.ResultCursorBase;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.cache.api.QueryManagement;
import com.bagri.xdm.common.XDMConstants;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.domain.Query;
import com.bagri.xdm.query.QueryBuilder;
import com.bagri.xquery.api.XQProcessor;

import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.DocumentNumberAllocator;

public class XQProcessorServer extends XQProcessorImpl implements XQProcessor {
	
	private Iterator<?> results;
	private CollectionFinderImpl clnFinder;
    private Map<Integer, XQueryExpression> queries = new HashMap<>();
    
    private static NamePool defNamePool = new NamePool();
    private static DocumentNumberAllocator defDocNumberAllocator = new DocumentNumberAllocator();
    
    public XQProcessorServer() {
    	super();
    	config.setNamePool(defNamePool);
    	config.setDocumentNumberAllocator(defDocNumberAllocator);
    }

    public XQProcessorServer(SchemaRepository xRepo) {
    	this();
    	logger.trace("<init>; got Repo: {}", xRepo);
    	setRepository(xRepo);
    }

	@Override
    public void cancelExecution() throws XQException {
	    logger.info("cancelExecution; not implemented on the server side.");
    }

	@Override
    public void setRepository(SchemaRepository xRepo) {
    	super.setRepository(xRepo);
    	clnFinder = new CollectionFinderImpl((com.bagri.xdm.cache.api.SchemaRepository) xRepo);
    	config.setCollectionFinder(clnFinder);
        config.setDefaultCollection("");
        SourceResolverImpl sResolver = new SourceResolverImpl(xRepo);
        config.setSourceResolver(sResolver);
        //config.registerExternalObjectModel(sResolver);
        config.setURIResolver(sResolver);
        ModuleURIResolver mResolver = new ModuleURIResolverImpl((com.bagri.xdm.cache.api.SchemaRepository) xRepo);
        config.setModuleURIResolver(mResolver);
        dqc.setUnparsedTextURIResolver(sResolver);
    }

    @Override
	public Iterator<?> executeXCommand(String command, Map<String, Object> bindings, XQStaticContext ctx) throws XQException {
		
        //setStaticContext(sqc, ctx);
		return executeXCommand(command, bindings, (Properties) null);
	}
    
    private XQItemAccessor getBoundItem(Map<String, Object> bindings, String varName) throws XQException {
		if (bindings.size() == 0) {
			throw new XQException("bindings not provided");
		}

    	XQItemAccessor item = (XQItemAccessor) bindings.get(varName);
		if (item == null) {
			throw new XQException("variable '" + varName + "' not bound");
		}
    	return item;
    }
	
	@Override
	public Iterator<?> executeXCommand(String command, Map<String, Object> params, Properties props) throws XQException {
		
		// TODO: rewrite it (use client impl)!? 
		// and think about command results!..
	    DocumentManagement dMgr = getRepository().getDocumentManagement();
	    try {
			if (command.startsWith("storeDocument")) {
				XQItemAccessor item = getBoundItem(params, "uri");
				String uri = item.getAtomicValue();
				item = getBoundItem(params, "doc");
				String xml = item.getItemAsString(null);
				// validate document ?
				// add/pass other params ?!
				Document doc = dMgr.storeDocumentFromString(uri, xml, null);
				return Collections.singletonList(doc).iterator();
			} else if (command.startsWith("removeDocument")) {
				XQItemAccessor item = getBoundItem(params, "uri");
				String uri = item.getAtomicValue();
				dMgr.removeDocument(uri);
				return Collections.emptyIterator(); 
			} else {
				throw new XQException("unknown command: " + command);
			}
	    } catch (XDMException ex) {
	    	throw new XQException(ex.getMessage());
	    }
	}
	
	private Iterator<?> execQuery(final String query) throws XQException {
	    logger.trace("execQuery.enter; this: {}", this);
		long stamp = System.currentTimeMillis();
   	    
   	    QueryManagement qMgr = (QueryManagement) getQueryManagement();
   	    Query xQuery = qMgr.getQuery(query);
   	    boolean cacheable = false;
   	    boolean readOnly = true;
	    //logger.trace("execQuery; module resolver: {}", config.getModuleURIResolver());
   	    sqc.setModuleURIResolver(config.getModuleURIResolver());
   	    
	    Integer qKey = qMgr.getQueryKey(query);
   	    XQueryExpression xqExp = queries.get(qKey);
   	    try {
        	if (xqExp == null) {
		        xqExp = sqc.compileQuery(query);
		        if (logger.isTraceEnabled()) {
		        	logger.trace("execQuery; query: \n{}; \nexpression: {}", explainQuery(xqExp), 
		        			xqExp.getExpression().getExpressionName());
		        }
	    	    // HOWTO: distinguish a query from command utilizing external function (store, remove)?
		        readOnly = !xqExp.getExpression().getExpressionName().startsWith(XDMConstants.bg_schema);
	        	queries.put(qKey, xqExp);
        	} 
   	    	
        	Map<String, Object> params = getObjectParams();
    	    if (xQuery == null) {
		        cacheable = true; 
	        	clnFinder.setQuery(null);
		        readOnly |= !xqExp.getExpression().isUpdatingExpression();
	        } else {
	        	//Map params = getParams();
    	    	QueryBuilder xdmQuery = xQuery.getXdmQuery();
    	    	if (!(params == null || params.isEmpty())) {
        	    	xdmQuery.resetParams(params);
    	    	}
	    		clnFinder.setQuery(xdmQuery);
	    		readOnly = xQuery.isReadOnly();
    	    }
        	clnFinder.setExpression(xqExp);

	        stamp = System.currentTimeMillis() - stamp;
		    logger.trace("execQuery; xQuery: {}; params: {}; time taken: {}", xQuery, params, stamp);
		    stamp = System.currentTimeMillis();
	        SequenceIterator itr = xqExp.iterator(dqc);
	        //Result r = new StreamResult();
	        //xqExp.run(dqc, r, null);
	        if (clnFinder.getQuery() != null && cacheable) {
	        	qMgr.addQuery(query, readOnly, clnFinder.getQuery());
	        }
	        stamp = System.currentTimeMillis() - stamp;
		    logger.trace("execQuery.exit; iterator props: {}; time taken: {}", itr.getProperties(), stamp);
	        return new XQIterator(getXQDataFactory(), itr); 
        } catch (Throwable ex) {
        	logger.error("execQuery.error: ", ex);
        	XQException xqe;
        	if (ex instanceof XPathException) {
        		XPathException xpe = (XPathException) ex;
        		if (xpe.getErrorCodeQName() == null) {
            		xqe = new XQException(xpe.getMessage());
        		} else {
	            	if (xpe.getLocator() == null) {
	            		xqe = new XQQueryException(xpe.getMessage(), xpe.getErrorCodeQName().toJaxpQName());
	            	} else {
	            		xqe = new XQQueryException(xpe.getMessage(), xpe.getErrorCodeQName().toJaxpQName(), 
	            			xpe.getLocator().getLineNumber(), xpe.getLocator().getColumnNumber(), 0);
	            	}
        		}
        	} else if (ex instanceof XDMException) {
        		xqe = new XQException(ex.getMessage(), ((XDMException) ex).getVendorCode());
        	} else {
        		xqe = new XQException(ex.getMessage());
        	}
        	// issues with not-serializable staff from Saxon exceptions..
        	//xqe.initCause(ex);
        	throw xqe;
        }
	}
    
	@Override
    public Iterator<?> executeXQuery(String query, XQStaticContext ctx) throws XQException {
        setStaticContext(sqc, ctx);
        return execQuery(query);
    }
    
	@Override
    public Iterator<?> executeXQuery(String query, Properties props) throws XQException {
		setStaticContext(sqc, props);
        return execQuery(query);
	}
	
	@Override
    public ResultCursor processXQuery(String query, XQStaticContext ctx) throws XQException {
   		throw new XQException("Not implemented on the server side. Use method executeXQuery instead");
    }
    
	@Override
	public Iterator<?> getResults() {
		return results;
	}

	@Override
	public void setResults(Iterator<?> itr) {
		this.results = itr;
	}
	
	@Override
	public String toString() {
		return "XQProcessorServer[" + getRepository().getClientId() + "]";
	}
	
}

