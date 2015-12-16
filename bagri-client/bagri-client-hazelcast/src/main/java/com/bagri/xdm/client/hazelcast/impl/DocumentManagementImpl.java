package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.client.common.impl.DocumentManagementBase;
import com.bagri.xdm.client.hazelcast.data.DocumentKey;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCollectionUpdater;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentManagementImpl extends DocumentManagementBase implements XDMDocumentManagement {

	private IMap<Long, XDMDocument> xddCache;
	private IMap<XDMDataKey, XDMElements> xdmCache;
	private IdGenerator<Long> docGen;
	private IExecutorService execService;
    private RepositoryImpl repo;
	
	public DocumentManagementImpl() {
		super();
	}

	//private void loadCache(IMap cache) {
	//	long stamp = System.currentTimeMillis();
	//	Set keys = cache.keySet();
	//	for (Object key: keys) {
	//		cache.get(key);
	//	}
	//	logger.debug("loadCache; cache: {}, time taken: {}", cache, System.currentTimeMillis() - stamp);
	//}
	
    public int getXddSize() {
    	return xddCache.size();
    }
    
    public int getXdmSize() {
    	return xdmCache.size();
    }
    
	IMap<XDMDataKey, XDMElements> getDataCache() {
		return xdmCache;
	}
	
	IMap<Long, XDMDocument> getDocumentCache() {
		return xddCache;
	}
	
	void initialize(RepositoryImpl repo) {
		this.repo = repo;
		HazelcastInstance hzClient = repo.getHazelcastClient();
		xddCache = hzClient.getMap(CN_XDM_DOCUMENT);
		xdmCache = hzClient.getMap(CN_XDM_ELEMENT);
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		docGen = new IdGeneratorImpl(hzClient.getAtomicLong(SQN_DOCUMENT));
	}

	@Override
	public XDMDocument getDocument(long docId) throws XDMException {
		// do this via task/EP ??
		XDMDocument doc = xddCache.get(new DocumentKey(docId));
		if (doc == null) {
			logger.trace("getDocument; can not get document for key: {}; cache size is: {}", 
					docId, xddCache.size());
			// throw ex?
			repo.getHealthManagement().checkClusterState();
		}
		return doc;
	}
	
	//@Override
	public Long getDocumentId(String uri) {
		// do this via EP ?!
   		Predicate<Long, XDMDocument> f = Predicates.equal("uri", uri);
		Set<Long> docKeys = xddCache.keySet(f);
		if (docKeys.size() == 0) {
			return null;
		}
		// TODO: check if too many docs ??
		return docKeys.iterator().next();
	}
	
	@Override
	public Collection<Long> getDocumentIds(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getDocumentAsString(long docId) throws XDMException {
		// actually, I can try just get it from XML cache!
		
		logger.trace("getDocumentAsString.enter; got docId: {}", docId);
		
		String result = null;
		DocumentContentProvider xp = new DocumentContentProvider(repo.getClientId(), docId);
		Future<String> future = execService.submitToKeyOwner(xp, docId);
		try {
			result = future.get();
			logger.trace("getDocumentAsString.exit; got template results: {}", 
					result == null ? 0 : result.length());
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentAsString; error getting result", ex);
			throw new XDMException(ex, XDMException.ecDocument);
		}
	}

	public XDMDocument storeDocument(String xml) throws XDMException {

		return storeDocumentFromString(0, null, xml);
	}

	@Override
	public XDMDocument storeDocumentFromString(long docId, String uri, String xml) throws XDMException {
		
		if (xml == null) {
			throw new XDMException("Document content can not be null", XDMException.ecDocument);
		}
		logger.trace("storeDocumentFromString.enter; docId: {}, uri: {}; xml: {}", docId, uri, xml.length());
		repo.getHealthManagement().checkClusterState();

		if (docId == 0 && uri == null) {
			docId = XDMDocumentKey.toKey(docGen.next(), 1);
		}
		
		DocumentCreator task = new DocumentCreator(repo.getClientId(), docId, repo.getTransactionId(), uri, xml);
		Future<XDMDocument> future = execService.submitToKeyOwner(task, docId);
		try {
			XDMDocument result = future.get();
			logger.trace("storeDocumentFromString.exit; returning: {}", result);
			return (XDMDocument) result;
		} catch (InterruptedException | ExecutionException ex) {
			// the document could be stored anyway..
			logger.error("storeDocumentFromString.error", ex);
			throw new XDMException(ex, XDMException.ecDocument);
		}
	}
	
	@Override
	public void removeDocument(long docId) throws XDMException {
		
		logger.trace("removeDocument.enter; docId: {}", docId);
		repo.getHealthManagement().checkClusterState();
		//XDMDocumentRemover proc = new XDMDocumentRemover();
		//Object result = xddCache.executeOnKey(docId, proc);
		
		DocumentRemover task = new DocumentRemover(repo.getClientId(), docId, repo.getTransactionId());
		Future<XDMDocument> future = execService.submitToKeyOwner(task, docId);
		try {
			XDMDocument result = future.get();
			logger.trace("removeDocument.exit; returning: {}", result);
			//return (XDMDocument) result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("removeDocument.error: ", ex);
			throw new XDMException(ex, XDMException.ecDocument);
		}
	}

	@Override
	public Collection<Long> getCollectionDocumentIds(int collectId) {
		//
		return null;
	}
	
	@Override
	public void removeCollectionDocuments(int collectId) throws XDMException {
		//
		// how properly locate docs?
		// should it be synch or asynch? 
	}
	
	@Override
	public int addDocumentToCollections(long docId, int[] collectIds) {
		//
		logger.trace("addDocumentsToCollections.enter; docId: {}, collectIds: {}", docId, Arrays.toString(collectIds));
		//repo.getHealthManagement().checkClusterState();
		int cnt = updateDocumentCollections(true, docId, collectIds);
		logger.trace("addDocumentsToCollections.exit; processed: {}", cnt);
		return cnt;
	}

	@Override
	public int removeDocumentFromCollections(long docId, int[] collectIds) {
		//
		logger.trace("removeDocumentsFromCollections.enter; docId: {}, collectIds: {}", docId, Arrays.toString(collectIds));
		//repo.getHealthManagement().checkClusterState();
		int cnt = updateDocumentCollections(false, docId, collectIds);
		logger.trace("removeDocumentsFromCollections.exit; processed: {}", cnt);
		return cnt;
	}
	
	private int updateDocumentCollections(boolean add, long docId, int[] collectIds) {
		
		DocumentCollectionUpdater task = new DocumentCollectionUpdater(repo.getClientId(), add, docId, collectIds);
		Future<Integer> result = execService.submitToKeyOwner(task, docId);
		int cnt = 0;
		try {
			cnt = result.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("updateDocumentsCollections.error: ", ex);
			//throw new XDMException(ex, XDMException.ecDocument);
		}
		return cnt;
	}
	
}
