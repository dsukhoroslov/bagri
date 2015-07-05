package com.bagri.xquery.saxon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static javax.xml.xquery.XQConstants.*;

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
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.ObjectValue;

import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMRepository;

import static com.bagri.xqj.BagriXQConstants.*;

import com.bagri.xqj.BagriXQUtils;
import com.bagri.xquery.api.XQProcessorBase;
import com.bagri.xquery.saxon.extension.RemoveDocument;
import com.bagri.xquery.saxon.extension.StoreDocument;

public abstract class XQProcessorImpl extends XQProcessorBase {

    protected Configuration config;
    protected StaticQueryContext sqc;
    protected DynamicQueryContext dqc;
    
    protected Properties properties = new Properties();
    
    public XQProcessorImpl() {
        config = Configuration.newConfiguration();
        config.setHostLanguage(Configuration.XQUERY);
        config.setSchemaValidationMode(Validation.STRIP);
        //FunctionLibraryList list;
        //list.
        //config.setConfigurationProperty(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION, Boolean.TRUE);
        sqc = config.newStaticQueryContext();
        // supported in Saxon-EE only
        //sqc.setUpdatingEnabled(true);
	    dqc = new DynamicQueryContext(config);
        dqc.setApplyFunctionConversionRulesToExternalVariables(false);
        //sqc. cvr = new StandardObjectConverter();
        JPConverter.allocate(XQItem.class, config);
        //BagriSourceResolver resolver = new BagriSourceResolver(null);
        //config.registerExternalObjectModel(resolver);
    }
    
    public String getProperty(String propName) {
    	return properties.getProperty(propName);
    }
    
    public Properties getProperties() {
    	return properties;
    }
    
    public void setProperties(Properties props) {
    	this.properties.clear();
    	properties.putAll(props);
    	try {
    		setStaticContext(sqc, properties);
    	} catch (XQException ex) {
    		logger.error("setProperties.error", ex);
    	}
    }
    
	public boolean isFeatureSupported(int feature) {
		
		switch (feature) {
			case xqf_Module:
			case xqf_Update: 
			case xqf_Full_Axis:
			case xqf_Serialization: 
			case xqf_Transaction: 
			case xqf_XQuery_Encoding_Decl: return true;

			case xqf_XQueryX: 
			case xqf_Schema_Import:
			case xqf_Schema_Validation:
			case xqf_User_Defined_XML_Schema_Type:
			// next two not supported by Saxon itself :(
			case xqf_Static_Typing:
			case xqf_Static_Typing_Extensions:
				
			// next four from MetaData2 interface
			case xqf_XQuery_Update_Facility: 
			case xqf_XQuery_Full_Text:
			case xqf_XQuery_30:
			case xqf_XA: return false;
		}
		return false;
	}
    
    protected void setStaticContext(StaticQueryContext sqc, XQStaticContext ctx) throws XQException {
    	// !!
        sqc.setSchemaAware(false);
    	sqc.setBaseURI(ctx.getBaseURI());
    	//ctx.getBindingMode()
    	sqc.setPreserveBoundarySpace(ctx.getBoundarySpacePolicy() == BOUNDARY_SPACE_PRESERVE);
    	if (ctx.getConstructionMode() == CONSTRUCTION_MODE_PRESERVE) {
    		sqc.setConstructionMode(Validation.PRESERVE);
    	} else {
    		sqc.setConstructionMode(Validation.STRIP);
    	}
    	// ctx.getContextItemStaticType() -> contextItemStaticType
        //if (contextItemStaticType != null) {
        //    sqc.setRequiredContextItemType(contextItemStaticType.getSaxonItemType());
        //}
    	sqc.setInheritNamespaces(ctx.getCopyNamespacesModeInherit() == COPY_NAMESPACES_MODE_INHERIT);
    	sqc.setPreserveNamespaces(ctx.getCopyNamespacesModePreserve() == COPY_NAMESPACES_MODE_PRESERVE);
    	sqc.declareDefaultCollation(ctx.getDefaultCollation());
    	sqc.setDefaultElementNamespace(ctx.getDefaultElementTypeNamespace());
    	sqc.setDefaultFunctionNamespace(ctx.getDefaultFunctionNamespace());
        //sqc.setEmptyLeast(emptyLeast);
    	sqc.clearNamespaces();
    	String[] prefixes = ctx.getNamespacePrefixes();
    	for (String prefix: prefixes) {
    		sqc.declareNamespace(prefix, ctx.getNamespaceURI(prefix));
    	}
    	//ctx.getDefaultOrderForEmptySequences()
    	//ctx.getHoldability()
    	//ctx.getOrderingMode()
    	if (ctx.getQueryLanguageTypeAndVersion() == LANGTYPE_XQUERY) {
    		sqc.setLanguageVersion(new DecimalValue(1L)); // change to 3L
    	}
    	//ctx.getQueryTimeout()
    	//ctx.getScrollability()
    }
    
