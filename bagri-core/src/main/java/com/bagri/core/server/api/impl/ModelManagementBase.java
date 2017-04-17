package com.bagri.core.server.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	protected abstract Map<String, Path> getPathCache();
	protected abstract IdGenerator<Long> getPathGen();
    
	//protected abstract <K> boolean lock(Map<K, ?> cache, K key); 
	//protected abstract <K> void unlock(Map<K, ?> cache, K key); 
	protected abstract <K, V> V putIfAbsent(Map<K, V> cache, K key, V value);
	//protected abstract <K, V> V putPathIfAbsent(Map<K, V> cache, K key, V value);

	protected abstract Set<Map.Entry<String, Path>> getTypedPathEntries(String root);
	protected abstract Set<Map.Entry<String, Path>> getTypedPathWithRegex(String regex, String root);

	/**
	 * WRONG_PATH identifies path not existing in XDMPath dictionary
	 */
    public static final int WRONG_PATH = -1;

	private String getPathKey(String root, String path) {
		return root + ":" + path;
	}

	/**
	 * search for registered full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
	 * 
	 * @param path String; node path in Clark form
	 * @return registered {@link Path} structure if any
	 */
	public Path getPath(String root, String path) {
		String pathKey = getPathKey(root, path);
		return getPathCache().get(pathKey);
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
	public Path translatePath(String root, String path, NodeKind kind, int dataType, Occurrence occurrence) throws BagriException {
		// "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
		
		//if (kind != NodeKind.document) {
			if (path == null || path.length() == 0) {
				return null; //WRONG_PATH;
			}
		
			//path = normalizePath(path);
		//}
		Path result = addDictionaryPath(root, path, kind, dataType, occurrence); 
		return result;
	}
	
	/**
	 * 
	 * @param path the long element path
	 * @return the path root
	 */
	public String getPathRoot(String path) {
		if (path.startsWith("/{")) {
			return path.substring(0, path.indexOf("/", path.indexOf("}")));
		}
		String[] segments = path.split("/");
		if (segments.length > 1) {
			return "/" + segments[1];
		} else if (segments.length > 0) {
			return "/" + segments[0];
		}
		return null;
	}
	
	/**
	 * return array of pathIds which are children of the root specified;
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param root String; root node path 
	 * @return Set&lt;Integer&gt;- set of registered pathIds who are direct or indirect children of the parent path provided
	 */
	public Set<Integer> getPathElements(String root) {

		logger.trace("getPathElements.enter; got root: {}", root);
		Set<Integer> result = new HashSet<Integer>();
		String pathKey = getPathKey(getPathRoot(root), root);
		Path xPath = getPathCache().get(pathKey);
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

	protected Path addDictionaryPath(String root, String path, NodeKind kind, 
			int dataType, Occurrence occurrence) throws BagriException {

		String pathKey = getPathKey(root, path);
		Path xpath = getPathCache().get(pathKey);
		if (xpath == null) {
			int pathId = getPathGen().next().intValue();
			xpath = new Path(path, root, kind, pathId, 0, pathId, dataType, occurrence); 
			xpath = putIfAbsent(getPathCache(), pathKey, xpath);
		}
		return xpath;
	}
	
	public void updatePath(Path path) {
		String pathKey = getPathKey(path.getRoot(), path.getPath());
		getPathCache().put(pathKey, path);
	}

	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to an array of registered pathIds which conforms to the regex specified
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param regex String; regex pattern 
	 * @return Set&lt;Integer&gt;- set of registered pathIds conforming to the pattern provided
	 */
	public Set<Integer> translatePathFromRegex(String root, String regex) {

		logger.trace("translatePathFromRegex.enter; got regex: {}, root: {}", regex, root);
		Set<Map.Entry<String, Path>> entries = getTypedPathWithRegex(regex, root); 
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
	public Collection<String> getPathFromRegex(String root, String regex) {
		logger.trace("getPathFromRegex.enter; got regex: {}, root: {}", regex, root);
		Set<Map.Entry<String, Path>> entries = getTypedPathWithRegex(regex, root); 
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
		//StringList sl = model.getNamespaces();
		//for (Object ns: sl) {
		//	String prefix = translateNamespace((String) ns);
		//	logger.trace("processModel; namespace: {}; {}", ns, prefix);
		//}
		
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
				// register document type..
				Path xp = translatePath(root, "", NodeKind.document, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne); 
				logger.trace("processModel; document root: {}; got XDMPath: {}", root, xp);
				
				//String prefix = translateNamespace(xsElement.getNamespace());
				// target namespace -> default
				translatePath(root, "/#xmlns", NodeKind.namespace, XQItemType.XQBASETYPE_QNAME, Occurrence.onlyOne); 

				// add these two??
				//xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				//xsi:schemaLocation="http://tpox-benchmark.com/security security.xsd">
				
				List<XSElementDeclaration> parents = new ArrayList<>(4);
				processElement(root, "", xsElement, substitutions, parents, 1, 1);
				//normalizeDocumentType(docType);
			}
		}
		
		return new ArrayList<Path>();
	}

	private void processElement(String root, String path, XSElementDeclaration xsElement, 
			Map<String, List<XSElementDeclaration>> substitutions,
			List<XSElementDeclaration> parents, int minOccurs, int maxOccurs) throws BagriException {
		
		if (!xsElement.getAbstract()) {
			path += "/{" + xsElement.getNamespace() + "}" + xsElement.getName();
			Path xp = translatePath(root, path, NodeKind.element, XQItemType.XQBASETYPE_ANYTYPE, 
					Occurrence.getOccurrence(minOccurs, maxOccurs));
			logger.trace("processElement; element: {}; type: {}; got XDMPath: {}", path, xsElement.getTypeDefinition(), xp);
		}
		
		List<XSElementDeclaration> subs = substitutions.get(xsElement.getName());
		logger.trace("processElement; got {} substitutions for element: {}", subs == null ? 0 : subs.size(), xsElement.getName());
		if (subs != null) {
			for (XSElementDeclaration sub: subs) {
				processElement(root, path, sub, substitutions, parents, minOccurs, maxOccurs);
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
		        processAttribute(root, path, (XSAttributeUse) xsAttrList.item(i));
		    }
		      
			processParticle(root, path, ctd.getParticle(), substitutions, parents);

			if (ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE || 
					ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_MIXED) {
				path += "/text()";
				int dataType = XQItemType.XQBASETYPE_ANYTYPE;
				XSSimpleTypeDefinition std = ctd.getSimpleType();
				if (std != null) {
					dataType = getBaseType(std); 
				}
				Path xp = translatePath(root, path, NodeKind.text, dataType, 
						Occurrence.getOccurrence(minOccurs, maxOccurs));
				logger.trace("processElement; complex text: {}; type: {}; got XDMPath: {}", 
						path, ctd.getBaseType(), xp);
			}
		} else { //if (xsElementDecl.getTypeDefinition().getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
			XSSimpleTypeDefinition std = (XSSimpleTypeDefinition) xsElement.getTypeDefinition();
			path += "/text()";
			Path xp = translatePath(root, path, NodeKind.text, getBaseType(std), 
					Occurrence.getOccurrence(minOccurs, maxOccurs));
			logger.trace("processElement; simple text: {}; type: {}; got XDMPath: {}", path, std, xp); 
		}
		
		parents.remove(xsElement);
	}

	
    private void processAttribute(String root, String path, XSAttributeUse xsAttribute) throws BagriException {
    	
	    path += "/@" + xsAttribute.getAttrDeclaration().getName();
	    XSSimpleTypeDefinition std = xsAttribute.getAttrDeclaration().getTypeDefinition();
	    Occurrence occurrence = Occurrence.getOccurrence(
	    		xsAttribute.getRequired() ? 1 : 0,
	    		std.getVariety() == XSSimpleTypeDefinition.VARIETY_LIST ? -1 : 1);
		Path xp = translatePath(root, path, NodeKind.attribute, getBaseType(std), occurrence);
		logger.trace("processAttribute; attribute: {}; type: {}; got XDMPath: {}", path, std, xp); 
    }
	
	private void processParticle(String root, String path, XSParticle xsParticle, 
			Map<String, List<XSElementDeclaration>> substitutions,
			List<XSElementDeclaration> parents) throws BagriException {
		
		if (xsParticle == null) {
			return;
		}
		
	    XSTerm xsTerm = xsParticle.getTerm();
	    
	    switch (xsTerm.getType()) {
	      case XSConstants.ELEMENT_DECLARATION:

	    	  processElement(root, path, (XSElementDeclaration) xsTerm, substitutions, parents, 
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
		    	  processParticle(root, path, xsp, substitutions, parents);
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
