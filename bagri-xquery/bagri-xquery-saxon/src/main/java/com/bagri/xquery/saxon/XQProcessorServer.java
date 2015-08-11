package com.bagri.xquery.saxon;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQStaticContext;

import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.DocumentNumberAllocator;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.QueryBuilder;
//import com.bagri.xdm.access.api.XDMDocumentManagementServer;
import com.bagri.xdm.api.XDMDocumentManagement;
//import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMQuery;
import com.bagri.xqj.BagriXQConstants;
import com.bagri.xquery.api.XQProcessor;

public class XQProcessorServer extends XQProcessorImpl implements XQProcessor {
	
	private Iterator results;
    private CollectionURIResolverImpl bcr;
    
    private static NamePool defNamePool = new NamePool();
    private static DocumentNumberAllocator defDocNumberAllocator = new DocumentNumberAllocator();
    
    public XQProcessorServer() {
    	super();
    	config.setNamePool(defNamePool);
    	config.setDocumentNumberAllocator(defDocNumberAllocator);
        //BagriSourceResolver resolver = new BagriSourceResolver(null);
        //config.registerExternalObjectModel(resolver);
    }

    public XQProcessorServer(XDMRepository xRepo) {
    	//this();
    	super();
    	config.setNamePool(defNamePool);
    	config.setDocumentNumberAllocator(defDocNumberAllocator);
    	logger.trace("<init>; got Repo: {}", xRepo);
    	setRepository(xRepo);
    }

    @Override
    public void setRepository(XDMRepository xRepo) {
    	super.setRepository(xRepo);
    	//CollectionURIResolver old = bcr;
    	bcr = new CollectionURIResolverImpl(xRepo);
        config.setCollectionURIResolver(bcr);
        SourceResolverImpl sResolver = new SourceResolverImpl(xRepo);
        config.setSourceResolver(sResolver);
        config.registerExternalObjectModel(sResolver);
        ModuleURIResolver mResolver = new ModuleURIResolverImpl((com.bagri.xdm.cache.api.XDMRepository) xRepo);
        config.setModuleURIResolver(mResolver);
    }

    //@Override
    //public void setXQDataFactory(XQDataFactory xqFactory) {
    //	super.setXQDataFactory(xqFactory);
    //	(BagriXQDataFactory) xqFactory
    //}

    @Override
	public Iterator executeXCommand(String command, Map<QName, XQItemAccessor> bindings, XQStaticContext ctx) throws XQException {
		
        //setStaticContext(sqc, ctx);
		return executeXCommand(command, bindings, (Properties) null);
	}
	
