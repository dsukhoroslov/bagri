package com.bagri.xquery.saxon;

import java.io.StringWriter;
import java.io.Writer;
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

import com.bagri.common.query.QueryBuilder;
//import com.bagri.xdm.access.api.XDMDocumentManagementServer;
import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
//import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.cache.api.XDMAccessManagement;
import com.bagri.xdm.cache.api.XDMClientManagement;
import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.bagri.xdm.common.XDMConstants;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMQuery;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.bagri.xquery.api.XQProcessor;

import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.DocumentNumberAllocator;

public class XQProcessorServer extends XQProcessorImpl implements XQProcessor {
	
	private Iterator results;
    private CollectionURIResolverImpl bcr;
    private Map<Integer, XQueryExpression> queries = new HashMap<>();
    
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
    public void cancelExecution() throws XQException {
	    logger.info("cancelExecution; not implemented on the server side.");
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
	public Iterator executeXCommand(String command, Map<QName, Object> bindings, XQStaticContext ctx) throws XQException {
		
        //setStaticContext(sqc, ctx);
		return executeXCommand(command, bindings, (Properties) null);
	}
    
    private XQItemAccessor getBoundItem(Map<QName, Object> bindings, String varName) throws XQException {
		if (bindings.size() == 0) {
			throw new XQException("bindings not provided");
		}

    	XQItemAccessor item;
		QName bName = new QName(varName);
		item = (XQItemAccessor) bindings.get(bName);
		if (item == null) {
			throw new XQException("variable '" + varName + "' not bound");
		}
    	return item;
    }
	
	@Override
	public Iterator executeXCommand(String command, Map<QName, Object> bindings, Properties props) throws XQException {
		
	    XDMDocumentManagement dMgr = getRepository().getDocumentManagement();
	    try {
			if (command.startsWith("storeDocument")) {
				XQItemAccessor item = getBoundItem(bindings, "doc");
				String xml = item.getItemAsString(null);
				// validate document ?
				// add/pass other params ?!
				XDMDocument doc = dMgr.storeDocumentFromString(null, xml, null);
				return Collections.singletonList(doc).iterator();
				//return Collections.emptyIterator();
			} else if (command.startsWith("removeDocument")) {
				XQItemAccessor item = getBoundItem(bindings, "docKey");
				long docKey = item.getLong();
				dMgr.removeDocument(new XDMDocumentId(docKey));
				return Collections.emptyIterator(); 
			} else {
				throw new XQException("unknown command: " + command);
			}
	    } catch (XDMException ex) {
	    	throw new XQException(ex.getMessage());
	    }
	}
	
	private Iterator execQuery(final String query) throws XQException {
	    logger.trace("execQuery.enter; this: {}", this);
		
		long stamp = System.currentTimeMillis();
   	    
   	    XDMQueryManagement qMgr = (XDMQueryManagement) getQueryManagement();
   	    XDMQuery xQuery = qMgr.getQuery(query);
   	    boolean cacheable = false;
   	    boolean readOnly = true;
	    //logger.trace("execQuery; module resolver: {}", config.getModuleURIResolver());
	    sqc.setModuleURIResolver(config.getModuleURIResolver());
   	    
	    //check query and get 
	    //if (query.indexOf("declare option bgdm:document-format \"JSON\"") > 0) {
	    //	properties.setProperty("xdm.document.format", "JSON");
	    //	logger.trace("execQuery; set document format: {}", "JSON");
	    //}
	    // actually, I pass document format option in properties..
	    
	    Integer qKey = qMgr.getQueryKey(query);
   	    XQueryExpression xqExp = queries.get(qKey);
   	    try {
        	if (xqExp == null) {
		        xqExp = sqc.compileQuery(query);
		        if (logger.isTraceEnabled()) {
		        	logger.trace("execQuery; query: \n{}", explainQuery(xqExp));
		        }
	    	    // HOWTO: distinguish a query from command utilizing external function (store, remove)?
		        readOnly = !xqExp.getExpression().getExpressionName().startsWith(XDMConstants.bg_schema);
	        	queries.put(qKey, xqExp);
        	} 
   	    	
    	    if (xQuery == null) {
		        cacheable = true; 
	        	bcr.setQuery(null);
		        readOnly |= !xqExp.getExpression().isUpdatingExpression();
	        } else {
	        	Map params = getParams();
    	    	QueryBuilder xdmQuery = xQuery.getXdmQuery();
    	    	if (!(params == null || params.isEmpty())) {
        	    	xdmQuery.resetParams(params);
    	    	}
	    		bcr.setQuery(xdmQuery);
	    		readOnly = xQuery.isReadOnly();
    	    }
        	bcr.setExpression(xqExp);

        	String user = getRepository().getUserName();
        	if (readOnly) {
        		if (!((XDMAccessManagement) getRepository().getAccessManagement()).hasPermission(user, Permission.read)) {
        			throw new XDMException("User " + user + " has no permission to read documents", XDMException.ecAccess);
        		}
        	} else {
        		if (!((XDMAccessManagement) getRepository().getAccessManagement()).hasPermission(user, Permission.modify)) {
        			throw new XDMException("User " + user + " has no permission to create/update/delete documents", XDMException.ecAccess);
        		}
        	}
        	
	        stamp = System.currentTimeMillis() - stamp;
		    logger.trace("execQuery; xQuery: {}; time taken: {}", xQuery, stamp);
		    stamp = System.currentTimeMillis();
	        SequenceIterator<Item> itr = xqExp.iterator(dqc);
	        //Result r = new StreamResult();
	        //xqExp.run(dqc, r, null);
	        if (bcr.getQuery() != null && cacheable) {
	        	qMgr.addQuery(query, readOnly, bcr.getQuery());
	        }
	        stamp = System.currentTimeMillis() - stamp;
		    logger.trace("execQuery.exit; iterator props: {}; time taken: {}", itr.getProperties(), stamp);
		    //serializeResults(itr);
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
	
	@Override
	public String toString() {
		return "XQProcessorServer[" + getRepository().getClientId() + "]";
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

