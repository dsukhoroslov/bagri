package com.bagri.xdm.process.hazelcast;

import java.net.URI;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQStaticContext;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.PathExpression;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.access.hazelcast.data.DataDocumentKey;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xquery.api.XQProcessor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.Member;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class HazelcastDocumentServer extends XDMDocumentManagerServer {
	
    private HazelcastInstance hzInstance;
    private IdGenerator docGen;
    private IMap<String, XDMDocument> xddCache;
    private IMap<XDMDataKey, XDMElement> xdmCache;
    private XQProcessor xqProcessor;
    
    public void setDocumentIdGenerator(IdGenerator docGen) {
    	this.docGen = docGen;
    }
    
    public int getXddSize() {
    	return xddCache.size();
    }
    
    public int getXdmSize() {
    	return xdmCache.size();
    }
    
    public long getSchemaSize() {
    	// @TODO: do this on all cache nodes!
    	LocalMapStats stats = xddCache.getLocalMapStats();
    	long size = stats.getBackupEntryMemoryCost() + stats.getOwnedEntryMemoryCost();
    	stats = xdmCache.getLocalMapStats();
    	size += stats.getBackupEntryMemoryCost() + stats.getOwnedEntryMemoryCost();
    	return size;
    }
    
    public Map<Integer, Integer> getTypeDocuments() {
    	return Collections.EMPTY_MAP;
    }

    public Map<Integer, Integer> getTypeElements() {
    	return Collections.EMPTY_MAP;
    }

    public Map<Integer, Long> getTypeSchemaSize() {
    	return Collections.EMPTY_MAP;
    }

    public void setXddCache(IMap<String, XDMDocument> cache) {
    	this.xddCache = cache;
    }

    public void setXdmCache(IMap<XDMDataKey, XDMElement> cache) {
    	this.xdmCache = cache;
    }

    //@Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		logger.debug("setHzInstange; got instance: {}", hzInstance.getName()); 
	}
	
	public void setXQProcessor(XQProcessor xqProcessor) {
		this.xqProcessor = xqProcessor;
		xqProcessor.setXdmManager(this);
	}
    
    @Override
	public Collection<String> buildDocument(int docType, String template, Map<String, String> params, Set entries) {
    	Set<String> uris = (Set<String>) entries;
        logger.trace("buildDocument.enter; uris: {}", uris.size());
		long stamp = System.currentTimeMillis();
        Collection<String> result = new ArrayList<String>(uris.size());
		
		Member local = hzInstance.getCluster().getLocalMember();
		for (Iterator<String> itr = uris.iterator(); itr.hasNext(); ) {
			String uri = itr.next();
			// @TODO: translate it to path ???
			uri = FileUtils.uri2Path(uri);
			if (!hzInstance.getPartitionService().getPartition(uri).getOwner().localMember()) {
				itr.remove();
		        logger.trace("buildDocument; uri {} removed", uri);
			} else {
				StringBuilder buff = new StringBuilder(template);
				for (Map.Entry<String, String> param: params.entrySet()) {
					String key = param.getKey();
					XDMDocument doc = xddCache.get(uri);
					String str = buildElement(xdmCache, param.getValue(), doc.getDocumentId(), docType);
					while (true) {
						int idx = buff.indexOf(key);
				        //logger.trace("aggregate; searching key: {} in buff: {}; result: {}", new Object[] {key, buff, idx});
						if (idx < 0) break;
						buff.replace(idx, idx + key.length(), str);
				        //logger.trace("aggregate; replaced key: {} with {}", key, str);
					}
				}
				result.add(buff.toString());
			}
		}
        
		stamp = System.currentTimeMillis() - stamp;
        logger.trace("buildDocument.exit; time taken: {}; returning: {}", stamp, result.size()); 
        return result;
	}

    private String buildElement(IMap dataMap, String path, long docId, int docType) {

    	Set<Integer> parts = mDictionary.getPathElements(docType, path);
		Predicate f = Predicates.and(
				Predicates.equal("documentId", docId), 
				Predicates.in("pathId", parts.toArray(new Integer[parts.size()])));

		Set<Map.Entry> xdEntries = dataMap.entrySet(f);
       	return buildXml(xdEntries);
    }
    
    public XDMDocument createDocument(String uri, String xml) {
    	
		long docId = docGen.newId(); 
		return createDocument(new AbstractMap.SimpleEntry(uri, null), docId, xml);
    }
    
	@Override
	public XDMDocument createDocument(Entry<String, XDMDocument> entry, long docId, String xml) {
		logger.trace("createDocument.enter; entry: {}", entry);
		//if (docEntry.isPresent()) {
		//	throw new IllegalStateException("Document Entry with id " + entry.getKey() + " already exists");
		//}

		String uri = entry.getKey();
		List<XDMElement> data = parseDocument(xml, docId);
		XDMElement root = getDocumentRoot(data);
		
		if (root != null) {
			int docType = mDictionary.translateDocumentType(root.getPath());
			Map<XDMDataKey, XDMElement> elements = new HashMap<XDMDataKey, XDMElement>(data.size());
			for (Iterator<XDMElement> itr = data.iterator(); itr.hasNext();) {
				addElement(elements, itr.next(), docType);
			}
			xdmCache.putAll(elements);

			//XDMDocument doc = new XDMDocumentPortable(docId, uri, docType); // + version, createdAt, createdBy, encoding
			String user = "system"; // get current user from context somehow..
			XDMDocument doc = new XDMDocument(docId, uri, docType, user); // + version, createdAt, encoding
			xddCache.put(uri, doc);
		
			mDictionary.normalizeDocumentType(docType);
			logger.trace("createDocument.exit; returning: {}", doc);
			return doc;
		} else {
			logger.warn("createDocument.exit; the document is not valid as it has no root element, returning null");
			return null;
		}
	}

	private void addElement(Map<XDMDataKey, XDMElement> elements, XDMElement xdm, int docType) {
		
		XDMDataKey xdk = mFactory.newXDMDataKey(xdm.getElementId(), xdm.getDocumentId());
		if (xdmCache.containsKey(xdk)) {
			throw new IllegalStateException("XDM Entry with id " + xdk + " already exists");
		}
		xdm.setPathId(mDictionary.translatePath(docType, xdm.getPath(), XDMNodeKind.fromPath(xdm.getPath())));
		//xdmCache.put(xdk, new XDMDataPortable(xdm));
		//xdmCache.put(xdk, xdm);
		//xdmEntry.setValue(xdm, false);
		//logger.trace("create.exit; stored key: {}", xdk);
		elements.put(xdk, xdm);
	}

	
	@Override
	public void deleteDocument(Entry<String, XDMDocument> entry) {

		String uri = entry.getKey();
		logger.trace("process.enter; entry: {}", uri);
		//if (!entry.isPresent()) {
		//	throw new IllegalStateException("Document Entry with id " + entry.getKey() + " not found");
		//}

	    boolean removed = false;
	    XDMDocument doc = xddCache.remove(uri);
	    if (doc != null) {
	    
	   		Predicate f = Predicates.equal("documentId", doc.getDocumentId());
			Set<XDMDataKey> xdmKeys = xdmCache.keySet(f);
			logger.trace("process; got {} document elements to remove", xdmKeys.size());
			int cnt = 0;
	        for (XDMDataKey key: xdmKeys) {
	        	//xdmCache.delete(key);
	        	DataDocumentKey ddk; //) key).
	        	if (xdmCache.remove(key) != null) {
	        		cnt++;
	        	} else {
	    			logger.trace("process; data not found for key {}", key);
	    			logger.trace("process; get returns: {}", xdmCache.get(key));
	        	}
	        }
			logger.trace("process; {} document elements were removed", cnt);
	        removed = true;
	    }
        //xddCache.delete(docEntry.getKey());
		logger.trace("process.exit; removed: {}", removed);
	}

	@Override
	public XDMDocument updateDocument(Entry<String, XDMDocument> entry, boolean newVersion, String xml) {
		// TODO: not implemented yet
		return null;
	}

	@SuppressWarnings("rawtypes")
	private Predicate getValueFilter(PathExpression pex) {
		String field = "value";
		Object value = pex.getValue();
		if (value instanceof Integer) {
			field = "asInt"; 
		} else if (value instanceof Long) {
			field = "asLong";
		} else if (value instanceof Boolean) {
			field = "asBoolean";
		} else if (value instanceof Byte) {
			field = "asByte";
		} else if (value instanceof Short) {
			field = "asShort";
		} else if (value instanceof Float) {
			field = "asFloat";
		} else if (value instanceof Double) {
			field = "asDouble";
		} else {
			value = value.toString();
		}
	
		switch (pex.getCompType()) {
			case EQ: return Predicates.equal(field, (Comparable) value);
			case LE: return Predicates.lessEqual(field, (Comparable) value);
			case LT: return Predicates.lessThan(field, (Comparable) value);
			case GE: return Predicates.greaterEqual(field, (Comparable) value);
			case GT: return Predicates.greaterThan(field, (Comparable) value);
			default: return null;
		}
		
	}
	
	@Override
	protected Set<Long> queryPathKeys(Set<Long> found, PathExpression pex) {

		int pathId = -1;
		if (pex.isRegex()) {
			Set<Integer> pathIds = mDictionary.translatePathFromRegex(pex.getDocType(), pex.getRegex());
			logger.trace("queryPathKeys; regex: {}; pathIds: {}", pex.getRegex(), pathIds);
			if (pathIds.size() > 0) {
				pathId = pathIds.iterator().next();
			}
		} else {
			String path = pex.getFullPath();
			logger.trace("queryPathKeys; path: {}; comparison: {}", path, pex.getCompType());
			pathId = mDictionary.translatePath(pex.getDocType(), path, XDMNodeKind.fromPath(path));
		}
		String value = pex.getValue().toString();
		Predicate valueFilter = getValueFilter(pex);
		if (valueFilter == null) {
			throw new IllegalArgumentException("Can't construct filter for expression: " + pex);
		}

		Predicate f = Predicates.and(Predicates.equal("pathId", pathId), valueFilter);
		Set<XDMDataKey> keys = xdmCache.keySet(f);
		logger.trace("queryPathKeys; path: {}, value: {}; got keys: {}; cache size: {}", 
				new Object[] {pathId, value, keys.size(), xdmCache.size()}); 
		
		if (keys.size() > 0) {
			Set<Long> docIds = new HashSet<Long>();
			for (XDMDataKey key: keys) {
				docIds.add(key.getDocumentId());
			}
			logger.trace("queryPathKeys; old keys: {}, new keys: {}", found.size(), docIds.size());
			found.retainAll(docIds);
		} else {
			found.clear();
		}
		return found;
	}

	@Override
	public Collection<String> getDocumentURIs(ExpressionBuilder query) {
		Set<String> paths;
		if (query.getRoot() != null) {
			int typeId = query.getRoot().getDocType();
			Predicate f = Predicates.equal("typeId", typeId);
			//Set<Long> keys = new HashSet<Long>(xddCache.keySet(f));
			Collection<XDMDocument> docs = xddCache.values(f);
			if (docs.size() == 0) {
				String root = mDictionary.getDocumentRoot(typeId);
				logger.trace("getDocumentURIs; no docs found for type: {}; root: {}", typeId, root);
				docs = xddCache.values();
			}
			Set<Long> docIds = new HashSet<Long>(docs.size());
			for (XDMDocument doc: docs) {
				docIds.add(doc.getDocumentId());
			}
			docIds = queryKeys(docIds, query.getRoot());
			logger.trace("getDocumentURIs; got docIds: {}", docIds);
			f = Predicates.in("documentId", docIds.toArray(new Long[docIds.size()]));
			paths = xddCache.keySet(f);
		} else {
			// ?!?
			paths = xddCache.keySet();
		}

		Set<String> result = new HashSet<String>(paths.size()); 
		for (String path: paths) {
			result.add(Paths.get(path).toUri().toString());
		}
		return result;
	}

	@Override
	public XDMDocument getDocument(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentAsString(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XDMDocument storeDocument(String xml) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XDMDocument storeDocument(String uri, String xml) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeDocument(String uri) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<String> getXML(ExpressionBuilder query, String template, Map params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object executeXCommand(String command, Map bindings,	Map context) {
		
		logger.trace("executeXCommand.enter; command: {}; bindings: {}", command, bindings);
		Object result = null;
		try {
			result = xqProcessor.executeXCommand(command, bindings, context);
		} catch (XQException ex) {
			logger.error("executeXCommand; error: ", ex);
		}
		logger.trace("executeXCommand.exit; returning: {}", result);
		return result;
	}

	@Override
	public Object executeXQuery(String query, Map bindings,	Map context) {

		logger.trace("executeXQuery.enter; command: {}; bindings: {}", query, bindings);
		Object result = null;
		try {
			result = xqProcessor.executeXQuery(query, context);
		} catch (XQException ex) {
			logger.error("executeXQuery; error: ", ex);
		}
		logger.trace("executeXQuery.exit; returning: {}", result);
		return result;
	}

	
}
