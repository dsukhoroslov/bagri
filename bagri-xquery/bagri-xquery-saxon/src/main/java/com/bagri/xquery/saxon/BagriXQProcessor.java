package com.bagri.xquery.saxon;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQStaticContext;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.lib.CollectionURIResolver;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.DocumentNumberAllocator;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.xdm.access.api.XDMDocumentManagement;
//import com.bagri.xdm.access.api.XDMDocumentManagementServer;
import com.bagri.xdm.access.api.XDMQueryManagement;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMQuery;
import com.bagri.xqj.BagriXQConstants;
import com.bagri.xqj.BagriXQUtils;
import com.bagri.xquery.api.XQProcessor;

public class BagriXQProcessor extends SaxonXQProcessor implements XQProcessor {
	
    private BagriCollectionResolver bcr;
    
    private static NamePool defNamePool = new NamePool();
    private static DocumentNumberAllocator defDocNumberAllocator = new DocumentNumberAllocator();
    
    public BagriXQProcessor() {
    	super();
    	config.setNamePool(defNamePool);
    	config.setDocumentNumberAllocator(defDocNumberAllocator);
        //BagriSourceResolver resolver = new BagriSourceResolver(null);
        //config.registerExternalObjectModel(resolver);
    }

    public BagriXQProcessor(XDMDocumentManagement dMgr) {
    	//this();
    	super();
    	config.setNamePool(defNamePool);
    	config.setDocumentNumberAllocator(defDocNumberAllocator);
    	logger.trace("<init>; got XDM: {}", dMgr);
    	setXdmManager(dMgr);
    }

    @Override
    public void setXdmManager(XDMDocumentManagement mgr) {
    	super.setXdmManager(mgr);
    	//CollectionURIResolver old = bcr;
    	bcr = new BagriCollectionResolver(mgr);
        config.setCollectionURIResolver(bcr);
        BagriSourceResolver resolver = new BagriSourceResolver(mgr);
        config.setSourceResolver(resolver);
        config.registerExternalObjectModel(resolver);
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
       	// profile: it takes 23.6 ms to compile query!
       	// TODO: think about query cache!

	    logger.trace("execQuery.enter; this: {}", this);
		
		long stamp = System.currentTimeMillis();
   	    XQueryExpression xqExp = null;
   	    XDMQuery xQuery = getXQManager().getQuery(query);
   	    boolean cacheable = false;

   	    try {
    	    if (xQuery == null) {
		        xqExp = sqc.compileQuery(query);
		        if (logger.isTraceEnabled()) {
		        	logger.trace("execQuery; query: \n{}", explainQuery(xqExp));
		        }
		        // got exception: can't serialize XQueryExpression properly
		        //getXQManager().addExpression(query, xqExp);

	    	    // HOWTO: distinguish a query from command utilizing external function (store, remove)?
		        cacheable = !xqExp.getExpression().getExpressionName().startsWith(BagriXQConstants.bg_schema);
	        	logger.trace("execQuery; isSubtree: {}; isVacuous: {}; isUpdating: {}", 
	        			xqExp.getExpression().isSubtreeExpression(), xqExp.getExpression().isVacuousExpression(), 
	        			xqExp.getExpression().isUpdatingExpression());

	        	bcr.setExpression(xqExp);
	        	bcr.setContainer(null);
	        } else {
	        	// cacheable = false; -> don't want to rewrite already cached xqExp/xdmExp
    	    	xqExp = (XQueryExpression) xQuery.getXqExpression();
    	    	Map params = getParams();
    	    	if (params == null || params.isEmpty()) {
    	    		// static query; must get params from the query itself..
    	    		// or, just run it again..
    	        	bcr.setExpression(xqExp);
    	        	bcr.setContainer(null);
    	    	} else {
        	    	ExpressionBuilder xdmExp = xQuery.getXdmExpression();
    	    		ExpressionContainer ec = new ExpressionContainer(xdmExp, params);
    	    		bcr.setContainer(ec);
    	    	}
	        	// xqExp was created in another instance of XQProcesser, with different Configuration and BCR.
	        	// the following call to xqExp.iterate causes old BCR from the expression's config to be used!
	        	xqExp.getExecutable().setConfiguration(config);
    	    }
	        
	        stamp = System.currentTimeMillis() - stamp;
		    logger.trace("execQuery; xQuery: {}; time taken: {}", xQuery, stamp);
		    stamp = System.currentTimeMillis();
	        SequenceIterator<Item> itr = xqExp.iterator(dqc);
	        if (bcr.getContainer() != null && cacheable) {
	        	getXQManager().addQuery(query, xqExp, bcr.getContainer().getExpression());
	        } else {
	        	// cache just xqExp
		        //getXQManager().addExpression(query, xqExp);
	        }
	        stamp = System.currentTimeMillis() - stamp;
		    logger.trace("execQuery.exit; iterator props: {}; time taken: {}", itr.getProperties(), stamp);
		    //serializeResults(itr);
	        return new BagriSequenceIterator(getXQDataFactory(), itr); 
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
    
	@SuppressWarnings("unchecked")
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

