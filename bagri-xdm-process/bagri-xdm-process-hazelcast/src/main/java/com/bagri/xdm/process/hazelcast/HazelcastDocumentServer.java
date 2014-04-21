package com.bagri.xdm.process.hazelcast;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.bagri.common.query.ExpressionBuilder;
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
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class HazelcastDocumentServer extends XDMDocumentManagerServer {
	
    private HazelcastInstance hzInstance;
    private IdGenerator docGen;
    private IMap<Long, XDMDocument> xddCache;
    private IMap<XDMDataKey, XDMElement> xdmCache;
    
    public void setDocumentIdGenerator(IdGenerator docGen) {
    	this.docGen = docGen;
    }

    public void setXddCache(IMap<Long, XDMDocument> cache) {
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
		return createDocument(new AbstractMap.SimpleEntry(docId, null), uri, xml);
    }
    
	@Override
	public XDMDocument createDocument(Entry<Long, XDMDocument> entry, String uri, String xml) {
		logger.trace("createDocument.enter; entry: {}", entry);
		//if (docEntry.isPresent()) {
		//	throw new IllegalStateException("Document Entry with id " + entry.getKey() + " already exists");
		//}

		Long id = entry.getKey();
		List<XDMElement> data = parseDocument(xml, id);
		XDMElement root = getDocumentRoot(data);
		
		if (root != null) {
			int docType = mDictionary.translateDocumentType(root.getPath());
			
			for (Iterator<XDMElement> itr = data.iterator(); itr.hasNext();) {
				createElement(itr.next(), docType);
			}

			//XDMDocument doc = new XDMDocumentPortable(docId, uri, docType); // + version, createdAt, createdBy, encoding
			XDMDocument doc = new XDMDocument(id, uri, docType); // + version, createdAt, createdBy, encoding
			xddCache.put(id, doc);
		
			mDictionary.normalizeDocumentType(docType);
			logger.trace("createDocument.exit; returning: {}", doc);
			return doc;
		} else {
			logger.warn("createDocument.exit; the document is not valid as it has no root element, returning null");
			return null;
		}
	}

	private Object createElement(XDMElement xdm, int docType) {
		
		XDMDataKey xdk = mFactory.newXDMDataKey(xdm.getElementId(), xdm.getDocumentId());
		//Object xdk1 = ctx.getKeyToInternalConverter().convert(xdk);

		//if (xdmEntry.isPresent()) {
		if (xdmCache.containsKey(xdk)) {
			throw new IllegalStateException("XDM Entry with id " + xdk + " already exists");
		}
		xdm.setPathId(mDictionary.translatePath(docType, xdm.getPath(), XDMNodeKind.fromPath(xdm.getPath())));
		//xdmCache.put(xdk, new XDMDataPortable(xdm));
		xdmCache.put(xdk, xdm);
		//xdmEntry.setValue(xdm, false);
		//logger.trace("create.exit; stored key: {}", xdk);
		return xdm.getElementId();
	}

	
	@Override
	public void deleteDocument(Entry<Long, XDMDocument> entry) {

		long docId = entry.getKey();
		logger.trace("process.enter; entry: {}", docId);
		//if (!entry.isPresent()) {
		//	throw new IllegalStateException("Document Entry with id " + entry.getKey() + " not found");
		//}

	    boolean removed = false;
	    if (xddCache.remove(docId) != null) {
	    
	   		Predicate f = Predicates.equal("documentId", docId);
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
	public XDMDocument updateDocument(Entry<Long, XDMDocument> entry, boolean newVersion, String xml) {
		// TODO Auto-generated method stub
		return null;
	}

}
