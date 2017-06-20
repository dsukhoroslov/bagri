package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.*;
import static com.bagri.xquery.saxon.SaxonUtils.*;
import static javax.xml.xquery.XQConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQStaticContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.xquery.api.XQProcessorBase;
import com.bagri.support.util.XMLUtils;
import com.bagri.xquery.saxon.ext.doc.GetDocumentContent;
import com.bagri.xquery.saxon.ext.doc.QueryDocumentUris;
import com.bagri.xquery.saxon.ext.doc.RemoveCollectionDocuments;
import com.bagri.xquery.saxon.ext.doc.RemoveDocument;
import com.bagri.xquery.saxon.ext.doc.StoreDocument;
import com.bagri.xquery.saxon.ext.http.HttpGet;
import com.bagri.xquery.saxon.ext.tx.BeginTransaction;
import com.bagri.xquery.saxon.ext.tx.CommitTransaction;
import com.bagri.xquery.saxon.ext.tx.RollbackTransaction;
import com.bagri.xquery.saxon.ext.util.GetUuid;
import com.bagri.xquery.saxon.ext.util.LogOutput;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardLogger;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ObjectValue;

public abstract class XQProcessorImpl extends XQProcessorBase {

    protected Configuration config;
    protected StaticQueryContext sqc;
    protected DynamicQueryContext dqc;
    
    protected Properties properties = new Properties();
    
