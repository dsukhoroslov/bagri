package com.bagri.xdm.client.common.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
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

import com.bagri.common.idgen.IdGenerator;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.domain.XDMOccurence;
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.domain.XDMNamespace;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;

import static com.bagri.xdm.common.XDMConstants.xs_ns;
import static com.bagri.xqj.BagriXQUtils.getBaseTypeForTypeName;

public abstract class XDMModelManagementBase implements XDMModelManagement {
	
	public static final int WRONG_PATH = -1;
	
    private final Logger logger; 
    protected static final long timeout = 100; // 100ms to wait for lock..
	
	public XDMModelManagementBase() {
		this.logger = LoggerFactory.getLogger(this.getClass());
		//initialize();
	}
	
	/*
	 * initialize caches here
	 */
	//protected abstract void initialize();

    protected Logger getLogger() {
        return logger;
    }

	protected abstract Map<String, XDMNamespace> getNamespaceCache();
	protected abstract Map<String, XDMPath> getPathCache();
	protected abstract Map<String, XDMDocumentType> getTypeCache();
	protected abstract IdGenerator<Long> getNamespaceGen();
	protected abstract IdGenerator<Long> getPathGen();
	protected abstract IdGenerator<Long> getTypeGen();
    
	protected abstract boolean lock(Map cache, Object key); 
	protected abstract void unlock(Map cache, Object key); 
	protected abstract <K, V> V putIfAbsent(Map<K, V> cache, K key, V value);

	protected abstract XDMDocumentType getDocumentTypeById(int typeId);
	protected abstract Set getTypedPathEntries(int typeId);
	protected abstract Set getTypedPathWithRegex(String regex, int typeId);
	
	//@Override
	public String normalizePathOld(String path) {
		//getLogger().trace("normalizePath.enter; goth path: {}", path);
		// profile: it takes 1.13 ms!
		// TODO: optimize it!
		StringTokenizer tc = new StringTokenizer(path, "{}", true);
		StringBuffer buff = new StringBuffer();
		boolean isNamespace = false;
		while (tc.hasMoreTokens()) {
			String toc = tc.nextToken();
			//getLogger().trace("token: {}", toc);
			if ("{".equals(toc)) {
				isNamespace = true;
			} else if ("}".equals(toc)) {
				isNamespace = false;
			} else {
				if (isNamespace) {
					String ns = translateNamespace(toc);
					buff.append(ns).append(":");
				} else {
					buff.append(toc);
				}
			}
		}
		String result = buff.toString(); 
		//getLogger().trace("normalizePath.exit; returning: {}", result);
		return result;
	}
	
