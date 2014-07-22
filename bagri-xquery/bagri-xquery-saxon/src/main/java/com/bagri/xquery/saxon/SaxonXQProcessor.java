package com.bagri.xquery.saxon;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQStaticContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.ObjectValue;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xquery.api.XQProcessorBase;
import com.bagri.xquery.saxon.extension.RemoveDocument;
import com.bagri.xquery.saxon.extension.StoreDocument;

public abstract class SaxonXQProcessor extends XQProcessorBase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected Configuration config;
    protected StaticQueryContext sqc;
    protected DynamicQueryContext dqc;
    
    protected XQDataFactory xqFactory;
    
    public SaxonXQProcessor() {
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
        //BagriSourceResolver resolver = new BagriSourceResolver(null);
        //config.registerExternalObjectModel(resolver);
    }
    
    protected void setStaticContext(StaticQueryContext sqc, XQStaticContext ctx) throws XQException {
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
    
    protected void setStaticContext(StaticQueryContext sqc, Map<String, Object> ctx) throws XQException {
    	sqc.setBaseURI((String) ctx.get("BaseURI"));
        sqc.setSchemaAware(false);
        //sqc.setConstructionMode(constructionModeIsPreserve ? Validation.PRESERVE : Validation.STRIP);
    	//sqc.setConstructionMode(ctx.getConstructionMode());
    	sqc.setConstructionMode(Validation.STRIP);
    	sqc.setDefaultElementNamespace((String) ctx.get("DefaultElementTypeNamespace"));
    	sqc.setDefaultFunctionNamespace((String) ctx.get("DefaultFunctionNamespace"));
        //sqc.setEmptyLeast(emptyLeast);
    	sqc.setInheritNamespaces((Boolean) ctx.get("CopyNamespacesModeInherit"));
    	//sqc.setPreserveBoundarySpace(preserve);
    	sqc.setPreserveNamespaces((Boolean) ctx.get("CopyNamespacesModePreserve"));
    	sqc.clearNamespaces();
    	Map<String, String> namespaces = (Map<String, String>) ctx.get("Namespaces");
    	for (Map.Entry<String, String> e: namespaces.entrySet()) {
    		sqc.declareNamespace(e.getKey(), e.getValue());
    	}
    	sqc.declareDefaultCollation((String) ctx.get("DefaultCollation"));
    	//...
    	
        //if (contextItemStaticType != null) {
        //    sqc.setRequiredContextItemType(contextItemStaticType.getSaxonItemType());
        //}
    }

    protected Map<String, Object> contextToMap(XQStaticContext ctx) throws XQException {
    	Map<String, Object> result = new HashMap<String, Object>(8);
    	result.put("BaseURI", ctx.getBaseURI());
    	result.put("DefaultElementTypeNamespace", ctx.getDefaultElementTypeNamespace());
    	result.put("DefaultFunctionNamespace", ctx.getDefaultFunctionNamespace());
    	result.put("DefaultCollation", ctx.getDefaultCollation());
    	result.put("CopyNamespacesModeInherit", ctx.getCopyNamespacesModeInherit() == XQConstants.COPY_NAMESPACES_MODE_INHERIT);
    	result.put("CopyNamespacesModePreserve", ctx.getCopyNamespacesModePreserve() == XQConstants.COPY_NAMESPACES_MODE_PRESERVE);
    	
    	Map<String, String> namespaces = new HashMap<String, String>();
    	for (String prefix: ctx.getNamespacePrefixes()) {
    		namespaces.put(prefix, ctx.getNamespaceURI(prefix));
    	}
    	result.put("Namespaces", namespaces);
    	return result;
    }

    @Override
    public void setXdmManager(XDMDocumentManagement mgr) {
    	//config.setConfigurationProperty("xdm", mgr);
    	super.setXdmManager(mgr);
        config.registerExtensionFunction(new StoreDocument(mgr));
        config.registerExtensionFunction(new RemoveDocument(mgr));
    }
    
    //@Override
    public void setXQDataFactory(XQDataFactory xqFactory) {
    	this.xqFactory = xqFactory;
    }
    
	//@Override
	public String convertToString(Object item) throws XQException {
		
		if (item instanceof NodeOverNodeInfo) {
			try {
				return QueryResult.serialize(((NodeOverNodeInfo) item).getUnderlyingNodeInfo());
			} catch (XPathException ex) {
				throw new XQException(ex.getMessage());
			}
		} else if (item instanceof Node) {
		    StringWriter sw = new StringWriter();
		    try {
		    	Transformer t = TransformerFactory.newInstance().newTransformer();
		    	t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    	t.setOutputProperty(OutputKeys.INDENT, "yes");
		    	t.transform(new DOMSource((Node) item), new StreamResult(sw));
		    } catch (TransformerException te) {
		    	throw new XQException(te.getMessage());
		    }
		    return sw.toString();
		} else if (item instanceof ObjectValue) {
			return convertToString(((ObjectValue) item).getObject());
		} else if (item instanceof XQSequence) {
			return ((XQSequence) item).getSequenceAsString(null);
		} else if (item instanceof XQItem) {
			return ((XQItem) item).getItemAsString(null);
		} else {
			return item.toString();
		}
	}
	
	//public XQItemType getItemType(Object item) throws XQException {
	//	XQItemType type = null;
	//	if (item instanceof AtomicValue) {
	//		int base = BagriJPConverter.getBaseType((AtomicValue) item);
     //     	type = new BagriXQItemType(base, XQItemType.XQITEMKIND_ATOMIC, null, 
    //      			BagriXQDataFactory.getTypeName(base), false, null);
	//	}
	//	return type;
	//}

	//@Override
    public void bindVariable(QName varName, Object var) throws XQException {
		//if (var instanceof XQItem) {
		//	var = ((XQItem) var).getObject();
		//	var = convertToItem(var);
		//}
        dqc.setParameter(getClarkName(varName), var);
    }
    
	//@Override
    public void unbindVariable(QName varName) throws XQException {
		logger.trace("unbindVariable.enter; numberOfKeys: {}; unbind: {}", dqc.getParameters().getNumberOfKeys(), varName);
        dqc.setParameter(getClarkName(varName), null);
		logger.trace("unbindVariable.exit; numberOfKeys: {}", dqc.getParameters().getNumberOfKeys());
    }
    
    private static String getClarkName(QName qname) {
        String uri = qname.getNamespaceURI();
        return "{" + (uri == null ? "" : uri) + "}" + qname.getLocalPart();
    }

    protected String explainQuery(XQueryExpression exp) throws XPathException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exp.getExpression().explain(baos);
        String res = new String(baos.toByteArray(), Charset.defaultCharset());
        return res;
    }

    public void parseXQuery(String query) throws XQException {

        try {
	        final XQueryExpression exp = sqc.compileQuery(query);
	        List results = exp.evaluate(dqc);
	        for (Object result: results) {
	            logger.trace("result: {}; class: {}", result, result.getClass().getName());
	        }
        } catch (XPathException ex) {
        	logger.error("parseXQuery.error: ", ex);
        	throw new XQException(ex.getMessage());
        }
    }
	
	//@Override
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
        	logger.error("prepareXQuery.error: ", ex);
        	throw new XQException(ex.getMessage());
        }
    }
	
}
