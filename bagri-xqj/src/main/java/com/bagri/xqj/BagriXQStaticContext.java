package com.bagri.xqj;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQStaticContext;

import static javax.xml.xquery.XQConstants.*;

public class BagriXQStaticContext implements XQStaticContext {

	private Map<String, String> namespaces = new HashMap<String, String>(6);
	private String defaultElementTypeNamespace = "http://www.w3.org/2001/XMLSchema";
	private String defaultFunctionNamespace = "http://www.w3.org/2005/xpath-functions";
	private String defaultCollationUri = "";
	private String baseUri = "";
	private int constructionMode = CONSTRUCTION_MODE_PRESERVE;
	private int orderingMode = ORDERING_MODE_UNORDERED; 
	private int defaultOrderForEmptySequences = DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST;
	private int boundarySpacePolicy = BOUNDARY_SPACE_PRESERVE;
	private int copyNamespacesModePreserve = COPY_NAMESPACES_MODE_PRESERVE;
	private int copyNamespacesModeInherit = COPY_NAMESPACES_MODE_INHERIT;
	private int bindingMode =  BINDING_MODE_IMMEDIATE;
	private int holdability = HOLDTYPE_CLOSE_CURSORS_AT_COMMIT; 
	private int queryLanguageTypeAndVersion = LANGTYPE_XQUERY;
	private int scrollability = SCROLLTYPE_FORWARD_ONLY; 
	private int queryTimeout = 0;
	private BagriXQItemType type = null;
	
	public BagriXQStaticContext() {
		//
		namespaces.put("xml", "http://www.w3.org/XML/1998/namespace");
		namespaces.put("xs", "http://www.w3.org/2001/XMLSchema");
		namespaces.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		namespaces.put("fn", "http://www.w3.org/2005/xpath-functions");
		namespaces.put("local", "http://www.w3.org/2005/xquery-local-functions");
	}
	
	public BagriXQStaticContext(XQStaticContext from) {
		try {
			copyFrom(from);
		} catch (XQException ex) {
			// log error...
		}
	}
	
	public void copyFrom(XQStaticContext from) throws XQException {

		this.namespaces.clear();
		for (String prefix: from.getNamespacePrefixes()) {
			this.declareNamespace(prefix, from.getNamespaceURI(prefix));
		}
		this.defaultElementTypeNamespace = from.getDefaultElementTypeNamespace();
		this.defaultFunctionNamespace = from.getDefaultFunctionNamespace();
		this.defaultCollationUri = from.getDefaultCollation();
		this.constructionMode = from.getConstructionMode();
		this.orderingMode = from.getOrderingMode();
		this.defaultOrderForEmptySequences = from.getDefaultOrderForEmptySequences();
		this.boundarySpacePolicy = from.getBoundarySpacePolicy();
		this.copyNamespacesModePreserve = from.getCopyNamespacesModePreserve();
		this.copyNamespacesModeInherit = from.getCopyNamespacesModeInherit();
		this.baseUri = from.getBaseURI();
		this.bindingMode = from.getBindingMode();
		this.holdability = from.getHoldability();
		this.queryLanguageTypeAndVersion = from.getQueryLanguageTypeAndVersion();
		this.scrollability = from.getScrollability();
		this.queryTimeout = from.getQueryTimeout();
		setContextItemStaticType(from.getContextItemStaticType());
	}

	@Override
	public String[] getNamespacePrefixes() {
		
		return namespaces.keySet().toArray(new String[namespaces.size()]);
	}

	@Override
	public String getNamespaceURI(String prefix) throws XQException {

		if (prefix == null) {
			throw new XQException("Namespace prefix is null");
		}  
		if (!namespaces.containsKey(prefix)) {
			throw new XQException("Unknown namespace prefix");
		}
		return namespaces.get(prefix);
	}

	@Override
	public void declareNamespace(String prefix, String uri) throws XQException {
		
		if (uri == null) {
			throw new XQException("Declared namespace URI is null");
		}  
		if (uri.length() == 0) {
			namespaces.remove(prefix);
		} else {
			namespaces.put(prefix, uri);
		}
	}
	
	@Override
	public String getDefaultElementTypeNamespace() {
		
		return defaultElementTypeNamespace;
	}

	@Override
	public void setDefaultElementTypeNamespace(String uri) throws XQException {
		
		if (uri == null) {
			throw new XQException("Default element type namespace URI is null");
		}  
		defaultElementTypeNamespace = uri;
	}

	@Override
	public String getDefaultFunctionNamespace() {
		
		return defaultFunctionNamespace;
	}

	@Override
	public void setDefaultFunctionNamespace(String uri) throws XQException {
		
		if (uri == null) {
			throw new XQException("Default function namespace URI is null");
		}  
		defaultFunctionNamespace = uri;
	}

	@Override
	public XQItemType getContextItemStaticType() {
		
		return type;
	}

	@Override
	public void setContextItemStaticType(XQItemType contextItemType) throws XQException {
		
		if (contextItemType == null) {
			this.type = null;
		} else {
			QName typeName = null;
			if (BagriXQUtils.isBaseTypeSupported(contextItemType.getItemKind())) {
				typeName = contextItemType.getTypeName();
			} else {
				// ???
			}
			QName nodeName = null;
			if (BagriXQUtils.isNodeNameSupported(contextItemType.getItemKind())) {
				nodeName = contextItemType.getNodeName();
			}
			this.type = new BagriXQItemType(contextItemType.getBaseType(), contextItemType.getItemKind(),
					nodeName, typeName, contextItemType.isElementNillable(), contextItemType.getSchemaURI());
		}
	}
	
