package com.bagri.xquery.saxon;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQStaticContext;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.extension.RemoveDocument;
import com.bagri.xquery.saxon.extension.StoreDocument;

public class BagriXQProcessor extends SaxonXQProcessor implements XQProcessor {
	
    private Configuration config;
    private StaticQueryContext sqc;
    private DynamicQueryContext dqc;
    //private SaxonObjectConverter cvr;
    private BagriCollectionResolver bcr;
    
    public BagriXQProcessor() {
        config = new Configuration();
        config.setHostLanguage(Configuration.XQUERY);
        config.setSchemaValidationMode(Validation.STRIP);
        sqc = config.newStaticQueryContext();
        // supported in Saxon-EE only
        //sqc.setUpdatingEnabled(true);
        //sqc.setl
	    dqc = new DynamicQueryContext(config);
        dqc.setApplyFunctionConversionRulesToExternalVariables(false);
        //sqc. cvr = new StandardObjectConverter();
        JPConverter.allocate(XQItem.class, config);
        BagriSourceResolver resolver = new BagriSourceResolver(null);
        config.registerExternalObjectModel(resolver);
    }

    @Override
    public void setXdmManager(XDMDocumentManagement mgr) {
    	//config.setConfigurationProperty("xdm", mgr);
    	super.setXdmManager(mgr);
    	bcr = new BagriCollectionResolver(mgr);
        config.setCollectionURIResolver(bcr);
        BagriSourceResolver resolver = new BagriSourceResolver(mgr);
        config.setSourceResolver(resolver);
        config.registerExternalObjectModel(resolver);
        //config.a
        config.registerExtensionFunction(new StoreDocument(mgr));
        config.registerExtensionFunction(new RemoveDocument(mgr));
    }
    
    public void parseXQuery(String query) throws XQException {

        try {
	        final XQueryExpression exp = sqc.compileQuery(query);
	        List results = exp.evaluate(dqc);
	        for (Object result: results) {
	            logger.trace("result: {}; class: {}", result, result.getClass().getName());
	        }
        } catch (XPathException ex) {
        	logger.error("", ex);
        	throw new XQException(ex.getMessage());
        }
    }
	
    private void setStaticContext(StaticQueryContext sqc, XQStaticContext ctx) throws XQException {
    	sqc.setBaseURI(ctx.getBaseURI());
        sqc.setSchemaAware(false);
        //sqc.setConstructionMode(constructionModeIsPreserve ? Validation.PRESERVE : Validation.STRIP);
    	//sqc.setConstructionMode(ctx.getConstructionMode());
    	sqc.setConstructionMode(Validation.STRIP);
    	sqc.setDefaultElementNamespace(ctx.getDefaultElementTypeNamespace());
    	sqc.setDefaultFunctionNamespace(ctx.getDefaultFunctionNamespace());
        //sqc.setEmptyLeast(emptyLeast);
    	sqc.setInheritNamespaces(ctx.getCopyNamespacesModeInherit() == XQConstants.COPY_NAMESPACES_MODE_INHERIT);
    	//sqc.setPreserveBoundarySpace(preserve);
    	sqc.setPreserveNamespaces(ctx.getCopyNamespacesModePreserve() == XQConstants.COPY_NAMESPACES_MODE_PRESERVE);
    	sqc.clearNamespaces();
    	String[] prefixes = ctx.getNamespacePrefixes();
    	for (String prefix: prefixes) {
    		sqc.declareNamespace(prefix, ctx.getNamespaceURI(prefix));
    	}
    	sqc.declareDefaultCollation(ctx.getDefaultCollation());
    	//...
    	
        //if (contextItemStaticType != null) {
        //    sqc.setRequiredContextItemType(contextItemStaticType.getSaxonItemType());
        //}
    }
    
    private String explainQuery(XQueryExpression exp) throws XPathException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exp.getExpression().explain(baos);
        String res = new String(baos.toByteArray(), Charset.defaultCharset());
        return res;
    }

	@SuppressWarnings("unchecked")
	@Override
    public Iterator evaluateXQuery(String query, XQStaticContext ctx) throws XQException {

        setStaticContext(sqc, ctx);
        try {
	        final XQueryExpression exp = sqc.compileQuery(query);
	        if (logger.isTraceEnabled()) {
	        	logger.trace("evaluateXQuery; query: \n{}", explainQuery(exp));
	        }
	        if (bcr != null) {
	        	bcr.setExpression(exp);
	        }
	        SequenceIterator<Item> itr = exp.iterator(dqc);
	        return new BagriSequenceIterator(xqFactory, itr); // iterToList(itr);
        } catch (XPathException ex) {
        	logger.error("evaluateXQuery", ex);
        	throw new XQException(ex.getMessage());
        }
    }
    
	@Override
    public Collection<QName> prepareXQuery(String query, XQStaticContext ctx) throws XQException {

        setStaticContext(sqc, ctx);
        try {
	        final XQueryExpression exp = sqc.compileQuery(query);
	        if (logger.isTraceEnabled()) {
	        	logger.trace("prepareXQuery; query: \n{}", explainQuery(exp));
	        }
	        
	        Set<QName> result = new HashSet<QName>(exp.getExternalVariableNames().length);
	        for (StructuredQName qname: exp.getExternalVariableNames()) {
	        	result.add(qname.toJaxpQName());
	        }
	        
	        Iterator<GlobalVariable> itr = exp.getStaticContext().getGlobalVariables(); 
	        while (itr.hasNext()) {
	        	result.add(itr.next().getVariableQName().toJaxpQName());
	        }

	        itr = exp.getStaticContext().getModuleVariables(); 
	        while (itr.hasNext()) {
	        	result.add(itr.next().getVariableQName().toJaxpQName());
	        }
	        
	        return result; 
        } catch (XPathException ex) {
        	logger.error("prepareXQuery", ex);
        	throw new XQException(ex.getMessage());
        }
    }
    
	@Override
    public void bindVariable(QName varName, Object var) throws XQException {
		//if (var instanceof XQItem) {
		//	var = ((XQItem) var).getObject();
		//	var = convertToItem(var);
		//}
        dqc.setParameter(getClarkName(varName), var);
    }
    
	@Override
    public void unbindVariable(QName varName) throws XQException {
		logger.trace("unbindVariable.enter; numberOfKeys: {}; unbind: {}", dqc.getParameters().getNumberOfKeys(), varName);
        dqc.setParameter(getClarkName(varName), null);
		logger.trace("unbindVariable.exit; numberOfKeys: {}", dqc.getParameters().getNumberOfKeys());
    }
    
    private static String getClarkName(QName qname) {
        String uri = qname.getNamespaceURI();
        return "{" + (uri == null ? "" : uri) + "}" + qname.getLocalPart();
    }

	@Override
	public Object processCommand(String command, Map<QName, XQItemAccessor> bindings, XQStaticContext ctx) throws XQException {
		
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
			return doc;
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
    
}

