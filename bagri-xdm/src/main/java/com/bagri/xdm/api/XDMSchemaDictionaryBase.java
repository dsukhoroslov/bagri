package com.bagri.xdm.api;

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
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.domain.XDMNamespace;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;

public abstract class XDMSchemaDictionaryBase implements XDMSchemaDictionary {
	
	public static final int WRONG_PATH = -1;
	
    private final Logger logger; 
    protected static final long timeout = 100; // 100ms to wait for lock..
	
	public XDMSchemaDictionaryBase() {
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
	
	@Override
	public String normalizePath(String path) {
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

	//protected int getDictionaryPath(int typeId, String path) {
		//getLogger().trace("getDictionaryPath.enter; goth path: {}", path);

		//String result = null;
	//	XDMPath xpath = getPathCache().get(path);
	//	if (xpath != null) {
			//result = String.valueOf(xpath.getPathId());
	//		return xpath.getPathId();
	//	}
		//getLogger().trace("getDictionaryPath.exit; returning: {}", result);
	//	return WRONG_PATH; //result;
	//}
    
	@Override
	public XDMPath translatePath(int typeId, String path, XDMNodeKind kind) {
		// "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
		
		//getLogger().trace("translatePath.enter; goth path: {}", path);
		if (kind != XDMNodeKind.document) {
			if (path == null || path.length() == 0) {
				return null; //WRONG_PATH;
			}
		
			path = normalizePath(path);
		}
		XDMPath result = addDictionaryPath(typeId, path, kind); 
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

	protected XDMPath addDictionaryPath(int typeId, String path, XDMNodeKind kind) {
		//getLogger().trace("addDictionaryPath.enter; goth path: {}", path);

		XDMPath xpath = getPathCache().get(path);
		if (xpath == null) {
			int pathId = getPathGen().next().intValue();
			xpath = new XDMPath(path, typeId, kind, pathId, 0, pathId); // specify parentId, postId at normalization phase
			XDMPath xp2 = putIfAbsent(getPathCache(), path, xpath);
			if (xp2.getPathId() == pathId) {
				XDMDocumentType type = getDocumentTypeById(typeId);
				if (type == null) {
					throw new IllegalStateException("document type for typeId " + typeId + " is not registered yet");
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
	public void normalizeDocumentType(int typeId) {

		// TODO: do this via EntryProcessor ?
		getLogger().trace("normalizeDocumentType.enter; got typeId: {}", typeId);

		XDMDocumentType type = getDocumentTypeById(typeId);
		if (type == null) {
			throw new IllegalStateException("type ID \"" + typeId + "\" not registered yet");
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
	public void registerSchema(String schema) {
		
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
	public void registerSchemas(String schemasUri) {

		XSImplementation impl = (XSImplementation)
				new DOMXSImplementationSourceImpl().getDOMImplementation("XS-Loader LS");
		XSLoader schemaLoader = impl.createXSLoader(null);
		LSInput lsi = ((DOMImplementationLS) impl).createLSInput();
		lsi.setSystemId(schemasUri);
		XSModel model = schemaLoader.load(lsi);
		processModel(model);
	}

	@Override
	public void registerSchemaUri(String schemaUri) {

		//XSImplementation impl = (XSImplementation)
		//		(new DOMXSImplementationSourceImpl()	).getDOMImplementation ("XS-Loader");
		
		XSImplementation impl = new XSImplementationImpl(); 
		XSLoader schemaLoader = impl.createXSLoader(null);
		XSModel model = schemaLoader.loadURI(schemaUri);
		processModel(model);
	}

	@SuppressWarnings("rawtypes")
	private List<XDMPath> processModel(XSModel model) {
		
		// register namespaces
		StringList sl = model.getNamespaces();
		for (Object ns: sl) {
			String prefix = translateNamespace((String) ns);
			logger.trace("namespace: {}; {}", ns, prefix);
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
		
		// process top-level elements
		for (Object o: elts.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			XSElementDeclaration xsElement = (XSElementDeclaration) e.getValue();
			if (xsElement.getTypeDefinition().getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
				// create docType for doc element
				String root = "/{" + xsElement.getNamespace() + "}" + xsElement.getName();
				int docType = translateDocumentType(root);
				// register document type..
				//dict.translatePath(docType, root, XDMNodeKind.document); ??
				
				Map<XSComplexTypeDefinition, Integer> loops = new HashMap<XSComplexTypeDefinition, Integer>();
				processElement(docType, "", xsElement, substitutions, loops);
				normalizeDocumentType(docType);
			}
		}
		
		return new ArrayList<XDMPath>();
	}

	private void processElement(int docType, String path, XSElementDeclaration xsElement, 
			Map<String, List<XSElementDeclaration>> substitutions,
			Map<XSComplexTypeDefinition, Integer> loops) {
		
		if (!xsElement.getAbstract()) {
			path += "/{" + xsElement.getNamespace() + "}" + xsElement.getName();
			translatePath(docType, path, XDMNodeKind.element);
			logger.trace("\telement: {}; type: {}", path, xsElement.getType());
		}
		
		List<XSElementDeclaration> subs = substitutions.get(xsElement.getName());
		if (subs != null) {
			for (XSElementDeclaration sub: subs) {
				processElement(docType, path, sub, substitutions, loops);
			}
		}

		if (xsElement.getTypeDefinition().getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {

			XSComplexTypeDefinition ctd = (XSComplexTypeDefinition) xsElement.getTypeDefinition();
			
			// todo: process derivations..?
			
			Integer cnt = loops.get(ctd);
			if (cnt == null) {
				loops.put(ctd, 1);
			} else if (cnt > 1) {
				return;
			} else {
				loops.put(ctd, cnt + 1); 
			}

			// element's attributes
		    XSObjectList xsAttrList = ctd.getAttributeUses();
		    for (int i = 0; i < xsAttrList.getLength(); i ++) {
		        processAttribute(docType, path, (XSAttributeUse) xsAttrList.item(i));
		    }
		      
			processParticle(docType, path, ctd.getParticle(), substitutions, loops);

			if (ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE || 
					ctd.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_MIXED) {
				path += "/text()";
				translatePath(docType, path, XDMNodeKind.text);
				logger.trace("\tcomplex text: {}; type: {}", path, ctd.getBaseType());
			}
		} else { //if (xsElementDecl.getTypeDefinition().getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
			XSSimpleTypeDefinition std = (XSSimpleTypeDefinition) xsElement.getTypeDefinition();
			path += "/text()";
			translatePath(docType, path, XDMNodeKind.text);
			logger.trace("\tsimple text: {}; type: {}", path, std.getBaseType());
		}
		
	}

	
    private void processAttribute(int docType, String path, XSAttributeUse xsAttribute) {
    	
	    path += "/@" + xsAttribute.getAttrDeclaration().getName();
		translatePath(docType, path, XDMNodeKind.attribute);
		logger.trace("\tattribute: {}", path);
    }
	
	/**
	 * Process particle
	 */
	private void processParticle(int docType, String path, XSParticle xsParticle, 
			Map<String, List<XSElementDeclaration>> substitutions,
			Map<XSComplexTypeDefinition, Integer> loops) {
		
		if (xsParticle == null) {
			return;
		}
		
	    XSTerm xsTerm = xsParticle.getTerm();
	    
	    switch (xsTerm.getType()) {
	      case XSConstants.ELEMENT_DECLARATION:

	        processElement(docType, path, (XSElementDeclaration) xsTerm, substitutions, loops);
	        break;

	      case XSConstants.MODEL_GROUP:

	        // this is one of the globally defined groups 
	        // (found in top-level declarations)

	        XSModelGroup xsGroup = (XSModelGroup) xsTerm;

	        // it also consists of particles
	        XSObjectList xsParticleList = xsGroup.getParticles();
	        for (int i = 0; i < xsParticleList.getLength(); i ++) {
	        	XSParticle xsp = (XSParticle) xsParticleList.item(i);
	        	processParticle(docType, path, xsp, substitutions, loops);
	        }

	        //...
	        break;

	      case XSConstants.WILDCARD:

	        //...
	        break;
	    }
	}
	
	public Collection<XDMDocumentType> getDocumentTypes() {
		Map<String, XDMDocumentType> typeCache = getTypeCache();
		return typeCache.values();
	}
	
}