	@Override
	public String getDefaultCollation() {
		
		return defaultCollationUri;
	}

	@Override
	public void setDefaultCollation(String uri) throws XQException {
		
		if (uri == null) {
			throw new XQException("Default collation URI is null");
		}  
		this.defaultCollationUri = uri;
	}
	
	@Override
	public int getConstructionMode() {
		
		return constructionMode;
	}

	@Override
	public void setConstructionMode(int mode) throws XQException {

		if (mode != CONSTRUCTION_MODE_PRESERVE && mode != CONSTRUCTION_MODE_STRIP) {
			throw new XQException("Wrong construction mode value: " + mode);
		}  
		this.constructionMode = mode;
	}

	@Override
	public int getOrderingMode() {
		
		return orderingMode;
	}

	@Override
	public void setOrderingMode(int mode) throws XQException {
		
		if (mode != ORDERING_MODE_ORDERED && mode != ORDERING_MODE_UNORDERED) {
			throw new XQException("Wrong ordering mode value: " + mode);
		}  
		this.orderingMode = mode;
	}

	@Override
	public int getDefaultOrderForEmptySequences() {
		
		return defaultOrderForEmptySequences;
	}

	@Override
	public void setDefaultOrderForEmptySequences(int order) throws XQException {

		if (order != DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST && order != DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST) {
			throw new XQException("Wrong default order for empty sequences value: " + order);
		}  
		this.defaultOrderForEmptySequences = order;
	}

	@Override
	public int getBoundarySpacePolicy() {
		
		return boundarySpacePolicy;
	}

	@Override
	public void setBoundarySpacePolicy(int policy) throws XQException {
		
		if (policy != BOUNDARY_SPACE_PRESERVE && policy != BOUNDARY_SPACE_STRIP) {
			throw new XQException("Wrong boundary space policy value: " + policy);
		}  
		this.boundarySpacePolicy = policy;
	}
	
	@Override
	public int getCopyNamespacesModePreserve() {
		
		return copyNamespacesModePreserve;
	}

	@Override
	public void setCopyNamespacesModePreserve(int mode) throws XQException {
		
		if (mode != COPY_NAMESPACES_MODE_PRESERVE && mode != COPY_NAMESPACES_MODE_NO_PRESERVE) {
			throw new XQException("Wrong copy namespace mode preserve value: " + mode);
		}  
		this.copyNamespacesModePreserve = mode;
	}
	
	@Override
	public int getCopyNamespacesModeInherit() {
		
		return copyNamespacesModeInherit;
	}

	@Override
	public void setCopyNamespacesModeInherit(int mode) throws XQException {
		
		if (mode != COPY_NAMESPACES_MODE_INHERIT && mode != COPY_NAMESPACES_MODE_NO_INHERIT) {
			throw new XQException("Wrong copy namespace mode inherit value: " + mode);
		}  
		this.copyNamespacesModeInherit = mode;
	}

	@Override
	public String getBaseURI() {
		
		return baseUri;
	}

	@Override
	public void setBaseURI(String baseUri) throws XQException {
		
		if (baseUri == null) {
			throw new XQException("Base URI is null");
		}  
		this.baseUri = baseUri;
	}
	
	@Override
	public int getBindingMode() {
		
		return bindingMode;
	}

	@Override
	public void setBindingMode(int bindingMode) throws XQException {

		if (bindingMode != BINDING_MODE_IMMEDIATE && bindingMode != BINDING_MODE_DEFERRED) {
			throw new XQException("Wrong binding mode value: " + bindingMode);
		}  
		this.bindingMode = bindingMode;
	}
	
	@Override
	public int getHoldability() {
		
		return holdability;
	}

	@Override
	public void setHoldability(int holdability) throws XQException {
		
		if (holdability != HOLDTYPE_HOLD_CURSORS_OVER_COMMIT && holdability != HOLDTYPE_CLOSE_CURSORS_AT_COMMIT) {
			throw new XQException("Wrong holdability value: " + holdability);
		}  
		this.holdability = holdability;
	}
	
	@Override
	public int getQueryLanguageTypeAndVersion() {
		
		return queryLanguageTypeAndVersion;
	}

	@Override
	public void setQueryLanguageTypeAndVersion(int langType) throws XQException {
		
		if (langType != LANGTYPE_XQUERY && langType != LANGTYPE_XQUERYX) {
			throw new XQException("Wrong language type and version value: " + langType);
		}  
		// we do not support XQueryX, don't see how it can be set.. 
		this.queryLanguageTypeAndVersion = langType;
	}
	
	@Override
	public int getScrollability() {
		
		return scrollability;
	}

	@Override
	public void setScrollability(int scrollability) throws XQException {
		
		if (scrollability != SCROLLTYPE_FORWARD_ONLY && scrollability != SCROLLTYPE_SCROLLABLE) {
			throw new XQException("Wrong scrollability value: " + scrollability);
		}  
		this.scrollability = scrollability;
	}
	
	@Override
	public int getQueryTimeout() {
		
		return queryTimeout;
	}

	@Override
	public void setQueryTimeout(int seconds) throws XQException {
		
		if (seconds < 0) {
			throw new XQException("Wrong query timeout value: " + seconds);
		}
		this.queryTimeout = seconds;
	}

}
