package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQStaticContext;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.model.Document;
import com.bagri.core.model.Query;
import com.bagri.core.query.QueryBuilder;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.xquery.api.XQProcessor;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.functions.IntegratedFunctionCall;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.DocumentNumberAllocator;

public class XQProcessorServer extends XQProcessorImpl implements XQProcessor {
	
	private ResultCursor cursor;
	private CollectionFinderImpl clnFinder;
	// local cache for XQueryExpressions.
	// may be make it static, synchronized? for XQProcessorServer instances..
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
    	clnFinder = new CollectionFinderImpl((com.bagri.core.server.api.SchemaRepository) xRepo);
    	config.setCollectionFinder(clnFinder);
        config.setDefaultCollection("");
        SourceResolverImpl sResolver = new SourceResolverImpl(xRepo);
        config.setSourceResolver(sResolver);
        //config.registerExternalObjectModel(sResolver);
        config.setURIResolver(sResolver);
        ModuleURIResolver mResolver = new ModuleURIResolverImpl((com.bagri.core.server.api.SchemaRepository) xRepo);
        config.setModuleURIResolver(mResolver);
        dqc.setUnparsedTextURIResolver(sResolver);
    	//config.setCompileWithTracing(logger.isDebugEnabled());
        //sqc.setCodeInjector(new CodeInjectorImpl());
    }

    @Override
	public Iterator<Object> executeXCommand(String command, Map<String, Object> bindings, XQStaticContext ctx) throws XQException {
		
        //setStaticContext(sqc, ctx);
		return executeXCommand(command, bindings, (Properties) null);
	}
    
	@Override
	public Iterator<Object> executeXCommand(String command, Map<String, Object> params, Properties props) throws XQException {
		
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
				DocumentAccessor doc = dMgr.storeDocument(uri, xml, null);
				List<Object> result = new ArrayList<>(1);
				result.add(doc);
				return result.iterator();
			} else if (command.startsWith("removeDocument")) {
				XQItemAccessor item = getBoundItem(params, "uri");
				String uri = item.getAtomicValue();
				dMgr.removeDocument(uri, null);
				return Collections.emptyIterator(); 
			} else {
				throw new XQException("unknown command: " + command);
			}
	    } catch (BagriException ex) {
	    	throw new XQException(ex.getMessage());
	    }
	}
	
	private Iterator<Object> execQuery(final String query) throws XQException {
	    logger.trace("execQuery.enter; this: {}", this);
		long stamp = System.currentTimeMillis();
   	    
   	    QueryManagement qMgr = (QueryManagement) getQueryManagement();
   	    Query xQuery = qMgr.getQuery(query);
	    Integer qKey = qMgr.getQueryKey(query);
   	    try {
   	   	    XQueryExpression xqExp = getXQuery(qKey, query, null);
        	Map<String, Object> params = getObjectParams();
    	    if (xQuery == null) {
	        	clnFinder.setQuery(null);
	        } else {
    	    	QueryBuilder xdmQuery = xQuery.getXdmQuery();
    	    	if (!(params == null || params.isEmpty())) {
        	    	xdmQuery.resetParams(params);
    	    	}
	    		clnFinder.setQuery(xdmQuery);
    	    }
        	clnFinder.setExpression(xqExp);

	        stamp = System.currentTimeMillis() - stamp;
		    logger.debug("execQuery; xQuery: {}; params: {}; time taken: {}", xQuery, params, stamp);
		    stamp = System.currentTimeMillis();
	        SequenceIterator itr = xqExp.iterator(dqc);
	        //Result r = new StreamResult();
	        //xqExp.run(dqc, r, null);
	        stamp = System.currentTimeMillis() - stamp;
		    logger.trace("execQuery.exit; time taken: {}", stamp);
	        return new XQIterator(getXQDataFactory(), itr); 
        } catch (XPathException xpe) {
        	logger.error("execQuery.error: ", xpe);
        	throw convertXPathException(xpe);
        }
	}
    
	@Override
    public Iterator<Object> executeXQuery(String query, Properties props) throws XQException {
		setStaticContext(sqc, props);
        return execQuery(query);
	}
	
	@Override
    public ResultCursor executeXQuery(String query, XQStaticContext ctx) throws XQException {
		// implement it? what for..?
   		throw new XQException("Not implemented on the server side. Use another executeXQuery method taking Properties as a parameter instead");
    }
	
	@Override
	public Query getCurrentQuery(final String query) throws XQException {
		if (clnFinder.getQuery() == null) {
			// not 'collection' query?
			// TODO: yes, fix it for updating query!
			return null;
		}
		return new Query(query, isQueryReadOnly(query), clnFinder.getQuery());
	}
    
	@Override
	public ResultCursor getResults() {
		return cursor;
	}

	@Override
	public void setResults(ResultCursor cursor) {
		this.cursor = cursor;
	}
	
	@Override
	public boolean isQueryReadOnly(final String query, Properties props) throws XQException {
		boolean result = super.isQueryReadOnly(query);
		if (result) {
			int qKey = getQueryManagement().getQueryKey(query);
			XQueryExpression xqExp;
			try {
				xqExp = getXQuery(qKey, query, props);
			} catch (XPathException xpe) {
	        	logger.error("isQueryReadOnly.error: ", xpe);
        		throw convertXPathException(xpe);
			}
			result = !isUpdatingExpression(xqExp.getExpression());
   	    }
   	    return result;
	}
	
	@Override
	public String toString() {
		SchemaRepository repo = getRepository();
		return "XQProcessorServer[" + (repo == null ? "unknown" : repo.getClientId()) + "]";
	}
	
    private XQException convertXPathException(XPathException xpe) {
    	XQException xqe;
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
    	return xqe;
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
    
    private XQueryExpression getXQuery(int queryKey, String query, Properties props) throws XPathException, XQException {
   	    XQueryExpression xqExp = queries.get(queryKey);
    	if (xqExp == null) {
    		if (props != null) {
    			setStaticContext(sqc, props);
    		}
       	    sqc.setModuleURIResolver(config.getModuleURIResolver());
	        xqExp = sqc.compileQuery(query);
	        if (logger.isTraceEnabled()) {
	        	logger.trace("getXQuery; query: \n{}; \nexpression: {}", explainQuery(xqExp), 
	        			xqExp.getExpression().getExpressionName());
	        }
        	queries.put(queryKey, xqExp);
    	} 
    	return xqExp;
    }
    
    private boolean isUpdatingExpression(Expression ex) {
		//logger.trace("isUpdatingExpression; got ex: {}; {}", ex.getClass().getName(), ex);
    	if (ex.isUpdatingExpression()) {
    		logger.debug("isUpdatingExpression; got updating ex: {}", ex);
    		return true;
    	}
    	if (ex instanceof IntegratedFunctionCall) {
    		String qName = ex.getExpressionName();
    		if (bg_remove_document.equals(qName) || bg_remove_cln_documents.equals(qName) || bg_store_document.equals(qName)) {
        		logger.trace("isUpdatingExpression; got updating UDF: {}", qName);
    			return true;
    		}
    	} else if (ex instanceof UserFunctionCall) {
    		UserFunctionCall ufc = (UserFunctionCall) ex;
    		ex = ufc.getFunction().getBody();
    	}  
    	
    	Iterator<Operand> itr = ex.operands().iterator();
    	while(itr.hasNext()) {
    		Expression e = itr.next().getChildExpression(); 
    		if (isUpdatingExpression(e)) {
    			return true;
    		}
    	}
    	return false;
    }

	@Override
	public void clearLocalCache() {
		logger.debug("clearLocalCache.enter; cache size before clear: {}", queries.size());
		queries.clear();
	}
    
}

