package com.bagri.core.server.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;

import org.apache.xerces.dom.DOMXSImplementationSourceImpl;
import org.apache.xerces.impl.xs.XSImplementationImpl;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.DocumentType;
import com.bagri.core.model.Namespace;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.support.idgen.IdGenerator;

import static com.bagri.core.Constants.xs_ns;
import static com.bagri.support.util.XQUtils.getBaseTypeForTypeName;

/**
 * Base implementation for XDM Model Management interface. Very close to its client ancestor class. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class ModelManagementBase implements ModelManagement {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    protected static final long timeout = 100; // 100ms to wait for lock..

	protected abstract Map<String, Namespace> getNamespaceCache();
	protected abstract Map<String, Path> getPathCache();
	protected abstract Map<String, DocumentType> getTypeCache();
	protected abstract IdGenerator<Long> getNamespaceGen();
	protected abstract IdGenerator<Long> getPathGen();
	protected abstract IdGenerator<Long> getTypeGen();
    
	protected abstract <K> boolean lock(Map<K, ?> cache, K key); 
	protected abstract <K> void unlock(Map<K, ?> cache, K key); 
	protected abstract <K, V> V putIfAbsent(Map<K, V> cache, K key, V value);

	protected abstract DocumentType getDocumentTypeById(int typeId);
	protected abstract Set<Map.Entry<String, Path>> getTypedPathEntries(int typeId);
	protected abstract Set<Map.Entry<String, Path>> getTypedPathWithRegex(String regex, int typeId);

	/**
	 * WRONG_PATH identifies path not existing in XDMPath dictionary
	 */
    public static final int WRONG_PATH = -1;

    /**
     * 
     * @return all registered XDM Document types
     */
	public Collection<DocumentType> getDocumentTypes() {
		Map<String, DocumentType> typeCache = getTypeCache();
		return typeCache.values();
	}
	
	/**
	 * 
	 * @return all registered XDM Namespaces
	 */
	public Collection<Namespace> getNamespaces() {
		Map<String, Namespace> nsCache = getNamespaceCache();
		return nsCache.values();
	}

    /**
	 * translates full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name"
	 * to its prefixed equivalent: "/ns0:Security/ns0:Name"
	 *  
	 * @param path String; the full node path in Clark form
	 * @return normalized path: String; e.g. "/ns0:Security/ns0:Name"
     */
	public String normalizePath(String path) {
		if (path == null) {
			return null;
		}
		StringBuffer buff = new StringBuffer();
		int pos = 0, end;
		char brace = '{';
		boolean isNamespace = false;
		while ((end = path.indexOf(brace, pos)) >= 0) {
            String segment = path.substring(pos, end);
            pos = end + 1;
			if (isNamespace) {
				isNamespace = false;
				brace = '{';
				String ns = translateNamespace(segment);
				buff.append(ns).append(":");
			} else {
				isNamespace = true;
				brace = '}';
				buff.append(segment);
			}
		}
		buff.append(path.substring(pos));
		return buff.toString();
	}
	
	/**
	 * performs translation from full namespace declaration to its prefix part:
	 * http://tpox-benchmark.com/security -&gt; ns0
	 * 
	 * returns null in case when new (not registered yet) namespace provided;
	 * 
	 * @param namespace String; the full namespace declaration
	 * @return namespace prefix: String; ns0 or null
	 */
	public String translateNamespace(String namespace) {
		return translateNamespace(namespace, null);
	}

	/**
	 * performs translation from full namespace declaration to its prefix part:
	 * http://tpox-benchmark.com/security -&gt; ns0
	 * 
	 * creates new prefix in case when new (not registered yet) namespace provided;
	 * uses the suggested prefix for the new one
	 * 
	 * @param namespace String; the full namespace declaration
	 * @param prefix String; the prefix suggested to use when the namespace is not registered yet, e.g. xsi
	 * @return namespace prefix: String; xsi
	 */
	public String translateNamespace(String namespace, String prefix) {
		Namespace xns = (Namespace) getNamespaceCache().get(namespace);
		if (xns == null) {
			if (prefix == null || prefix.isEmpty()) {
				prefix = "ns" + getNamespaceGen().next();
			}
			xns = new Namespace(namespace, prefix, null);
			xns = putIfAbsent(getNamespaceCache(), namespace, xns);
		}
		String result = xns.getPrefix();
		return result;
	}

	/**
	 * search for registered full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
	 * 
	 * @param path String; node path in Clark form
	 * @return registered {@link Path} structure if any
	 */
	public Path getPath(String path) {
		return getPathCache().get(normalizePath(path));
	}
    
	/**
	 * translates full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
	 * to XDMPath;
	 * 
	 * creates new XDMPath if it is not registered yet;
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param path String; the full node path in Clark form
	 * @param kind XDMNodeKind; the type of the node, one of {@link NodeKind} enum literals
	 * @param dataType int; type of the node value
	 * @param occurrence {@link Occurrence}; multiplicity of the node
	 * @return new or existing {@link Path} structure
	 * @throws BagriException in case of any error
	 */
	public Path translatePath(int typeId, String path, NodeKind kind, int dataType, Occurrence occurrence) throws BagriException {
		// "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
		
		if (kind != NodeKind.document) {
			if (path == null || path.length() == 0) {
				return null; //WRONG_PATH;
			}
		
			path = normalizePath(path);
		}
		Path result = addDictionaryPath(typeId, path, kind, dataType, occurrence); 
		return result;
	}
	
	/**
	 * performs translation from full namespace declaration to its prefix part:
	 * http://tpox-benchmark.com/security -&gt; ns0.
	 * 
	 * creates new prefix in case when new (not registered yet) namespace provided;
	 * 
	 * @param namespace String; the full namespace declaration 
	 * @return namespace prefix: String; ns0
	 */
	public String getNamespacePrefix(String namespace) {
		String result = null;
		Namespace xns = getNamespaceCache().get(namespace);
		if (xns != null) {
			result = xns.getPrefix();
		}
		return result;
	}
	
	/**
	 * return array of pathIds which are children of the root specified;
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param root String; root node path 
	 * @return Set&lt;Integer&gt;- set of registered pathIds who are direct or indirect children of the parent path provided
	 */
	public Set<Integer> getPathElements(int typeId, String root) {

		logger.trace("getPathElements.enter; got path: {}, type: {}", root, typeId);
		Set<Integer> result = new HashSet<Integer>();

		Path xPath = getPathCache().get(root);
		if (xPath != null) {
			int pId = xPath.getPathId();
			while (pId <= xPath.getPostId()) {
				result.add(pId);
				pId++;
			}
		}
		logger.trace("getPathElements.exit; returning: {}", result);
		return result; 
	}

	/**
	 * returns document type ID for the root document element specified. root is a long
	 * path representation like {@literal "/{http://tpox-benchmark.com/security}Security"}
	 * 
	 * returns -1 in case when root path is not registered in docType cache yet;
	 * 
	 * @param root String; the document root path
	 * @return document typeId registered for the root path  
	 */
	public int getDocumentType(String root) {
		// 
		logger.trace("getDocumentType.enter; got path: {}", root);
		root = normalizePath(root);
		logger.trace("getDocumentType; normalized path: {}", root);

		int result = WRONG_PATH;
		DocumentType xdt = getTypeCache().get(root);
		if (xdt != null) {
			result = xdt.getTypeId();
		} else {
			logger.debug("getDocumentType; type not found; keys: {}; types: {}", getTypeCache().keySet(), getTypeCache().values());
			// throw XDMException ?
		}
		logger.trace("getDocumentType.exit; returning: {}", result);
		return result;
	}
	
	/**
	 * returns document root path like {@literal "/{http://tpox-benchmark.com/security}Security"}
	 * for the typeId specified
	 * 
	 * @param typeId int; the document's type id
	 * @return String path registered for the type id  
	 */
	public String getDocumentRoot(int typeId) {
		DocumentType type = getDocumentTypeById(typeId);
		return type.getRootPath();
	}

	/**
	 * returns document type ID for the root document element specified. root is a long
	 * path representation like {@literal "/{http://tpox-benchmark.com/security}Security"}
	 * 
	 * returns new typeId in case when root path is not registered in docType cache yet;
	 * 
	 * @param root String; the document root path
	 * @return int document typeId registered for the root path  
	 */
	public int translateDocumentType(String root) {
		//
		logger.trace("translateDocumentType.enter; got path: {}", root);
		root = normalizePath(root);
		logger.trace("translateDocumentType; normalized path: {}", root);

		DocumentType xdt = (DocumentType) getTypeCache().get(root);
		if (xdt == null) {
			int typeId = getTypeGen().next().intValue();
			xdt = new DocumentType(typeId, root); 
			xdt = putIfAbsent(getTypeCache(), root, xdt);
		}
		int result = xdt.getTypeId();
		logger.trace("translateDocumentType.exit; returning: {}", result);
		return result;
	}

	protected Path addDictionaryPath(int typeId, String path, NodeKind kind, 
			int dataType, Occurrence occurrence) throws BagriException {

		Path xpath = getPathCache().get(path);
		if (xpath == null) {
			int pathId = getPathGen().next().intValue();
			xpath = new Path(path, typeId, kind, pathId, 0, pathId, dataType, occurrence); // specify parentId, postId at normalization phase
			Path xp2 = putIfAbsent(getPathCache(), path, xpath);
			if (xp2.getPathId() == pathId) {
				DocumentType type = getDocumentTypeById(typeId);
				if (type == null) {
					throw new BagriException("document type for typeId " + typeId + " is not registered yet",
							BagriException.ecModel);
				}
				if (type.isNormalized()) {
					type.setNormalized(false);
					getTypeCache().put(type.getRootPath(), type);
				}
			} else {
				xpath = xp2;
			}
		}
		return xpath;
	}

	/**
	 * normalizes all registered paths belonging to the document type id. 
	 * i.e. set their parentId and pathId attributes properly  
	 * 
	 * @param typeId int; the document's type id  
	 * @throws BagriException in case of any error 
	 */
	public void normalizeDocumentType(int typeId) throws BagriException {

		// TODO: do this via EntryProcessor ?
		logger.trace("normalizeDocumentType.enter; got typeId: {}", typeId);

		DocumentType type = getDocumentTypeById(typeId);
		if (type == null) {
			throw new BagriException("type ID \"" + typeId + "\" not registered yet", BagriException.ecModel);
		}
		
		if (type.isNormalized()) {
			logger.trace("normalizeDocumentType; already normalized (1): {}", type);
			return;
		}
		
		int cnt = 0;
		final String root = type.getRootPath();
		boolean locked = false;
		try {
			locked = lock(getTypeCache(), root);
			if (!locked) {
				logger.info("normalizeDocumentType; Can't get lock on document-type {} for normalization, " +
							"thus it is being normalized by someone else.", type);
				return;
			}
			
			// it can be already normalized after we get lock!
			type = getDocumentTypeById(typeId);
			if (type.isNormalized()) {
				logger.trace("normalizeDocumentType; already normalized (2): {}", type);
				return;
			}
			
			cnt = normalizeDocPath(type.getTypeId());
			type.setNormalized(true);
			getTypeCache().put(root, type);
		} finally {
			if (locked) {
				unlock(getTypeCache(), root);
			}
		}
		logger.trace("normalizeDocumentType.exit; typeId: {}; normalized {} path elements", typeId, cnt);
	}
	
	private int normalizeDocPath(int typeId) {
		Set<Map.Entry<String, Path>> entries = getTypedPathEntries(typeId); 
		
		TreeMap<Integer, Path> elts = new TreeMap<Integer, Path>(); //entries.size());
		for (Map.Entry<String, Path> path: entries) {
			elts.put(path.getValue().getPathId(), path.getValue());
		}
		
		Map<String, Path> pes = new HashMap<String, Path>(entries.size());

		Stack<Path> pathStack = new Stack<Path>();
		for (Map.Entry<Integer, Path> elt: elts.entrySet()) {
			Path path = elt.getValue();
			if (!pathStack.isEmpty()) {
				Path top = pathStack.peek(); 
				if (path.getPath().startsWith(top.getPath())) {
					path.setParentId(top.getPathId());
				} else {
					int lastId = top.getPathId();
					while (top != null && !path.getPath().startsWith(top.getPath())) {
						top.setPostId(lastId);
						pathStack.pop();
						if (pathStack.isEmpty()) {
							top = null;
						} else {
							top = pathStack.peek();
						}
					}
				}				
			}
			pathStack.push(path);
			pes.put(path.getPath(), path);
		}

		Path top = pathStack.peek(); 
		int lastId = top.getPathId();
		while (!pathStack.isEmpty()) {
			top = pathStack.pop();
			top.setPostId(lastId);
		}

		getPathCache().putAll(pes);
		return pes.size();
	}
	
	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to an array of registered pathIds which conforms to the regex specified
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param regex String; regex pattern 
	 * @return Set&lt;Integer&gt;- set of registered pathIds conforming to the pattern provided
	 */
	public Set<Integer> translatePathFromRegex(int typeId, String regex) {

		logger.trace("translatePathFromRegex.enter; got regex: {}, type: {}", regex, typeId);
		Set<Map.Entry<String, Path>> entries = getTypedPathWithRegex(regex, typeId); 
		Set<Integer> result = new HashSet<Integer>(entries.size());
		for (Map.Entry<String, Path> e: entries) {
			logger.trace("translatePathFromRegex; path found: {}", e.getValue());
			result.add(e.getValue().getPathId());
		}

		logger.trace("translatePathFromRegex.exit; returning: {}", result);
		return result;
	}

	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to Collection of registered path which conforms to the regex specified
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param regex String; regex pattern 
	 * @return Set&lt;String&gt;- set of registered paths conforming to the pattern provided
	 */
	public Collection<String> getPathFromRegex(int typeId, String regex) {

		logger.trace("getPathFromRegex.enter; got regex: {}, type: {}", regex, typeId);
		Set<Map.Entry<String, Path>> entries = getTypedPathWithRegex(regex, typeId); 
		List<String> result = new ArrayList<String>(entries.size());
		for (Map.Entry<String, Path> e: entries) {
			logger.trace("getPathFromRegex; path found: {}", e.getValue());
			result.add(e.getKey());
		}
		logger.trace("getPathFromRegex.exit; returning: {}", result);
		return result;
	}
	
	protected int[] fromCollection(Collection<Integer> from) {
		int idx = 0;
		int[] result = new int[from.size()];
		for (Integer i: from) {
			result[idx++] = i;
		}
		return result;
	}

	/**
	 * registers bunch of node path's specified in the XML schema (XSD)
	 * 
	 * Note: subject to move out to Parser API
	 * 
	 * @param schema String; schema in plain text  
	 * @throws BagriException in case of any error
	 */
	public void registerSchema(String schema) throws BagriException {
		
		XSImplementation impl = (XSImplementation)
				new DOMXSImplementationSourceImpl().getDOMImplementation("XS-Loader LS");
		XSLoader schemaLoader = impl.createXSLoader(null);
		LSInput lsi = ((DOMImplementationLS) impl).createLSInput();
		lsi.setStringData(schema);
		XSModel model = schemaLoader.load(lsi);
		processModel(model);
	}

	/**
	 * registers bunch of schemas located in the schemaUri folder   
	 * 
	 * Note: subject to move out to Parser API
	 * 
	 * @param schemasUri String; the folder containing schemas to register  
	 * @throws BagriException in case of any error
	 */
	public void registerSchemas(String schemasUri) throws BagriException {

		XSImplementation impl = (XSImplementation) new DOMXSImplementationSourceImpl().getDOMImplementation("XS-Loader LS");
		XSLoader schemaLoader = impl.createXSLoader(null);
		LSInput lsi = ((DOMImplementationLS) impl).createLSInput();
		lsi.setSystemId(schemasUri);
		XSModel model = schemaLoader.load(lsi);
		processModel(model);
	}

	/**
	 * registers bunch of schemas located in the schemaUri folder   
	 * 
	 * Note: subject to move out to Parser API
	 * 
	 * @param schemaUri String; the folder containing schemas to register  
	 * @throws BagriException in case of any error
	 */
	public void registerSchemaUri(String schemaUri) throws BagriException {

		XSImplementation impl = new XSImplementationImpl(); 
		XSLoader schemaLoader = impl.createXSLoader(null);
		XSModel model = schemaLoader.loadURI(schemaUri);
		processModel(model);
	}

	@SuppressWarnings("rawtypes")
	private List<Path> processModel(XSModel model) throws BagriException {
		
		// register namespaces
		StringList sl = model.getNamespaces();
		for (Object ns: sl) {
			String prefix = translateNamespace((String) ns);
			logger.trace("processModel; namespace: {}; {}", ns, prefix);
		}
		
		XSNamedMap elts = model.getComponents(XSConstants.ELEMENT_DECLARATION);

		// collect substitutions 
		Map<String, List<XSElementDeclaration>> substitutions = new HashMap<String, List<XSElementDeclaration>>();
		for (Object o: elts.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			XSElementDeclaration xsElement = (XSElementDeclaration) e.getValue();
			XSElementDeclaration subGroup = xsElement.getSubstitutionGroupAffiliation();
			if (subGroup != null) {
				List<XSElementDeclaration> subs = substitutions.get(subGroup.getName());
				if (subs == null) {
					subs = new ArrayList<XSElementDeclaration>();
					substitutions.put(subGroup.getName(), subs);
				}
				subs.add(xsElement);
			}
		}
		logger.trace("processModel; got substitutions: {}", substitutions.size());
		
		// process top-level elements
		for (Object o: elts.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			XSElementDeclaration xsElement = (XSElementDeclaration) e.getValue();
			if (xsElement.getTypeDefinition().getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
				// create docType for doc element
				String root = "/{" + xsElement.getNamespace() + "}" + xsElement.getName();
				int docType = translateDocumentType(root);
				// register document type..
				Path xp = translatePath(docType, "", NodeKind.document, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne); 
				logger.trace("processModel; document type: {}; got XDMPath: {}", docType, xp);
				
				String prefix = translateNamespace(xsElement.getNamespace());
				// target namespace -> default
				translatePath(docType, "/#xmlns", NodeKind.namespace, XQItemType.XQBASETYPE_QNAME, Occurrence.onlyOne); 

				// add these two??
				//xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				//xsi:schemaLocation="http://tpox-benchmark.com/security security.xsd">
				
				List<XSElementDeclaration> parents = new ArrayList<>(4);
				processElement(docType, "", xsElement, substitutions, parents, 1, 1);
				normalizeDocumentType(docType);
			}
		}
		
		return new ArrayList<Path>();
	}

	private void processElement(int docType, String path, XSElementDeclaration xsElement, 
			Map<String, List<XSElementDeclaration>> substitutions,
			List<XSElementDeclaration> parents, int minOccurs, int maxOccurs) throws BagriException {
		
		if (!xsElement.getAbstract()) {
			path += "/{" + xsElement.getNamespace() + "}" + xsElement.getName();
			Path xp = translatePath(docType, path, NodeKind.element, XQItemType.XQBASETYPE_ANYTYPE, 
					Occurrence.getOccurrence(minOccurs, maxOccurs));
			logger.trace("processElement; element: {}; type: {}; got XDMPath: {}", path, xsElement.getTypeDefinition(), xp);
		}
		
		List<XSElementDeclaration> subs = substitutions.get(xsElement.getName());
		logger.trace("processElement; got {} substitutions for element: {}", subs == null ? 0 : subs.size(), xsElement.getName());
		if (subs != null) {
			for (XSElementDeclaration sub: subs) {
				processElement(docType, path, sub, substitutions, parents, minOccurs, maxOccurs);
			}
		}

		if (parents.contains(xsElement)) {
			return;
		}
		parents.add(xsElement);
		
		if (xsElement.getTypeDefinition().getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {

			XSComplexTypeDefinition ctd = (XSComplexTypeDefinition) xsElement.getTypeDefinition();
			
			// TODO: process derivations..?

			// element's attributes
		    XSObjectList xsAttrList = ctd.getAttributeUses();
		    for (int i = 0; i < xsAttrList.getLength(); i ++) {
		        processAttribute(docType, path, (XSAttributeUse) xsAttrList.item(i));
		    }
		      
			processParticle(docType, path, ctd.getParticle(), substitutions, parents);

			if (ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE || 
					ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_MIXED) {
				path += "/text()";
				int dataType = XQItemType.XQBASETYPE_ANYTYPE;
				XSSimpleTypeDefinition std = ctd.getSimpleType();
				if (std != null) {
					dataType = getBaseType(std); 
				}
				Path xp = translatePath(docType, path, NodeKind.text, dataType, 
						Occurrence.getOccurrence(minOccurs, maxOccurs));
				logger.trace("processElement; complex text: {}; type: {}; got XDMPath: {}", 
						path, ctd.getBaseType(), xp);
			}
		} else { //if (xsElementDecl.getTypeDefinition().getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
			XSSimpleTypeDefinition std = (XSSimpleTypeDefinition) xsElement.getTypeDefinition();
			path += "/text()";
			Path xp = translatePath(docType, path, NodeKind.text, getBaseType(std), 
					Occurrence.getOccurrence(minOccurs, maxOccurs));
			logger.trace("processElement; simple text: {}; type: {}; got XDMPath: {}", path, std, xp); 
		}
		
		parents.remove(xsElement);
	}

	
    private void processAttribute(int docType, String path, XSAttributeUse xsAttribute) throws BagriException {
    	
	    path += "/@" + xsAttribute.getAttrDeclaration().getName();
	    XSSimpleTypeDefinition std = xsAttribute.getAttrDeclaration().getTypeDefinition();
	    Occurrence occurrence = Occurrence.getOccurrence(
	    		xsAttribute.getRequired() ? 1 : 0,
	    		std.getVariety() == XSSimpleTypeDefinition.VARIETY_LIST ? -1 : 1);
		Path xp = translatePath(docType, path, NodeKind.attribute, getBaseType(std), occurrence);
		logger.trace("processAttribute; attribute: {}; type: {}; got XDMPath: {}", path, std, xp); 
    }
	
	private void processParticle(int docType, String path, XSParticle xsParticle, 
			Map<String, List<XSElementDeclaration>> substitutions,
			List<XSElementDeclaration> parents) throws BagriException {
		
		if (xsParticle == null) {
			return;
		}
		
	    XSTerm xsTerm = xsParticle.getTerm();
	    
	    switch (xsTerm.getType()) {
	      case XSConstants.ELEMENT_DECLARATION:

	    	  processElement(docType, path, (XSElementDeclaration) xsTerm, substitutions, parents, 
	        		xsParticle.getMinOccurs(), xsParticle.getMaxOccurs());
	    	  break;

	      case XSConstants.MODEL_GROUP:

	    	  // this is one of the globally defined groups 
	    	  // (found in top-level declarations)

	    	  XSModelGroup xsGroup = (XSModelGroup) xsTerm;
	
		      // it also consists of particles
		      XSObjectList xsParticleList = xsGroup.getParticles();
		      for (int i = 0; i < xsParticleList.getLength(); i ++) {
		    	  XSParticle xsp = (XSParticle) xsParticleList.item(i);
		    	  processParticle(docType, path, xsp, substitutions, parents);
		      }
	
		      //...
		      break;

	      case XSConstants.WILDCARD:

	          //...
	          break;
	    }
	}
	
	private int getBaseType(XSTypeDefinition std) {
		if (xs_ns.equals(std.getNamespace())) {
			QName qn = new QName(std.getNamespace(), std.getName());
			int type = getBaseTypeForTypeName(qn);
			logger.trace("getBaseType; returning {} for type {}", type, std.getName());
			return type;
		}
		return getBaseType(std.getBaseType());
	}
	
}
