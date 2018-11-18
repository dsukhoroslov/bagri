package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

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
import com.bagri.core.model.Query;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.query.QueryBuilder;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.support.util.PropUtils;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.functions.IntegratedFunctionCall;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.DocumentNumberAllocator;

public class XQProcessorServer extends XQProcessorImpl implements XQProcessor {
	
	//private ResultCursor<?> cursor;
	private CollectionFinderImpl clnFinder;
	
	// local cache for XQueryExpressions.
	// may be make it static, synchronized? for XQProcessorServer instances..
    private static Map<Integer, XQueryExpression> queries = Collections.synchronizedMap(new HashMap<>());
    //private Map<Integer, XQueryExpression> queries = new HashMap<>();
    //private static Map<Integer, XQueryExpression> queries = new ConcurrentHashMap<>();
    
    private static NamePool defNamePool = new NamePool();
    private static DocumentNumberAllocator defDocNumberAllocator = new DocumentNumberAllocator();
    
	private ThreadLocal<DynamicQueryContext> thDContext = new ThreadLocal<DynamicQueryContext>() {
		
		@Override
		protected DynamicQueryContext initialValue() {
			DynamicQueryContext dqc = new DynamicQueryContext(config);
	        dqc.setApplyFunctionConversionRulesToExternalVariables(false);
	        SourceResolverImpl sResolver = (SourceResolverImpl) config.getSourceResolver();
	        dqc.setUnparsedTextURIResolver(sResolver);
			return dqc;
 		}
	};
    
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
        //config.setConfigurationProperty(FeatureKeys., value);
        //config.setConfigurationProperty(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION, Boolean.TRUE);
        SourceResolverImpl sResolver = new SourceResolverImpl(xRepo, config);
        config.setSourceResolver(sResolver);
        //config.registerExternalObjectModel(sResolver);
        config.setURIResolver(sResolver);
        ModuleURIResolver mResolver = new ModuleURIResolverImpl((com.bagri.core.server.api.SchemaRepository) xRepo);
        config.setModuleURIResolver(mResolver);
        //dqc.setUnparsedTextURIResolver(sResolver);
    	//config.setCompileWithTracing(logger.isDebugEnabled());
        //sqc.setCodeInjector(new CodeInjectorImpl());
    }
	
	@Override
	protected DynamicQueryContext getDynamicContext() {
		return thDContext.get();
	}
	
    @Override
	public Iterator<Object> executeXCommand(String command, Map<String, Object> params, XQStaticContext ctx) throws XQException {
		
        //setStaticContext(sqc, ctx);
		return executeXCommand(command, params, (Properties) null);
	}
    
	@Override
	public Iterator<Object> executeXCommand(String command, Map<String, Object> params, Properties props) throws XQException {
		
		// TODO: rewrite it (use client impl)!? 
		// and think about command results!..
	    DocumentManagement dMgr = getRepository().getDocumentManagement();
	    try {
			if (command.equals(cmd_store_document)) {
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
			} else if (command.equals(cmd_remove_document)) {
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
	
	@Override
    public Iterator<Object> executeXQuery(String query, Properties props) throws XQException {
		try {
			Map<String, Object> params = getObjectParams();
	        return executeXQuery(query, params, props);
        } catch (XPathException xpe) {
        	logger.error("executeXQuery.error: ", xpe);
        	throw convertXPathException(xpe);
        }
	}
	
	@Override
    public Iterator<Object> executeXQuery(String query, Map<String, Object> params, Properties props) throws XQException {
	    logger.trace("executeQuery.enter; this: {}", this);
		long stamp = System.currentTimeMillis();
		// this can be done twice in getXQuery
		setStaticContext(sqc, props);
   	    
   	    QueryManagement qMgr = (QueryManagement) getQueryManagement();
   	    Query xQuery = qMgr.getQuery(query);
	    Integer qKey = qMgr.getQueryKey(query);
   	    try {
   			String overrides = props.getProperty(pn_query_customQuery);
   	    	QueryBuilder xdmQuery = parseOverride(overrides);

   	    	//if (params != null) {
			//	for (Map.Entry<String, Object> var: params.entrySet()) {
			//		bindVariable(var.getKey(), var.getValue());
			//	}
			//}
   	    	DynamicQueryContext dqc = getDynamicContext();
   	    	
   	    	Object content = props.get(pn_document_content);
   	    	if (content != null) {
   	    		SourceResolverImpl sResolver = (SourceResolverImpl) config.getSourceResolver();
   	    		sResolver.setDocumentContent(content);
   	    	}
					
   	   	    XQueryExpression xqExp = getXQuery(qKey, query, null);
   	   	    // what it is for!?
   	   	    xqExp.explainPathMap();
   	   	    //TraceListener tl; tl. 
   	   	    //xqExp.newController(dqc).makePipelineConfiguration().
   	   	    //sqc.
        	clnFinder.setExpression(xqExp);
    	    if (xdmQuery == null && xQuery != null) {
	    	    xdmQuery = xQuery.getXdmQuery();
    	    }
    	    if (params != null && xdmQuery != null) {
    	    	xdmQuery.resetParams(params);
    	    }
    		clnFinder.setQuery(xdmQuery);

	        stamp = System.currentTimeMillis() - stamp;
		    logger.debug("executeXQuery; xQuery: {}; params: {}; time taken: {}", xQuery, params == null ? null : params.keySet(), stamp);
		    stamp = System.currentTimeMillis();
	        SequenceIterator itr = xqExp.iterator(dqc);
	        //Result r = new StreamResult();
	        //xqExp.run(dqc, r, null);
	        stamp = System.currentTimeMillis() - stamp;
		    logger.trace("executeXQuery.exit; time taken: {}", stamp);

		    // not sure we can do this here!
			//if (params != null) {
			//	for (Map.Entry<String, Object> var: params.entrySet()) {
			//		unbindVariable(var.getKey());
			//	}
			//}
			
		    return new XQIterator(getXQDataFactory(), itr); 
        } catch (IOException ioe) {
        	//
        	throw new XQException(ioe.getMessage());
        } catch (XPathException xpe) {
        	logger.error("executeXQuery.error: ", xpe);
        	throw convertXPathException(xpe);
        }
	}
	
	@Override
    public <T> ResultCursor<T> executeXQuery(String query, XQStaticContext ctx) throws XQException {
		// implement it? what for..?
   		throw new XQException("Not implemented on the server side. Use another executeXQuery method taking Properties as a parameter instead");
    }
	
	@Override
	public Query getCurrentQuery(final String query) throws XQException {
		QueryBuilder qb = clnFinder.getQuery();
		if (qb == null) {
			// not 'collection' query?
			// TODO: yes, fix it for updating query!
			return null;
		}
		return new Query(query, isQueryReadOnly(query), qb);
	}
    
	//@Override
	//public <T> ResultCursor<T> getResults() {
	//	return (ResultCursor<T>) cursor;
	//}

	//@Override
	//public <T> void setResults(ResultCursor<T> cursor) {
	//	this.cursor = cursor;
	//}
	
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
	public void clearLocalCache() {
		logger.debug("clearLocalCache.enter; cache size before clear: {}", queries.size());
		queries.clear();
		ResourceCollectionImpl.clear();
	}
	
	@Override
	public void close() {
		super.close();
		//clearLocalCache();
		logger.info("close; processor: {}", this);
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
    		//logger.info("getXQuery; this: {}; queries: {}", this, queries.size());
       	    sqc.setModuleURIResolver(config.getModuleURIResolver());
	        xqExp = sqc.compileQuery(query);
	        if (logger.isTraceEnabled()) {
	        	logger.trace("getXQuery; query: \n{}; \nexpression: {}", explainQuery(xqExp), 
	        			xqExp.getExpression().getExpressionName());
	        }
        	queries.putIfAbsent(queryKey, xqExp);
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

	private QueryBuilder parseOverride(String overrides) throws IOException {
		QueryBuilder result = null;
		if (overrides != null) {
			result = new QueryBuilder();
			Properties conts = PropUtils.propsFromString(overrides);
			Enumeration<String> en = (Enumeration<String>) conts.propertyNames();
			while (en.hasMoreElements()) {
				String clnId = en.nextElement();
				int clnIdx = Integer.parseInt(clnId);
				ExpressionContainer ec = new ExpressionContainer();
				result.addContainer(clnIdx, ec);
			    // (/inventory/product-id EQ $pids) AND ((/inventory/virtual-stores/status EQ 'active') AND ((/inventory/virtual-stores/region-id EQ rid) OR (rid EQ null)))
				ec.getBuilder().buildExpression(conts.getProperty(clnId), clnIdx);
			}
		}
		logger.debug("parseOverride; returning: {}", result);
		return result;
	}

    
}

