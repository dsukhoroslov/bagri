package com.bagri.xdm.cache.hazelcast.impl;

import java.io.IOException;
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
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.xquery.XQException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.manage.JMXUtils;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathExpression;

import static com.bagri.xdm.client.common.XDMCacheConstants.*;

import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.cache.api.XDMIndexManagement;
import com.bagri.xdm.cache.common.XDMDocumentManagementServer;
import com.bagri.xdm.client.common.XDMCacheConstants;
import com.bagri.xdm.client.common.impl.XDMModelManagementBase;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.client.xml.XDMStaxParser;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMIndexedValue;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.ExceptionIterator;
import com.bagri.xquery.saxon.XQProcessorServer;
import com.hazelcast.core.BaseMap;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.mapreduce.aggregation.Aggregation;
import com.hazelcast.mapreduce.aggregation.Aggregations;
import com.hazelcast.mapreduce.aggregation.Supplier;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentManagementImpl extends XDMDocumentManagementServer {
	
    private HazelcastInstance hzInstance;
    //private XDMQueryManagement queryManager;
    private IndexManagementImpl indexManager;
    private TransactionManagementImpl txManager;

    private IdGenerator<Long> docGen;
    private Map<Long, Source> srcCache;
    private IMap<Long, String> xmlCache;
	private IMap<Long, XDMDocument> xddCache;
    private IMap<XDMDataKey, XDMElements> xdmCache;

    IMap<Long, String> getXmlCache() {
    	return xmlCache;
    }

    IMap<Long, XDMDocument> getDocumentCache() {
    	return xddCache;
    }

    IMap<XDMDataKey, XDMElements> getElementCache() {
    	return xdmCache;
    }
    
    public void setDocumentIdGenerator(IdGenerator docGen) {
    	this.docGen = docGen;
    }
    
    public void setXddCache(IMap<Long, XDMDocument> cache) {
    	this.xddCache = cache;
    }

    public void setXdmCache(IMap<XDMDataKey, XDMElements> cache) {
    	this.xdmCache = cache;
    }

    public void setXmlCache(IMap<Long, String> cache) {
    	this.xmlCache = cache;
    	this.srcCache = new ConcurrentHashMap<Long, Source>();
    }
    
    //@Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
	}
	
    public void setIndexManager(IndexManagementImpl indexManager) {
    	this.indexManager = indexManager;
    }
    
    public void setTxManager(TransactionManagementImpl txManager) {
    	this.txManager = txManager;
    }

    //public void setQueryManager(XDMQueryManagement xqManager) {
    //	this.queryManager = xqManager;
    //}

	@Override
	public Collection<String> buildDocument(Set<Long> docIds, String template, Map<String, String> params) {
        logger.trace("buildDocument.enter; docIds: {}", docIds.size());
		long stamp = System.currentTimeMillis();
        Collection<String> result = new ArrayList<String>(docIds.size());
		
		for (Iterator<Long> itr = docIds.iterator(); itr.hasNext(); ) {
			Long docId = itr.next();
			if (hzInstance.getPartitionService().getPartition(docId).getOwner().localMember()) {
				StringBuilder buff = new StringBuilder(template);
				for (Map.Entry<String, String> param: params.entrySet()) {
					String key = param.getKey();
					XDMDocument doc = xddCache.get(docId);
					String str = buildElement(xdmCache, param.getValue(), doc.getDocumentId(), doc.getTypeId());
					while (true) {
						int idx = buff.indexOf(key);
				        //logger.trace("aggregate; searching key: {} in buff: {}; result: {}", new Object[] {key, buff, idx});
						if (idx < 0) break;
						buff.replace(idx, idx + key.length(), str);
				        //logger.trace("aggregate; replaced key: {} with {}", key, str);
					}
				}
				result.add(buff.toString());
			} else {
				// remove is not supported by the HZ iterator provided! 
				// actually, don't think we have to do it at all..
				//itr.remove();
		        logger.debug("buildDocument; docId {} is not local, processing skipped", docId);
			}
		}
        
		stamp = System.currentTimeMillis() - stamp;
        logger.trace("buildDocument.exit; time taken: {}; returning: {}", stamp, result.size()); 
        return result;
	}
    
    private Set<XDMDataKey> getDocumentElementKeys(String path, long docId, int docType) {
    	Set<Integer> parts = model.getPathElements(docType, path);
    	Set<XDMDataKey> keys = new HashSet<XDMDataKey>(parts.size());
    	// not all the path keys exists as data key for particular document!
    	for (Integer part: parts) {
    		keys.add(factory.newXDMDataKey(docId, part));
    	}
    	return keys;
    }

    public Collection<XDMElements> getDocumentElements(long docId) {
		XDMDocument doc = getDocument(docId);
		if (doc == null) {
			return null;
		}

		int typeId = doc.getTypeId();
		Set<XDMDataKey> keys = getDocumentElementKeys(model.getDocumentRoot(typeId), docId, typeId);
		Map<XDMDataKey, XDMElements> elements = xdmCache.getAll(keys);
		return elements.values();
    }
    
    private String buildElement(BaseMap dataMap, String path, long docId, int docType) {
    	Set<XDMDataKey> xdKeys = getDocumentElementKeys(path, docId, docType);
       	return buildXml(((IMap) dataMap).getAll(xdKeys));
    }
    
    public XDMDocument createDocument(String uri, String xml) {
    	
		long docId = docGen.next(); 
		return createDocument(new AbstractMap.SimpleEntry(docId, null), uri, xml);
    }
    
	//@Override
	public XDMDocument createDocument(Entry<Long, XDMDocument> entry, String uri, String xml) {
		logger.trace("createDocument.enter; entry: {}", entry);

		// TODO: move this out & refactor ?
		XDMStaxParser parser = new XDMStaxParser(model);
		List<XDMData> data;
		try {
			data = parser.parse(xml);
		} catch (IOException | XMLStreamException ex) {
			logger.debug("createDocument.error", ex); 
			throw new IllegalArgumentException(ex);
		}

		Long docId = entry.getKey();
		int docType = loadElements(docId, data); 
		if (docType >= 0) {
			String user = JMXUtils.getCurrentUser();
			XDMDocument doc = new XDMDocument(docId, uri, docType, user, txManager.getCurrentTxId()); // + version, createdAt, encoding
			xddCache.set(docId, doc);
			xmlCache.set(docId, xml);
			logger.trace("createDocument.exit; returning: {}", doc);
			return doc;
		} else {
			logger.warn("createDocument.exit; the document is not valid as it has no root element, returning null");
			return null;
		}
	}
	
	public int loadElements(long docId, List<XDMData> data) {
		
		long stamp = System.currentTimeMillis();
		XDMData root = getDataRoot(data);
		if (root != null) {
			int docType = model.translateDocumentType(root.getPath());
			Map<XDMDataKey, XDMElements> elements = new HashMap<XDMDataKey, XDMElements>(data.size());
			
			for (XDMData xdm: data) {
				XDMDataKey xdk = factory.newXDMDataKey(docId, xdm.getPathId());
				XDMElements xdes = elements.get(xdk);
				if (xdes == null) {
					xdes = new XDMElements(xdk.getPathId(), null);
					elements.put(xdk, xdes);
				}
				xdes.addElement(xdm.getElement());
				indexManager.addIndex(docId, xdm.getPathId(), xdm.getValue());
			}
			xdmCache.putAll(elements);
			
			stamp = System.currentTimeMillis() - stamp;
			logger.trace("loadElements; cached {} elements for docId: {}; time taken: {}", 
					elements.size(), docId, stamp);
			model.normalizeDocumentType(docType);
			return docType;
		}
		return XDMModelManagementBase.WRONG_PATH;
	}
	
	int indexElements(int docType, int pathId) {
		Set<Long> docIds = getDocumentsOfType(docType);
		int cnt = 0;
		for (Long docId: docIds) {
			XDMDataKey xdk = factory.newXDMDataKey(docId, pathId);
			XDMElements elts = xdmCache.get(xdk);
			if (elts != null) {
				for (XDMElement elt: elts.getElements()) {
					indexManager.addIndex(docId, pathId, elt.getValue());
					cnt++;
				}
			}
		}
		return cnt;
	}

	int deindexElements(int docType, int pathId) {
		Set<Long> docIds = getDocumentsOfType(docType);
		int cnt = 0;
		for (Long docId: docIds) {
			XDMDataKey xdk = factory.newXDMDataKey(docId, pathId);
			XDMElements elts = xdmCache.get(xdk);
			if (elts != null) {
				for (XDMElement elt: elts.getElements()) {
					indexManager.dropIndex(docId, pathId, elt.getValue());
					cnt++;
				}
			}
		}
		return cnt;
	}
	
	private Set<Long> getDocumentsOfType(int docType) {
   		Predicate<Long, XDMDocument> f = Predicates.equal("typeId", docType);
		return xddCache.keySet(f);
	}
	
	//@Override
	public void deleteDocument(Entry<Long, XDMDocument> entry) {

		Long docId = entry.getKey();
		logger.trace("deleteDocument.enter; docId: {}", docId);
	    boolean removed = false;
	    XDMDocument doc = xddCache.remove(docId);
	    if (doc != null) {
	    	deleteDocumentElements(docId, doc.getTypeId());
			xmlCache.delete(docId);
			srcCache.remove(docId);
	        removed = true;
	    //} else { 
		//	throw new IllegalStateException("Document Entry with id " + entry.getKey() + " not found");
	    }
		logger.trace("deleteDocument.exit; removed: {}", removed);
	}
	
	private void deleteDocumentElements(long docId, int typeId) {

    	int cnt = 0;
    	//Set<XDMDataKey> localKeys = xdmCache.localKeySet();
    	Collection<XDMPath> allPaths = model.getTypePaths(typeId);
		logger.trace("deleteDocumentElements; got {} possible paths to remove; xdmCache size: {}", 
				allPaths.size(), xdmCache.size());
		int iCnt = 0;
        for (XDMPath path: allPaths) {
        	int pathId = path.getPathId();
        	XDMDataKey dKey = factory.newXDMDataKey(docId, pathId);
        	if (indexManager.isPathIndexed(pathId)) {
	       		XDMElements elts = xdmCache.remove(dKey);
	       		if (elts != null) {
	       			for (XDMElement elt: elts.getElements()) {
	       				indexManager.dropIndex(docId, pathId, elt.getValue());
	       				iCnt++;
	       			}
	       		}
        	} else {
        		xdmCache.delete(dKey);
        	}
   			cnt++;
        }
		logger.trace("deleteDocumentElements; deleted keys: {}; indexes: {}; xdmCache size after delete: {}",
				cnt, iCnt, xdmCache.size());
	}

	@Override
	public XDMDocument getDocument(long docId) {
		XDMDocument doc = xddCache.get(docId); 
		logger.trace("getDocument; returning: {}", doc);
		return doc;
	}

	//@Override
	public Long getDocumentId(String uri) {
   		Predicate<Long, XDMDocument> f = Predicates.equal("uri", uri);
		Set<Long> docKeys = xddCache.keySet(f);
		if (docKeys.size() == 0) {
			return null;
		}
		// todo: check if too many docs ??
		return docKeys.iterator().next();
	}

	@Override
	public Iterator<Long> getDocumentIds(String pattern) {
		// TODO: implement it
		return null;
	}
	
	@Override
	public String getDocumentAsString(long docId) {
		String xml = xmlCache.get(docId);
		if (xml == null) {
			XDMDocument doc = getDocument(docId);
			if (doc == null) {
				logger.info("getDocumentAsString; no document found for ID: {}", docId);
				return null;
			}
			
			// if docId is not local then buildDocument returns null!
			// query docId owner node for the XML instead
			if (hzInstance.getPartitionService().getPartition(docId).getOwner().localMember()) {
				String root = model.getDocumentRoot(doc.getTypeId());
				Map<String, String> params = new HashMap<String, String>();
				params.put(":doc", root);
				Collection<String> results = buildDocument(Collections.singleton(docId), ":doc", params);
				if (!results.isEmpty()) {
					xml = results.iterator().next();
					xmlCache.set(docId, xml);
				}
			} else {
				DocumentContentProvider xp = new DocumentContentProvider(docId);
				IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
				Future<String> future = execService.submitToKeyOwner(xp, docId);
				try {
					xml = future.get();
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("getDocumentAsString; error getting result", ex);
					// rethrow ex?
				}
			}
		}
		return xml;
	}

	@Override
	public Source getDocumentAsSource(long docId) {
		return srcCache.get(docId);
	}
	
	@Override
	public XDMDocument storeDocumentFromSource(long docId, String uri, Source source) {
		srcCache.put(docId, source);
		return xddCache.get(docId);
	}
	
	//@Override
	public XDMDocument storeDocumentFromString(String xml) {

	    return storeDocumentFromString(0, null, xml);
	}

	@Override
	public XDMDocument storeDocumentFromString(long docId, String uri, String xml) {
		// create new document version ??
		// what if we want to pass here correct URI ??
		logger.trace("storeDocument.enter; docId: {}; uri: {}; xml: {}", docId, uri, xml.length());

		boolean update = false;
		if (docId == 0) {
			docId = docGen.next();
		} else {
			update = true;
		}
		
		if (uri == null) {
			uri = "" + docId + ".xml";
		} else {
			Long existingId = getDocumentId(uri);
			if (existingId != null && existingId != docId) {
				// otherwise we'll get a situation when two different Documents
				// are stored in the same file.
				throw new IllegalArgumentException("Document with URI '" + uri + "' already exists; docId: " + existingId);
			}
		}
		
		if (update) {
		    XDMDocument doc = xddCache.get(docId);
		    if (doc != null) {
		    	deleteDocumentElements(docId, doc.getTypeId());
		    }
		}
	    return createDocument(new AbstractMap.SimpleEntry(docId, null), uri, xml);
	    // go to updateDocument ..?
	}

	@Override
	public void removeDocument(long docId) {
		//deleteDocument(new AbstractMap.SimpleEntry(docId, null));
		
		logger.trace("removeDocument.enter; docId: {}", docId);
	    XDMDocument doc = getDocument(docId);
	    boolean removed = false;
	    if (doc != null) {
			//String user = JMXUtils.getCurrentUser();
	    	doc.finishDocument(txManager.getCurrentTxId()); //, user);
	    	xddCache.put(docId, doc);
		    removed = true;
	    }
		logger.trace("removeDocument.exit; removed: {}", removed);
	}

	
}