	@Override
	public String normalizePath(String path) {
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
	
	@Override
	public String translateNamespace(String namespace) {
		
		return translateNamespace(namespace, null);
	}

	@Override
	public String translateNamespace(String namespace, String prefix) {
		//getLogger().trace("getNamespacePrefix.enter; goth namespace: {}", namespace);

		XDMNamespace xns = (XDMNamespace) getNamespaceCache().get(namespace);
		if (xns == null) {
			if (prefix == null || prefix.isEmpty()) {
				prefix = "ns" + getNamespaceGen().next();
			}
			xns = new XDMNamespace(namespace, prefix, null);
			xns = putIfAbsent(getNamespaceCache(), namespace, xns);
		}
		String result = xns.getPrefix();
		//getLogger().trace("getNamespacePrefix.exit; returning: {}", result);
		return result;
	}

	@Override
	public XDMPath getPath(String path) {
		return getPathCache().get(normalizePath(path));
	}
    
	@Override
	public XDMPath translatePath(int typeId, String path, XDMNodeKind kind, int dataType, XDMOccurence occurence) throws XDMException {
		// "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
		
		//getLogger().trace("translatePath.enter; goth path: {}", path);
		if (kind != XDMNodeKind.document) {
			if (path == null || path.length() == 0) {
				return null; //WRONG_PATH;
			}
		
			path = normalizePath(path);
		}
		XDMPath result = addDictionaryPath(typeId, path, kind, dataType, occurence); 
		//getLogger().trace("translatePath.exit; returning: {}", result);
		return result;
	}
	
	@Override
	public String getNamespacePrefix(String namespace) {
		//getLogger().trace("getNamespacePrefix.enter; goth namespace: {}", namespace);

		String result = null;
		XDMNamespace xns = getNamespaceCache().get(namespace);
		if (xns != null) {
			result = xns.getPrefix();
		}
		//getLogger().trace("getNamespacePrefix.exit; returning: {}", result);
		return result;
	}
	
	@Override
	public Set<Integer> getPathElements(int typeId, String root) {

		getLogger().trace("getPathElements.enter; got path: {}, type: {}", root, typeId);
		Set<Integer> result = new HashSet<Integer>();

		XDMPath xPath = getPathCache().get(root);
		if (xPath != null) {
			int pId = xPath.getPathId();
			while (pId <= xPath.getPostId()) {
				result.add(pId);
				pId++;
			}
		}
		
		getLogger().trace("getPathElements.exit; returning: {}", result);
		return result; //fromCollection(result);
	}

	@Override
	public int getDocumentType(String root) {
		// 
		getLogger().trace("getDocumentTypeId.enter; got path: {}", root);
		root = normalizePath(root);
		getLogger().trace("getDocumentTypeId; normalized path: {}", root);

		int result = WRONG_PATH;
		XDMDocumentType xdt = getTypeCache().get(root);
		if (xdt != null) {
			result = xdt.getTypeId();
		} else {
			if (getLogger().isTraceEnabled()) {
				getLogger().trace("getDocumentTypeId; type not found; keys: {}; types: {}", 
						getTypeCache().keySet(), getTypeCache().values());
				// throw XDMException ?
			}
		}
		getLogger().trace("getDocumentTypeId.exit; returning: {}", result);
		return result;
	}
	
	@Override
	public String getDocumentRoot(int typeId) {
		XDMDocumentType type = getDocumentTypeById(typeId);
		return type.getRootPath();
	}

	@Override
	public int translateDocumentType(String root) {
		//
		getLogger().trace("translateDocumentType.enter; got path: {}", root);
		root = normalizePath(root);
		getLogger().trace("translateDocumentType; normalized path: {}", root);

		XDMDocumentType xdt = (XDMDocumentType) getTypeCache().get(root);
		if (xdt == null) {
			int typeId = getTypeGen().next().intValue();
			xdt = new XDMDocumentType(typeId, root); 
			xdt = putIfAbsent(getTypeCache(), root, xdt);
		}
		int result = xdt.getTypeId();
		getLogger().trace("translateDocumentType.exit; returning: {}", result);
		return result;
	}

	protected XDMPath addDictionaryPath(int typeId, String path, XDMNodeKind kind, 
			int dataType, XDMOccurence occurence) throws XDMException {
		//getLogger().trace("addDictionaryPath.enter; goth path: {}", path);

		XDMPath xpath = getPathCache().get(path);
		if (xpath == null) {
			int pathId = getPathGen().next().intValue();
			xpath = new XDMPath(path, typeId, kind, pathId, 0, pathId, dataType, occurence); // specify parentId, postId at normalization phase
			XDMPath xp2 = putIfAbsent(getPathCache(), path, xpath);
			if (xp2.getPathId() == pathId) {
				XDMDocumentType type = getDocumentTypeById(typeId);
				if (type == null) {
					throw new XDMException("document type for typeId " + typeId + " is not registered yet",
							XDMException.ecModel);
				}
				if (type.isNormalized()) {
					type.setNormalized(false);
					getTypeCache().put(type.getRootPath(), type);
				}
			} else {
				xpath = xp2;
			}
		}
		//int result = xpath.getPathId();
		//getLogger().trace("addDictionaryPath.exit; returning: {}", result);
		return xpath;
	}

	@Override
	public void normalizeDocumentType(int typeId) throws XDMException {

		// TODO: do this via EntryProcessor ?
		getLogger().trace("normalizeDocumentType.enter; got typeId: {}", typeId);

		XDMDocumentType type = getDocumentTypeById(typeId);
		if (type == null) {
			throw new XDMException("type ID \"" + typeId + "\" not registered yet", XDMException.ecModel);
		}
		
		if (type.isNormalized()) {
			getLogger().trace("normalizeDocumentType; already normalized (1): {}", type);
			return;
		}
		
		int cnt = 0;
		final String root = type.getRootPath();
		boolean locked = false;
		try {
			locked = lock(getTypeCache(), root);
			if (!locked) {
				getLogger().info("normalizeDocumentType; Can't get lock on document-type {} for normalization, " +
							"thus it is being normalized by someone else.", type);
				return;
			}
			
			// it can be already normalized after we get lock!
			type = getDocumentTypeById(typeId);
			if (type.isNormalized()) {
				getLogger().trace("normalizeDocumentType; already normalized (2): {}", type);
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
		getLogger().trace("normalizeDocumentType.exit; typeId: {}; normalized {} path elements", typeId, cnt);
	}
	
	private int normalizeDocPath(int typeId) {
		Set<Map.Entry<String, XDMPath>> entries = getTypedPathEntries(typeId); 
		
		TreeMap<Integer, XDMPath> elts = new TreeMap<Integer, XDMPath>(); //entries.size());
		for (Map.Entry<String, XDMPath> path: entries) {
			elts.put(path.getValue().getPathId(), path.getValue());
		}
		
		Map<String, XDMPath> pes = new HashMap<String, XDMPath>(entries.size());

		Stack<XDMPath> pathStack = new Stack<XDMPath>();
		for (Map.Entry<Integer, XDMPath> elt: elts.entrySet()) {
			XDMPath path = elt.getValue();
			if (!pathStack.isEmpty()) {
				XDMPath top = pathStack.peek(); 
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

		XDMPath top = pathStack.peek(); 
		int lastId = top.getPathId();
		while (!pathStack.isEmpty()) {
			top = pathStack.pop();
			top.setPostId(lastId);
		}

		getPathCache().putAll(pes);
		return pes.size();
	}
	
	@Override
	public Set<Integer> translatePathFromRegex(int typeId, String regex) {

		getLogger().trace("translatePathFromRegex.enter; got regex: {}, type: {}", regex, typeId);
		
		//Filter f = new AndFilter(new RegexFilter(new KeyExtractor(IdentityExtractor.INSTANCE), regex), 
		//		new EqualsFilter("getTypeId", typeId));
		//Filter f = new RegexFilter(new KeyExtractor(IdentityExtractor.INSTANCE), regex); 

		Set<Map.Entry> entries = getTypedPathWithRegex(regex, typeId); //NamedCache(pathCache).entrySet(f);
		Set<Integer> result = new HashSet<Integer>(entries.size());
		for (Map.Entry<String, XDMPath> e: entries) {
			getLogger().trace("translatePathFromRegex; path found: {}", e.getValue());
			result.add(e.getValue().getPathId());
		}

		getLogger().trace("translatePathFromRegex.exit; returning: {}", result);
		return result;
	}

	@Override
	public Collection<String> getPathFromRegex(int typeId, String regex) {

		getLogger().trace("getPathFromRegex.enter; got regex: {}, type: {}", regex, typeId);
		
		//Filter f = new AndFilter(new RegexFilter(new KeyExtractor(IdentityExtractor.INSTANCE), regex), 
		//		new EqualsFilter("getTypeId", typeId));

		Set<Map.Entry> entries = getTypedPathWithRegex(regex, typeId); //NamedCache(pathCache).entrySet(f);
		List<String> result = new ArrayList<String>(entries.size());
		for (Map.Entry<String, XDMPath> e: entries) {
			getLogger().trace("getPathFromRegex; path found: {}", e.getValue());
			result.add(e.getKey());
		}
		getLogger().trace("getPathFromRegex.exit; returning: {}", result);
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

	
	@Override
	public void registerSchema(String schema) throws XDMException {
		
		XSImplementation impl = (XSImplementation)
				new DOMXSImplementationSourceImpl().getDOMImplementation("XS-Loader LS");
		XSLoader schemaLoader = impl.createXSLoader(null);
		LSInput lsi = ((DOMImplementationLS) impl).createLSInput();
		lsi.setStringData(schema);
		//LSInputList input = impl.createLSInputList(new LSInput[] {lsi});
		//XSModel model = schemaLoader.loadInputList(input);
		XSModel model = schemaLoader.load(lsi);
		processModel(model);
	}

	//@Override
	public void registerSchemas(String schemasUri) throws XDMException {

		XSImplementation impl = (XSImplementation)
				new DOMXSImplementationSourceImpl().getDOMImplementation("XS-Loader LS");
		XSLoader schemaLoader = impl.createXSLoader(null);
		LSInput lsi = ((DOMImplementationLS) impl).createLSInput();
		lsi.setSystemId(schemasUri);
		XSModel model = schemaLoader.load(lsi);
		processModel(model);
	}

	@Override
	public void registerSchemaUri(String schemaUri) throws XDMException {

		//XSImplementation impl = (XSImplementation)
		//		(new DOMXSImplementationSourceImpl()	).getDOMImplementation ("XS-Loader");
		
		XSImplementation impl = new XSImplementationImpl(); 
		XSLoader schemaLoader = impl.createXSLoader(null);
		XSModel model = schemaLoader.loadURI(schemaUri);
		processModel(model);
	}

	@SuppressWarnings("rawtypes")
	private List<XDMPath> processModel(XSModel model) throws XDMException {
		
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
				XDMPath xp = translatePath(docType, "", XDMNodeKind.document, XQItemType.XQBASETYPE_ANYTYPE, XDMOccurence.onlyOne); 
				logger.trace("processModel; document type: {}; got XDMPath: {}", docType, xp);
				
				String prefix = translateNamespace(xsElement.getNamespace());
				// target namespace -> default
				translatePath(docType, "/#xmlns", XDMNodeKind.namespace, XQItemType.XQBASETYPE_QNAME, XDMOccurence.onlyOne); 

				// add these two??
				//xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				//xsi:schemaLocation="http://tpox-benchmark.com/security security.xsd">
				
				List<XSElementDeclaration> parents = new ArrayList<>(4);
				processElement(docType, "", xsElement, substitutions, parents, 1, 1);
				normalizeDocumentType(docType);
			}
		}
		
		return new ArrayList<XDMPath>();
	}

	private void processElement(int docType, String path, XSElementDeclaration xsElement, 
			Map<String, List<XSElementDeclaration>> substitutions,
			List<XSElementDeclaration> parents, int minOccurence, int maxOccurence) throws XDMException {
		
		if (!xsElement.getAbstract()) {
			path += "/{" + xsElement.getNamespace() + "}" + xsElement.getName();
			XDMPath xp = translatePath(docType, path, XDMNodeKind.element, XQItemType.XQBASETYPE_ANYTYPE, 
					XDMOccurence.getOccurence(minOccurence, maxOccurence));
			logger.trace("processElement; element: {}; type: {}; got XDMPath: {}", path, xsElement.getTypeDefinition(), xp);
		}
		
		List<XSElementDeclaration> subs = substitutions.get(xsElement.getName());
		logger.trace("processElement; got {} substitutions for element: {}", subs == null ? 0 : subs.size(), xsElement.getName());
		if (subs != null) {
			for (XSElementDeclaration sub: subs) {
				processElement(docType, path, sub, substitutions, parents, minOccurence, maxOccurence);
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
				XDMPath xp = translatePath(docType, path, XDMNodeKind.text, dataType, 
						XDMOccurence.getOccurence(minOccurence, maxOccurence));
				logger.trace("processElement; complex text: {}; type: {}; got XDMPath: {}", 
						path, ctd.getBaseType(), xp);
			}
		} else { //if (xsElementDecl.getTypeDefinition().getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
			XSSimpleTypeDefinition std = (XSSimpleTypeDefinition) xsElement.getTypeDefinition();
			path += "/text()";
			XDMPath xp = translatePath(docType, path, XDMNodeKind.text, getBaseType(std), 
					XDMOccurence.getOccurence(minOccurence, maxOccurence));
			logger.trace("processElement; simple text: {}; type: {}; got XDMPath: {}", path, std, xp); 
		}
		
		parents.remove(xsElement);
	}

	
    private void processAttribute(int docType, String path, XSAttributeUse xsAttribute) throws XDMException {
    	
	    path += "/@" + xsAttribute.getAttrDeclaration().getName();
	    XSSimpleTypeDefinition std = xsAttribute.getAttrDeclaration().getTypeDefinition();
	    XDMOccurence occurence = XDMOccurence.getOccurence(
	    		xsAttribute.getRequired() ? 1 : 0,
	    		std.getVariety() == XSSimpleTypeDefinition.VARIETY_LIST ? -1 : 1);
		XDMPath xp = translatePath(docType, path, XDMNodeKind.attribute, getBaseType(std), occurence);
		logger.trace("processAttribute; attribute: {}; type: {}; got XDMPath: {}", path, std, xp); 
    }
	
	/**
	 * Process particle
	 * @throws XDMException 
	 */
	private void processParticle(int docType, String path, XSParticle xsParticle, 
			Map<String, List<XSElementDeclaration>> substitutions,
			List<XSElementDeclaration> parents) throws XDMException {
		
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
	
	public Collection<XDMDocumentType> getDocumentTypes() {
		Map<String, XDMDocumentType> typeCache = getTypeCache();
		return typeCache.values();
	}
	
	public Collection<XDMNamespace> getNamespaces() {
		Map<String, XDMNamespace> nsCache = getNamespaceCache();
		return nsCache.values();
	}
}
