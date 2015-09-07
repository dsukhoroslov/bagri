package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.query.PathBuilder.*;
import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;

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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Source;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.common.XDMDocumentManagementServer;
import com.bagri.xdm.client.common.impl.XDMModelManagementBase;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMFragmentedDocument;
import com.bagri.xdm.domain.XDMParser;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.system.XDMFragment;
import com.bagri.xdm.system.XDMTriggerAction.Action;
import com.bagri.xdm.system.XDMTriggerAction.Scope;
import com.bagri.xquery.api.XQProcessor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentManagementImpl extends XDMDocumentManagementServer {
	
	private RepositoryImpl repo;
    private HazelcastInstance hzInstance;
    private IndexManagementImpl indexManager;
    private TransactionManagementImpl txManager;
    private TriggerManagementImpl triggerManager;

    private IdGenerator<Long> docGen;
    private Map<XDMDocumentKey, Source> srcCache;
    private IMap<XDMDocumentKey, String> xmlCache;
	private IMap<XDMDocumentKey, XDMDocument> xddCache;
    private IMap<XDMDataKey, XDMElements> xdmCache;

    public void setRepository(RepositoryImpl repo) {
    	this.repo = repo;
    	//this.model = repo.getModelManagement();
    	this.txManager = (TransactionManagementImpl) repo.getTxManagement();
    	this.triggerManager = (TriggerManagementImpl) repo.getTriggerManagement();
    }
    
    IMap<XDMDocumentKey, String> getXmlCache() {
    	return xmlCache;
    }

    IMap<XDMDocumentKey, XDMDocument> getDocumentCache() {
    	return xddCache;
    }

    IMap<XDMDataKey, XDMElements> getElementCache() {
    	return xdmCache;
    }
    
    public void setDocumentIdGenerator(IdGenerator<Long> docGen) {
    	this.docGen = docGen;
    }
    
    public void setXddCache(IMap<XDMDocumentKey, XDMDocument> cache) {
    	this.xddCache = cache;
    }

    public void setXdmCache(IMap<XDMDataKey, XDMElements> cache) {
    	this.xdmCache = cache;
    }

    public void setXmlCache(IMap<XDMDocumentKey, String> cache) {
    	this.xmlCache = cache;
    	this.srcCache = new ConcurrentHashMap<XDMDocumentKey, Source>();
    }
    
    //@Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
	}
	
    public void setIndexManager(IndexManagementImpl indexManager) {
    	this.indexManager = indexManager;
    }
    
    //public void setTxManager(TransactionManagementImpl txManager) {
    //	this.txManager = txManager;
    //}

    //public void setQueryManager(XDMQueryManagement xqManager) {
    //	this.queryManager = xqManager;
    //}

	@Override
	public Collection<String> buildDocument(Set<Long> docIds, String template, Map<String, String> params) {
        logger.trace("buildDocument.enter; docIds: {}", docIds.size());
		long stamp = System.currentTimeMillis();
        Collection<String> result = new ArrayList<String>(docIds.size());
		
		for (Iterator<Long> itr = docIds.iterator(); itr.hasNext(); ) {
			//Long docId = itr.next();
			XDMDocumentKey docKey = factory.newXDMDocumentKey(itr.next());
			if (hzInstance.getPartitionService().getPartition(docKey).getOwner().localMember()) {
				StringBuilder buff = new StringBuilder(template);
				for (Map.Entry<String, String> param: params.entrySet()) {
					String key = param.getKey();
					XDMDocument doc = xddCache.get(docKey);
					String str = buildElement(xdmCache, param.getValue(), doc.getFragments(), doc.getTypeId());
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
		        logger.debug("buildDocument; docId {} is not local, processing skipped", docKey);
			}
		}
        
		stamp = System.currentTimeMillis() - stamp;
        logger.trace("buildDocument.exit; time taken: {}; returning: {}", stamp, result.size()); 
        return result;
	}
    
    private Set<XDMDataKey> getDocumentElementKeys(String path, long[] fragments, int docType) {
    	Set<Integer> parts = model.getPathElements(docType, path);
    	Set<XDMDataKey> keys = new HashSet<XDMDataKey>(parts.size()*fragments.length);
    	// not all the path keys exists as data key for particular document!
    	for (long docKey: fragments) {
	    	for (Integer part: parts) {
	    		keys.add(factory.newXDMDataKey(docKey, part));
	    	}
    	}
    	return keys;
    }
    
    public Collection<XDMElements> getDocumentElements(long docId) {
		XDMDocument doc = getDocument(docId);
		if (doc == null) {
			return null;
		}

		int typeId = doc.getTypeId();
		Set<XDMDataKey> keys = getDocumentElementKeys(model.getDocumentRoot(typeId), 
				doc.getFragments(), typeId);
		Map<XDMDataKey, XDMElements> elements = xdmCache.getAll(keys);
		return elements.values();
    }
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String buildElement(IMap dataMap, String path, long[] fragments, int docType) {
    	Set<XDMDataKey> xdKeys = getDocumentElementKeys(path, fragments, docType);
       	return buildXml(dataMap.getAll(xdKeys));
    }
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public XDMDocument createDocument(String uri, String xml) throws XDMException {
    	
		XDMDocumentKey docKey = factory.newXDMDocumentKey(docGen.next(), 1); 
		return createDocument(new AbstractMap.SimpleEntry(docKey, null), uri, xml);
    }
	
	private String getDataFormat() {
		XQProcessor xqp = repo.getXQProcessor();
		String format = xqp.getProperties().getProperty("xdm.document.format");
		if (format != null) {
			return format;
		}
		return XDMParser.df_xml;
	}
    
	//@Override
	public XDMDocument createDocument(Entry<XDMDocumentKey, XDMDocument> entry, String uri, String xml) throws XDMException {
		logger.trace("createDocument.enter; entry: {}", entry);

		// TODO: move this out & refactor ?
		String dataFormat = getDataFormat();
		XDMParser parser = factory.newXDMParser(dataFormat, model);
		List<XDMData> data;
		try {
			data = parser.parse(xml);
		} catch (IOException ex) {
			logger.debug("createDocument.error", ex); 
			throw new XDMException(ex, XDMException.ecInOut);
		}

		XDMDocumentKey docKey = entry.getKey();
		List<Long> fragments = loadElements(docKey.getKey(), data); 
		if (fragments == null) {
			logger.warn("createDocument.exit; the document is not valid as it has no root element");
			throw new XDMException("invalid document", XDMException.ecDocument);
		} 
		int docType = fragments.get(0).intValue();
		String user = JMXUtils.getCurrentUser();
		XDMDocument doc;
		if (fragments.size() == 1) {
			doc = new XDMDocument(docKey.getDocumentId(), docKey.getVersion(), uri, docType, user, txManager.getCurrentTxId());
		} else {
			doc = new XDMFragmentedDocument(docKey.getDocumentId(), docKey.getVersion(), uri, docType, user, txManager.getCurrentTxId());
			long[] fa = new long[fragments.size()];
			fa[0] = docKey.getKey();
			for (int i=1; i < fragments.size(); i++) {
				fa[i] = fragments.get(i);
			}
			((XDMFragmentedDocument) doc).setFragments(fa);
		}
		Action action = docKey.getVersion() == 0 ? Action.insert : Action.update;
		triggerManager.applyTrigger(doc, action, Scope.before); 
		xddCache.set(docKey, doc);
		xmlCache.set(docKey, xml);
		triggerManager.applyTrigger(doc, action, Scope.after); 
		logger.trace("createDocument.exit; returning: {}", doc);
		return doc;
	}
	
	public List<Long> loadElements(long docKey, List<XDMData> data) throws XDMException {
		
		long stamp = System.currentTimeMillis();
		XDMData root = getDataRoot(data);
		if (root != null) {
			int docType = model.translateDocumentType(root.getPath());
			// normalize it ASAP !?
			model.normalizeDocumentType(docType);
			Map<XDMDataKey, XDMElements> elements = new HashMap<XDMDataKey, XDMElements>(data.size());
			
			Set<Integer> fragments = new HashSet<>();
			for (XDMFragment fragment: repo.getSchema().getFragments()) {
				int fType = model.getDocumentType(fragment.getDocumentType());
				if (fType == docType) {
					XDMPath path = model.getPath(fragment.getPath());
					if (path != null) {
						fragments.add(path.getPathId());
					} else if (isRegexPath(fragment.getPath())) {
						String nPath = model.normalizePath(fragment.getPath());
						fragments.addAll(model.translatePathFromRegex(docType, regexFromPath(nPath)));
					} else {	
						logger.info("loadElements; path not found for fragment: {}; docType: {} ({})", 
								fragment, root.getPath(), docType);
					}
				}
			}
			logger.info("loadElements; fragments found: {}; for docType: {} ({}); docKey: {}", 
					fragments, root.getPath(), docType, docKey);
			
			long fraPath = docKey;
			long fraPost = 0;
			int size = 1;
			if (fragments.size() > 0) {
				size = data.size() / fragments.size();
			}
			List<Long> result = new ArrayList<>(size);
			result.add(new Long(docType));
			for (XDMData xdm: data) {
				if (fragments.contains(xdm.getPathId())) {
					fraPath = docGen.next();
					result.add(fraPath);
					fraPost = xdm.getPostId();
				} else if (fraPost > 0 && xdm.getPathId() > fraPost) {
					fraPath = docKey;
					fraPost = 0;
				}
				XDMDataKey xdk = factory.newXDMDataKey(fraPath, xdm.getPathId());
				XDMElements xdes = elements.get(xdk);
				if (xdes == null) {
					xdes = new XDMElements(xdk.getPathId(), null);
					elements.put(xdk, xdes);
				}
				xdes.addElement(xdm.getElement());
				indexManager.addIndex(docKey, xdm.getPathId(), xdm.getPath(), xdm.getValue());
			}
			xdmCache.putAll(elements);
			
			stamp = System.currentTimeMillis() - stamp;
			logger.info("loadElements; cached {} elements for docKey: {}; fragments: {}; time taken: {}", 
					elements.size(), docKey, result.size(), stamp);
			//model.normalizeDocumentType(docType);
			return result;
		}
		return null;
	}
	
	int indexElements(int docType, int pathId) throws XDMException {
		Set<XDMDocumentKey> docKeys = getDocumentsOfType(docType);
		String path = model.getPath(pathId).getPath();
		int cnt = 0;
		for (XDMDocumentKey docKey: docKeys) {
			XDMDataKey xdk = factory.newXDMDataKey(docKey.getKey(), pathId);
			XDMElements elts = xdmCache.get(xdk);
			if (elts != null) {
				for (XDMElement elt: elts.getElements()) {
					indexManager.addIndex(docKey.getKey(), pathId, path, elt.getValue());
					cnt++;
				}
			}
		}
		return cnt;
	}

	int deindexElements(int docType, int pathId) {
		Set<XDMDocumentKey> docKeys = getDocumentsOfType(docType);
		int cnt = 0;
		for (XDMDocumentKey docKey: docKeys) {
			XDMDataKey xdk = factory.newXDMDataKey(docKey.getKey(), pathId);
			XDMElements elts = xdmCache.get(xdk);
			if (elts != null) {
				for (XDMElement elt: elts.getElements()) {
					indexManager.dropIndex(docKey.getKey(), pathId, elt.getValue());
					cnt++;
				}
			}
		}
		return cnt;
	}

	private int deindexElements(long docKey, int pathId) {
		int cnt = 0;
		XDMDataKey xdk = factory.newXDMDataKey(docKey, pathId);
		XDMElements elts = xdmCache.get(xdk);
		if (elts != null) {
			for (XDMElement elt: elts.getElements()) {
				indexManager.dropIndex(docKey, pathId, elt.getValue());
				cnt++;
			}
		}
		return cnt;
	}
	
	@SuppressWarnings("unchecked")
	private Set<XDMDocumentKey> getDocumentsOfType(int docType) {
   		Predicate<XDMDocumentKey, XDMDocument> f = Predicates.and(Predicates.equal("typeId", docType), 
   				Predicates.equal("txFinish", 0L));
		return xddCache.keySet(f);
	}
	
	//@Override
	public void deleteDocument(Entry<Long, XDMDocument> entry) throws XDMException {

		Long docId = entry.getKey();
		logger.trace("deleteDocument.enter; docId: {}", docId);
	    boolean removed = false;
	    XDMDocument doc = xddCache.remove(docId);
	    if (doc != null) {
	    	deleteDocumentElements(doc.getFragments(), doc.getTypeId());
			triggerManager.applyTrigger(doc, Action.delete, Scope.before); 
			xmlCache.delete(docId);
			srcCache.remove(docId);
			triggerManager.applyTrigger(doc, Action.delete, Scope.after); 
	        removed = true;
	    } else { 
			throw new XDMException("Document Entry with id " + entry.getKey() + " not found", 
					XDMException.ecDocument);
	    }
		logger.trace("deleteDocument.exit; removed: {}", removed);
	}
	
	private void deleteDocumentElements(long[] fragments, int typeId) {

    	int cnt = 0;
    	//Set<XDMDataKey> localKeys = xdmCache.localKeySet();
    	Collection<XDMPath> allPaths = model.getTypePaths(typeId);
		logger.trace("deleteDocumentElements; got {} possible paths to remove; xdmCache size: {}", 
				allPaths.size(), xdmCache.size());
		int iCnt = 0;
		for (long docId: fragments) {
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
		}
		logger.trace("deleteDocumentElements; deleted keys: {}; indexes: {}; xdmCache size after delete: {}",
				cnt, iCnt, xdmCache.size());
	}

	@Override
	public XDMDocument getDocument(long docId) {
		XDMDocument doc = getDocument(factory.newXDMDocumentKey(docId)); 
		logger.trace("getDocument; returning: {}", doc);
		return doc;
	}

	private XDMDocument getDocument(XDMDocumentKey docKey) {
		return xddCache.get(docKey); 
	}
	
	//@Override
	@SuppressWarnings("unchecked")
	public long getDocumentId(String uri) {
		// the txFinish can be > 0, but not committed yet!
   		Predicate<XDMDocumentKey, XDMDocument> f = Predicates.and(Predicates.equal("uri", uri), 
   				Predicates.equal("txFinish", 0L));
		Set<XDMDocumentKey> docKeys = xddCache.keySet(f);
		if (docKeys.size() == 0) {
			return 0L;
		}

		// should also check if doc's start transaction is committed..
		long docId = 0;
		for (XDMDocumentKey docKey: docKeys) {
			if (docKey.getKey() > docId) {
				docId = docKey.getKey();
			}
		}
		return docId;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<Long> getDocumentIds(String pattern) {
		logger.info("getDocumentIds.enter; got pattern: {}", pattern);
   		Predicate<XDMDocumentKey, XDMDocument> f = Predicates.and(Predicates.regex("uri", pattern), 
   				Predicates.equal("txFinish", 0L));
		Set<XDMDocumentKey> docKeys = xddCache.keySet(f);

		// should also check if doc's start transaction is committed..
		List<Long> result = new ArrayList<>(docKeys.size());
		for (XDMDocumentKey docKey: docKeys) {
			result.add(docKey.getKey());
		}
		logger.info("getDocumentIds.exit; returning: {}", result);
		return result.iterator();
	}
	
	@Override
	public String getDocumentAsString(long docId) throws XDMException {
		XDMDocumentKey docKey = factory.newXDMDocumentKey(docId);
		String xml = xmlCache.get(docKey);
		if (xml == null) {
			XDMDocument doc = getDocument(docKey);
			if (doc == null) {
				logger.info("getDocumentAsString; no document found for ID: {}", docId);
				return null;
			}
			
			// if docId is not local then buildDocument returns null!
			// query docId owner node for the XML instead
			if (hzInstance.getPartitionService().getPartition(docKey).getOwner().localMember()) {
				String root = model.getDocumentRoot(doc.getTypeId());
				Map<String, String> params = new HashMap<String, String>();
				params.put(":doc", root);
				Collection<String> results = buildDocument(Collections.singleton(docId), ":doc", params);
				if (!results.isEmpty()) {
					xml = results.iterator().next();
					xmlCache.set(docKey, xml);
				}
			} else {
				DocumentContentProvider xp = new DocumentContentProvider(null, docId); //??
				IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
				Future<String> future = execService.submitToKeyOwner(xp, docId);
				try {
					xml = future.get();
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("getDocumentAsString; error getting result", ex);
					throw new XDMException(ex, XDMException.ecDocument);
				}
			}
		}
		return xml;
	}

	@Override
	public Source getDocumentAsSource(long docId) {
		return srcCache.get(factory.newXDMDocumentKey(docId));
	}
	
	@Override
	public XDMDocument storeDocumentFromSource(long docId, String uri, Source source) {
		srcCache.put(factory.newXDMDocumentKey(docId), source);
		return xddCache.get(docId);
	}
	
	//@Override
	public XDMDocument storeDocumentFromString(String xml) throws XDMException {

	    return storeDocumentFromString(0, null, xml);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public XDMDocument storeDocumentFromString(long docId, String uri, String xml) throws XDMException {
		// create new document version ??
		// what if we want to pass here correct URI ??
		logger.trace("storeDocumentFromString.enter; docId: {}; uri: {}; xml: {}", docId, uri, xml.length());

		boolean update = false;
		if (docId == 0) {
			if (uri == null) {
				docId = XDMDocumentKey.toKey(docGen.next(), 1);
				uri = "" + docId + ".xml";
			} else {
				Long existingId = getDocumentId(uri);
				if (existingId != null) {
					docId = existingId;
					update = true;
				} else {
					docId = XDMDocumentKey.toKey(docGen.next(), 1);
				}
			}
		} else {
			update = true;
			if (uri == null) {
				uri = "" + docId + ".xml";
			} else {
				Long existingId = getDocumentId(uri);
				// shouldn't we check here if document with docId exists?
				if (existingId != null && existingId != docId) {
					// otherwise we'll get a situation when two different Documents
					// are stored in the same file.
					// what if they point to different versions of the same document!?
					throw new XDMException("Document with URI '" + uri + "' already exists; docId: " + 
							existingId, XDMException.ecDocument);
				}
			}
		}
		
		XDMDocumentKey docKey = factory.newXDMDocumentKey(docId);
		boolean locked = lockDocument(docKey);
		if (locked) {
			try {
				XDMDocumentKey newKey = docKey;
				if (update) {
				    XDMDocument doc = xddCache.get(newKey);
				    if (doc != null) {
				    	if (doc.getTxFinish() > TX_NO && txManager.isTxVisible(doc.getTxFinish())) {
				    		throw new XDMException("Document with ID: " + doc.getDocumentId() + 
				    				", version: " + doc.getVersion() + " has been concurrently updated", 
				    				XDMException.ecDocument);
				    	}
				    	logger.trace("storeDocumentFromString; going to update document: {}", doc);
				    	// we must finish old Document and create a new one!
				    	doc.finishDocument(txManager.getCurrentTxId());
				    	//deleteDocumentElements(docId, doc.getTypeId());
				    	xddCache.put(docKey, doc);
				    	newKey = factory.newXDMDocumentKey(doc.getDocumentId(), doc.getVersion() + 1);
				    	// delete unique index here..
				    	Collection<Integer> pathIds = indexManager.getTypeIndexes(doc.getTypeId(), true);
				    	for (int pathId: pathIds) {
					    	deindexElements(doc.getDocumentKey(), pathId);
				    	}
				    	// shouldn't we lock the newKey too?
				    }
				}
			    return createDocument(new AbstractMap.SimpleEntry(newKey, null), uri, xml);
			} catch (XDMException ex) {
				throw ex;
			} catch (Exception ex) {
				logger.error("storeDocumentFromString.error; docId: " + docId, ex);
				throw new XDMException(ex, XDMException.ecDocument);
			} finally {
				unlockDocument(docKey);
			}
		} else {
    		throw new XDMException("Was not able to aquire lock on Document: " + docKey + 
    				", timed out", XDMException.ecDocument);
		}
	}

	@Override
	public void removeDocument(long docKey) throws XDMException {
		//deleteDocument(new AbstractMap.SimpleEntry(docId, null));
		
		logger.trace("removeDocument.enter; docKey: {}", docKey);
	    XDMDocument doc = getDocument(docKey);
	    boolean removed = false;
	    if (doc != null && (doc.getTxFinish() == TX_NO || !txManager.isTxVisible(doc.getTxFinish()))) {
			//String user = JMXUtils.getCurrentUser();
			triggerManager.applyTrigger(doc, Action.delete, Scope.before); 
	    	doc.finishDocument(txManager.getCurrentTxId()); //, user);
	    	//xddCache.put(docKey, doc);
	    	xddCache.put(factory.newXDMDocumentKey(docKey), doc);
			//xmlCache.delete(docId); ??
			//srcCache.remove(docId); ??
	    	Collection<Integer> pathIds = indexManager.getTypeIndexes(doc.getTypeId(), true);
	    	for (int pathId: pathIds) {
		    	deindexElements(doc.getDocumentKey(), pathId);
	    	}
	    	triggerManager.applyTrigger(doc, Action.delete, Scope.after); 
		    removed = true;
	    }
		logger.trace("removeDocument.exit; removed: {}", removed);
	}

	private boolean lockDocument(XDMDocumentKey docKey) { //throws XDMException {
		
		boolean locked = false;
		long timeout = txManager.getTransactionTimeout();
		if (timeout > 0) {
			try {
				locked = xddCache.tryLock(docKey, timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
				logger.error("lockDocument.error", ex);
				//throw new XDMException(ex);
			}
		} else {
			locked = xddCache.tryLock(docKey);
		}
		return locked;
	}

	private void unlockDocument(XDMDocumentKey docKey) {

		xddCache.unlock(docKey);
	}
	
}