	@Override
	public Iterator executeXCommand(String command, Map<QName, XQItemAccessor> bindings, Properties props) throws XQException {
		
	    XDMDocumentManagement dMgr = getRepository().getDocumentManagement();
	    
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
			// add/pass other params ?!
			XDMDocument doc = dMgr.storeDocumentFromString(0, null, xml);
			return Collections.singletonList(doc).iterator();
			//return Collections.emptyIterator();
		} else if (command.startsWith("removeDocument")) {

			if (bindings.size() == 0) {
				throw new XQException("document uri not provided");
			}
			
			XQItemAccessor item;
			if (bindings.size() > 1) {
				QName dName = new QName("docId");
				item = bindings.get(dName);
				if (item == null) {
					throw new XQException("document ID not provided");
				}
			} else {
				item = bindings.entrySet().iterator().next().getValue();
			}
			long docId = item.getLong();
			dMgr.removeDocument(docId);
			return Collections.emptyIterator(); 
		} else {
			throw new XQException("unknown command: " + command);
		}
	}
	
	private Iterator execQuery(String query) throws XQException {
	    logger.trace("execQuery.enter; this: {}", this);
		
		long stamp = System.currentTimeMillis();
   	    XQueryExpression xqExp = null;
   	    
   	    XDMQueryManagement qMgr = (XDMQueryManagement) getQueryManagement();
   	    XDMQuery xQuery = qMgr.getQuery(query);
   	    boolean cacheable = false;
   	    boolean readOnly = true;
	    //logger.trace("execQuery; module resolver: {}", config.getModuleURIResolver());
	    sqc.setModuleURIResolver(config.getModuleURIResolver());
   	    
	    //check query and get 
	    if (query.indexOf("declare option bgdm:document-format \"JSON\"") > 0) {
	    	properties.setProperty("xdm.document.format", "JSON");
		    logger.trace("execQuery; set document format: {}", "JSON");
	    }
	    
   	    try {
    	    if (xQuery == null) {
		        xqExp = sqc.compileQuery(query);
		        if (logger.isTraceEnabled()) {
		        	logger.trace("execQuery; query: \n{}", explainQuery(xqExp));
		        }
		        // got exception: can't serialize XQueryExpression properly
		        //getXQManager().addExpression(query, xqExp);

		        cacheable = true; 
	    	    // HOWTO: distinguish a query from command utilizing external function (store, remove)?
		        readOnly = !xqExp.getExpression().getExpressionName().startsWith(BagriXQConstants.bg_schema);

	        	bcr.setExpression(xqExp);
	        	bcr.setQuery(null);
	        } else {
	        	// cacheable = false; -> don't want to rewrite already cached xqExp/xdmExp
    	    	xqExp = (XQueryExpression) xQuery.getXqExpression();
	        	bcr.setExpression(xqExp);
    	    	Map params = getParams();
    	    	QueryBuilder xdmQuery = xQuery.getXdmQuery();
    	    	if (!(params == null || params.isEmpty())) {
        	    	xdmQuery.resetParams(params);
    	    	}
	    		bcr.setQuery(xdmQuery);

	    		// xqExp was created in another instance of XQProcesser, with different Configuration and BCR.
	        	// the following call to xqExp.iterate causes old BCR from the expression's config to be used!
	        	xqExp.getExecutable().setConfiguration(config);
    	    }
	        readOnly |= !xqExp.getExpression().isUpdatingExpression();
    	    
	        stamp = System.currentTimeMillis() - stamp;
		    logger.trace("execQuery; xQuery: {}; time taken: {}", xQuery, stamp);
		    stamp = System.currentTimeMillis();
	        SequenceIterator<Item> itr = xqExp.iterator(dqc);
	        //Result r = new StreamResult();
	        //xqExp.run(dqc, r, null);
	        if (bcr.getQuery() != null && cacheable) {
	        	qMgr.addQuery(query, readOnly, xqExp, bcr.getQuery());
	        } else {
	        	// cache just xqExp
		        //qMgr.addExpression(query, xqExp);
	        }
	        stamp = System.currentTimeMillis() - stamp;
		    logger.trace("execQuery.exit; iterator props: {}; time taken: {}", itr.getProperties(), stamp);
		    //serializeResults(itr);
	        return new XQSequenceIterator(getXQDataFactory(), itr); 
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
        	} else {
        		xqe = new XQException(ex.getMessage());
        	}
        	xqe.initCause(ex);
        	throw xqe;
        }
	}
    
	@Override
    public Iterator executeXQuery(String query, XQStaticContext ctx) throws XQException {

        setStaticContext(sqc, ctx);
        return execQuery(query);
    }
    
	@Override
    public Iterator executeXQuery(String query, Properties props) throws XQException {

		setStaticContext(sqc, props);
        return execQuery(query);
	}
	
	@Override
	public Iterator getResults() {
		return results;
	}

	@Override
	public void setResults(Iterator itr) {
		this.results = itr;
	}
	
	private void serializeResults(SequenceIterator results) throws XQException {
		
		try {
			//SequenceIterator iterator = (SequenceIterator) results; 
			Writer writer = new StringWriter();
			Properties props = new Properties();
			props.setProperty("method", "text");
			QueryResult.serializeSequence(results, config, writer, props);
			logger.trace("serializeResults; serialized: {}", writer.toString());
			//Reader reader = new StringReader();
		} catch (XPathException ex) {
			throw new XQException(ex.getMessage());
		}
		
	}

}

