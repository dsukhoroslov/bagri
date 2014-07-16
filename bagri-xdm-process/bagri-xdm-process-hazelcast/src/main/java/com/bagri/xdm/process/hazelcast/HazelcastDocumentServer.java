package com.bagri.xdm.process.hazelcast;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.access.hazelcast.data.DataDocumentKey;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
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
    
    @Override
	public Collection<String> buildDocument(int docType, String template, Map<String, String> params, Set entries) {
    	Set<Long> docIds = (Set<Long>) entries;
        logger.trace("buildDocument.enter; docIds: {}", docIds.size());
		long stamp = System.currentTimeMillis();
        Collection<String> result = new ArrayList<String>(docIds.size());
		
		Member local = hzInstance.getCluster().getLocalMember();
		for (Iterator<Long> itr = docIds.iterator(); itr.hasNext(); ) {
			Long docId = itr.next();
			if (!local.equals(hzInstance.getPartitionService().getPartition(docId).getOwner())) {
				itr.remove();
		        logger.trace("buildDocument; docId {} removed", docId);
			} else {
				StringBuilder buff = new StringBuilder(template);
				for (Map.Entry<String, String> param: params.entrySet()) {
					String key = param.getKey();
					String str = buildElement(xdmCache, param.getValue(), docId, docType);
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

}