    protected void setStaticContext(StaticQueryContext sqc, Properties props) throws XQException {
		logger.trace("setStaticContext.enter; got props: {}", props);
       	// !!
        sqc.setSchemaAware(false);
   		sqc.setBaseURI(props.getProperty(pn_baseURI));
    	//props.getProperty(pn_bindingMode)
    	sqc.setPreserveBoundarySpace(String.valueOf(BOUNDARY_SPACE_PRESERVE).equals(props.getProperty(pn_boundarySpacePolicy)));
   		if (String.valueOf(CONSTRUCTION_MODE_PRESERVE).equals(props.getProperty(pn_constructionMode))) {
    		sqc.setConstructionMode(Validation.PRESERVE);
    	} else {
    		sqc.setConstructionMode(Validation.STRIP);
        }
    	// ctx.getContextItemStaticType() -> contextItemStaticType
        //if (contextItemStaticType != null) {
        //    sqc.setRequiredContextItemType(contextItemStaticType.getSaxonItemType());
        //}
    	sqc.declareDefaultCollation(props.getProperty(pn_defaultCollationUri));
   		sqc.setDefaultElementNamespace(props.getProperty(pn_defaultElementTypeNamespace));
    	sqc.setDefaultFunctionNamespace(props.getProperty(pn_defaultFunctionNamespace));
        //sqc.setEmptyLeast(emptyLeast);
    	sqc.setInheritNamespaces(String.valueOf(COPY_NAMESPACES_MODE_INHERIT).equals(props.getProperty(pn_copyNamespacesModeInherit)));
    	sqc.setPreserveNamespaces(String.valueOf(COPY_NAMESPACES_MODE_PRESERVE).equals(props.getProperty(pn_copyNamespacesModePreserve)));
    	sqc.clearNamespaces();
    	String namespaces = props.getProperty(pn_defaultNamespaces, "");
    	StringTokenizer st = new StringTokenizer(namespaces, " ");
    	while (st.hasMoreTokens()) {
    		String namespace = st.nextToken();
    		int idx = namespace.indexOf(":");
   			sqc.declareNamespace(namespace.substring(0, idx), namespace.substring(idx + 1));
    	}
    	//props.getProperty(pn_defaultOrderForEmptySequences)
    	//props.getProperty(pn_holdability)
    	//props.getProperty(pn_orderingMode)
    	if (String.valueOf(LANGTYPE_XQUERY).equals(props.getProperty(pn_queryLanguageTypeAndVersion))) {
    		sqc.setLanguageVersion(new DecimalValue(1L)); // change to 3L
    	}
    	//props.getProperty(pn_queryTimeout)
    	//props.getProperty(pn_scrollability)
   		logger.trace("setStaticContext.exit; built context: {}; base URI: {}", sqc, sqc.getBaseURI());
    }

    protected Properties contextToProps(XQStaticContext ctx) throws XQException {
    	Properties result = new Properties();
    	result.put(pn_baseURI, ctx.getBaseURI());
    	result.setProperty(pn_bindingMode, String.valueOf(ctx.getBindingMode()));
    	result.setProperty(pn_boundarySpacePolicy, String.valueOf(ctx.getBoundarySpacePolicy()));
    	result.setProperty(pn_constructionMode, String.valueOf(ctx.getConstructionMode()));
    	//ctx.getContextItemStaticType()
    	result.setProperty(pn_copyNamespacesModeInherit, String.valueOf(ctx.getCopyNamespacesModeInherit()));
    	result.setProperty(pn_copyNamespacesModePreserve, String.valueOf(ctx.getCopyNamespacesModePreserve()));
    	result.setProperty(pn_defaultCollationUri, ctx.getDefaultCollation());
    	result.setProperty(pn_defaultElementTypeNamespace, ctx.getDefaultElementTypeNamespace());
    	result.setProperty(pn_defaultFunctionNamespace, ctx.getDefaultFunctionNamespace());
    	if (ctx.getNamespacePrefixes().length > 0) {
    		StringBuffer namespaces = new StringBuffer();
    		for (String prefix: ctx.getNamespacePrefixes()) {
    			namespaces.append(prefix).append(":").append(ctx.getNamespaceURI(prefix));
    			namespaces.append(" ");
    		}
    		result.put(pn_defaultNamespaces, namespaces.toString());
    	}
    	result.setProperty(pn_defaultOrderForEmptySequences, String.valueOf(ctx.getDefaultOrderForEmptySequences()));
    	result.setProperty(pn_holdability, String.valueOf(ctx.getHoldability()));
    	result.setProperty(pn_orderingMode, String.valueOf(ctx.getOrderingMode()));
    	result.setProperty(pn_queryLanguageTypeAndVersion, String.valueOf(ctx.getQueryLanguageTypeAndVersion()));
    	result.setProperty(pn_queryTimeout, String.valueOf(ctx.getQueryTimeout()));
    	result.setProperty(pn_scrollability, String.valueOf(ctx.getScrollability()));
    	return result;
    }

    @Override
    public void setRepository(XDMRepository xRepo) {
    	//config.setConfigurationProperty("xdm", mgr);
    	super.setRepository(xRepo);
        config.registerExtensionFunction(new StoreDocument(xRepo.getDocumentManagement()));
        config.registerExtensionFunction(new RemoveDocument(xRepo.getDocumentManagement()));
        if (xRepo instanceof com.bagri.xdm.cache.api.XDMRepository) {
        	logger.debug("setRepository; registering extensions"); 
        	XQCompilerImpl.registerExtensions(config, ((com.bagri.xdm.cache.api.XDMRepository) xRepo).getLibraries());
        } else {
        	logger.debug("setRepository; client side repo - has no access to extensions"); 
        }
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
			try {
				return XMLUtils.nodeToString((Node) item);
			} catch (Exception ex) {
				throw new XQException(ex.getMessage());
			} 
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
		//logger.trace("unbindVariable.enter; numberOfKeys: {}; unbind: {}", dqc.getParameters().getNumberOfKeys(), varName);
        dqc.setParameter(getClarkName(varName), null);
		//logger.trace("unbindVariable.exit; numberOfKeys: {}", dqc.getParameters().getNumberOfKeys());
    }
    
    protected Map<String, Object> getParams() {
    	Map<String, Object> params = new HashMap<String, Object>(dqc.getParameters().getNumberOfKeys());
    	GlobalParameterSet pset = dqc.getParameters();
    	for (StructuredQName name: pset.getKeys()) {
    		params.put(name.getClarkName(), pset.get(name));
    	}
    	return params;
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
	        // why do we do this? to populate dqc with params??
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