    public XQProcessorImpl() {
        config = Configuration.newConfiguration();
        //config.setSchemaValidationMode(Validation.STRIP);
        //config.setConfigurationProperty(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION, Boolean.TRUE);
        sqc = config.newStaticQueryContext();
        // supported in Saxon-EE only
        //sqc.setUpdatingEnabled(true);
	    dqc = new DynamicQueryContext(config);
        dqc.setApplyFunctionConversionRulesToExternalVariables(false);
        //sqc. cvr = new StandardObjectConverter();
        //JPConverter.allocate(XQItem.class, null, config);
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
    
	public boolean isQueryReadOnly(final String query) throws XQException {
   	    return (query.indexOf(bg_remove_cln_documents) < 0) && (query.indexOf(bg_remove_document) < 0) 
   	    		&& (query.indexOf(bg_store_document) < 0);
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
        sqc.setEmptyLeast(ctx.getDefaultOrderForEmptySequences() == DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST);
    	sqc.clearNamespaces();
    	String[] prefixes = ctx.getNamespacePrefixes();
    	for (String prefix: prefixes) {
    		sqc.declareNamespace(prefix, ctx.getNamespaceURI(prefix));
    	}
    	//ctx.getHoldability()
    	//ctx.getOrderingMode()
    	if (ctx.getQueryLanguageTypeAndVersion() == LANGTYPE_XQUERY) {
    		sqc.setLanguageVersion(saxon_xquery_version); 
    	}
    	//ctx.getQueryTimeout()
    	//ctx.getScrollability()
        //config.setConfigurationProperty(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION, Boolean.TRUE);
    }
    
    protected void setStaticContext(StaticQueryContext sqc, Properties props) throws XQException {
		logger.trace("setStaticContext.enter; got props: {}", props);
       	// !!
        sqc.setSchemaAware(false);
        String value = props.getProperty(pn_xqj_baseURI);
        if (value != null && !value.isEmpty()) {
        	sqc.setBaseURI(value);
        }
    	//props.getProperty(pn_bindingMode)
        value = props.getProperty(pn_xqj_boundarySpacePolicy);
        if (value != null) {
        	sqc.setPreserveBoundarySpace(String.valueOf(BOUNDARY_SPACE_PRESERVE).equals(value));
        }
        value = props.getProperty(pn_xqj_constructionMode);
        if (value != null) {
        	if (String.valueOf(CONSTRUCTION_MODE_PRESERVE).equals(value)) {
        		sqc.setConstructionMode(Validation.PRESERVE);
        	} else {
        		sqc.setConstructionMode(Validation.STRIP);
        	}
        }
    	// ctx.getContextItemStaticType() -> contextItemStaticType
        //if (contextItemStaticType != null) {
        //    sqc.setRequiredContextItemType(contextItemStaticType.getSaxonItemType());
        //}
        value = props.getProperty(pn_xqj_defaultCollationUri);
        if (value != null) {
        	sqc.declareDefaultCollation(value);
        }
        value = props.getProperty(pn_xqj_defaultElementTypeNamespace);
        if (value != null) {
        	sqc.setDefaultElementNamespace(value);
        }
        value = props.getProperty(pn_xqj_defaultFunctionNamespace);
        if (value != null) {
        	sqc.setDefaultFunctionNamespace(value);
        }
    	value = props.getProperty(pn_xqj_defaultOrderForEmptySequences);
    	if (value != null) {
    		sqc.setEmptyLeast(String.valueOf(DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST).equals(value));
    	}
        value = props.getProperty(pn_xqj_copyNamespacesModeInherit);
        if (value != null) {
        	sqc.setInheritNamespaces(String.valueOf(COPY_NAMESPACES_MODE_INHERIT).equals(value));
        }
        value = props.getProperty(pn_xqj_copyNamespacesModePreserve);
        if (value != null) {
        	sqc.setPreserveNamespaces(String.valueOf(COPY_NAMESPACES_MODE_PRESERVE).equals(value));
        }
        value = props.getProperty(pn_xqj_defaultNamespaces);
        if (value != null) {
        	sqc.clearNamespaces();
        	StringTokenizer st = new StringTokenizer(value, " ");
        	while (st.hasMoreTokens()) {
        		String namespace = st.nextToken();
        		int idx = namespace.indexOf(":");
        		sqc.declareNamespace(namespace.substring(0, idx), namespace.substring(idx + 1));
        	}
    	}
    	//props.getProperty(pn_holdability)
    	//props.getProperty(pn_orderingMode)
        value = props.getProperty(pn_xqj_queryLanguageTypeAndVersion);
        if (value != null) {
        	if (String.valueOf(LANGTYPE_XQUERY).equals(value)) {
        		sqc.setLanguageVersion(saxon_xquery_version); 
        	}
    	}
    	//props.getProperty(pn_queryTimeout)
    	//props.getProperty(pn_scrollability)
        if (logger.isTraceEnabled()) {
        	StringBuilder ns = new StringBuilder(); 
        	Iterator<String> itr = sqc.iterateDeclaredPrefixes(); 
        	while (itr.hasNext()) {
        		String prefix = itr.next();
        		ns.append(prefix).append(":").append(sqc.getNamespaceForPrefix(prefix)).append(" ");
        	}
        	if (ns.length() > 1) {
        		ns.deleteCharAt(ns.length() - 1);
        	}
        	logger.trace("setStaticContext.exit; built context: [baseURI: {}; preserveBoundarySpace: {}; constructionMode: {}; defaultCollationName: {}; " +
        			"defaultElementTypeNamespace: {}; defaultFunctionNamespace: {}; emptyLeast: {}; inheritNamespaces: {}; " +
        			"preserveNamespaces: {}; defaultNamespaces: {}; queryLanguageTypeAndVersion: {}]",
        			sqc.getBaseURI(), sqc.isPreserveBoundarySpace(), sqc.getConstructionMode(), sqc.getDefaultCollationName(), sqc.getDefaultElementNamespace(),
        			sqc.getDefaultFunctionNamespace(), sqc.isEmptyLeast(), sqc.isInheritNamespaces(), sqc.isPreserveNamespaces(), ns.toString(), sqc.getLanguageVersion());
        }
    }

    @Override
    public void setRepository(SchemaRepository xRepo) {
    	super.setRepository(xRepo);
        config.registerExtensionFunction(new GetUuid());
        config.registerExtensionFunction(new LogOutput());
        config.registerExtensionFunction(new HttpGet());
        config.registerExtensionFunction(new GetDocumentContent(xRepo.getDocumentManagement()));
        config.registerExtensionFunction(new RemoveDocument(xRepo.getDocumentManagement()));
        config.registerExtensionFunction(new StoreDocument(xRepo.getDocumentManagement()));
        config.registerExtensionFunction(new RemoveCollectionDocuments(xRepo.getDocumentManagement()));
        config.registerExtensionFunction(new QueryDocumentUris(xRepo.getQueryManagement()));
        config.registerExtensionFunction(new BeginTransaction(xRepo.getTxManagement()));
        config.registerExtensionFunction(new CommitTransaction(xRepo.getTxManagement()));
        config.registerExtensionFunction(new RollbackTransaction(xRepo.getTxManagement()));
        if (xRepo instanceof com.bagri.core.server.api.SchemaRepository) {
        	logger.debug("setRepository; registering extensions"); 
        	XQCompilerImpl.registerExtensions(config, ((com.bagri.core.server.api.SchemaRepository) xRepo).getLibraries());
        } else {
        	logger.debug("setRepository; client side repo - has no access to extensions"); 
        }
    }
    
    public Document convertToDocument(String xml) throws XQException {
    	
    	String baseURI = sqc.getBaseURI(); 
        StringReader sr = new StringReader(xml);
        InputSource is = new InputSource(sr);
        is.setSystemId(baseURI);
        Source source = new SAXSource(is);
        source.setSystemId(baseURI);
        
        try {
        	DocumentInfo doc = config.buildDocument(source);
        	return (Document) DocumentOverNodeInfo.wrap(doc);
		} catch (XPathException ex) {
			throw new XQException(ex.getMessage());
		}
    }
    
	public String convertToString(Object item, Properties props) throws XQException {
		
		if (item instanceof NodeOverNodeInfo) {
			try {
				if (props == null) {
					return QueryResult.serialize(((NodeOverNodeInfo) item).getUnderlyingNodeInfo()); 
				} else {
					Writer writer = new StringWriter();
					QueryResult.serialize(((NodeOverNodeInfo) item).getUnderlyingNodeInfo(), new StreamResult(writer), props);
					writer.close();
					return writer.toString();
				}
			} catch (IOException | XPathException ex) {  
				throw new XQException(ex.getMessage());
			}
		} else if (item instanceof Node) {
			try {
				return XMLUtils.nodeToString((Node) item, props);
			} catch (Exception ex) {
				throw new XQException(ex.getMessage());
			} 
		} else if (item instanceof ObjectValue) {
			return convertToString(((ObjectValue) item).getObject(), props);
		} else if (item instanceof XQSequence) {
			//return ((XQSequence) item).getSequenceAsString(props);
			Writer writer = new StringWriter();
			SequenceIterator itr = new XQSequenceIterator((XQSequence) item, config);
			try {
				QueryResult.serializeSequence(itr, config, writer, props);
				writer.close();
			} catch (IOException | XPathException ex) {  
				throw new XQException(ex.getMessage());
			}
			return writer.toString();
		} else if (item instanceof XQItem) {
			return ((XQItem) item).getItemAsString(props);
		} else {
			return item.toString();
		}
	}
	
	//@Override
    public void bindVariable(String varName, Object var) throws XQException {
    	try {
    		if (var == null) {
        		dqc.setParameter(getStructuredQName(varName), EmptySequence.getInstance());
    		} else {
        		Item item;
	    		if (var instanceof XQItem) {
	    			item = convertXQItem((XQItem) var, config);
	    		} else {
	    			item = objectToItem(var, config);
	    		}
	    		dqc.setParameter(getStructuredQName(varName), item);
    		}
    	} catch (XPathException ex) {
    		throw new XQException(ex.getMessage());
    	}
    }
    
	//@Override
    public void unbindVariable(String varName) throws XQException {
        dqc.setParameter(getStructuredQName(varName), null);
    }

    private static StructuredQName getStructuredQName(String vName) {
    	//return new StructuredQName(qname.getPrefix(), qname.getNamespaceURI(), qname.getLocalPart());
    	return StructuredQName.fromClarkName(vName);
    }
    
    // why it is not <QName, Object> ??
    // because it is used in QueryBuilder where params identified by plain Strings
    protected Map<String, Object> getObjectParams() throws XPathException {
    	GlobalParameterSet params = dqc.getParameters();
    	// got params.getNumberOfKeys() = -1 at one test!
    	//Map<String, Object> bindings = new HashMap<>(params.getNumberOfKeys());
    	Map<String, Object> bindings = new HashMap<>();
    	for (StructuredQName name: params.getKeys()) {
    		Object value = params.get(name);
    		if (value instanceof EmptySequence) {
    			value = null;
    		} else {
    			value = itemToObject((Item) value);
				//value = SequenceTool.convertToJava((Item) value);
    		}
    		bindings.put(name.getClarkName(), value);
    		logger.trace("getParams; name: {}; value: {}", name, value);
    	}
    	return bindings;
    }
    
    protected String explainQuery(XQueryExpression exp) throws XPathException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	PrintStream ps = new PrintStream(baos);
    	Logger log = new StandardLogger(ps);
        exp.getExpression().explain(log);
        String res = new String(baos.toByteArray(), Charset.defaultCharset());
        log.close();
        ps.close();
        try {
			baos.close();
		} catch (IOException ex) {
			throw new XPathException(ex);
		}
        return res;
    }

    // this is for test only?
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
    public Collection<String> prepareXQuery(String query, XQStaticContext ctx) throws XQException {

        setStaticContext(sqc, ctx);
        try {
	        final XQueryExpression exp = sqc.compileQuery(query);
	        if (logger.isTraceEnabled()) {
	        	logger.trace("prepareXQuery; query: \n{}", explainQuery(exp));
	        }
	        
	        Set<String> result = new HashSet<>();
	        Iterator<GlobalVariable> itr = exp.getMainModule().getModuleVariables();
	        while (itr.hasNext()) {
	        	result.add(itr.next().getVariableQName().getClarkName());
	        }
	        return result; 
        } catch (XPathException ex) {
        	logger.error("prepareXQuery.error: ", ex);
        	throw new XQException(ex.getMessage());
        }
    }

    public void setTimeZone(TimeZone timeZone) throws XQException {
    	
        GregorianCalendar now = new GregorianCalendar(timeZone);
        try {
            dqc.setCurrentDateTime(new DateTimeValue(now, true));
        } catch (XPathException ex) {
            throw new XQException(ex.getMessage());
        }
    }
}
