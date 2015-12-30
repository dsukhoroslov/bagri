package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_ELEMENT;
import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.xdm.client.common.XDMCacheConstants.SQN_DOCUMENT;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.client.common.impl.DocumentManagementBase;
import com.bagri.xdm.client.hazelcast.data.DocumentKey;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCollectionUpdater;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
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
	public XDMDocument getDocument(XDMDocumentId docId) throws XDMException {
		// do this via task/EP ??
		XDMDocument doc = xddCache.get(new DocumentKey(docId.getDocumentKey()));
		if (doc == null) {
			// get it via URI?
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
	public Collection<XDMDocumentId> getDocumentIds(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getDocumentAsString(XDMDocumentId docId) throws XDMException {
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

		return storeDocumentFromString(null, xml, null);
	}

	@Override
	public XDMDocument storeDocumentFromString(XDMDocumentId docId, String content, Properties props) throws XDMException {
		
		if (content == null) {
			throw new XDMException("Document content can not be null", XDMException.ecDocument);
		}
		logger.trace("storeDocumentFromString.enter; docId: {}; content: {}", docId, content.length());
		repo.getHealthManagement().checkClusterState();

		if (docId == null) {
			docId = new XDMDocumentId(docGen.next(), 1);
		}
		
		DocumentCreator task = new DocumentCreator(repo.getClientId(), repo.getTransactionId(), docId, content, props);
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
	public void removeDocument(XDMDocumentId docId) throws XDMException {
		
		logger.trace("removeDocument.enter; docId: {}", docId);
		repo.getHealthManagement().checkClusterState();
		//XDMDocumentRemover proc = new XDMDocumentRemover();
		//Object result = xddCache.executeOnKey(docId, proc);
		
		DocumentRemover task = new DocumentRemover(repo.getClientId(), repo.getTransactionId(), docId);
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
	public Collection<XDMDocumentId> getCollectionDocumentIds(int collectId) {
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
	public int addDocumentToCollections(XDMDocumentId docId, int[] collectIds) {
		//
		logger.trace("addDocumentsToCollections.enter; docId: {}, collectIds: {}", docId, Arrays.toString(collectIds));
		//repo.getHealthManagement().checkClusterState();
		int cnt = updateDocumentCollections(docId, true, collectIds);
		logger.trace("addDocumentsToCollections.exit; processed: {}", cnt);
		return cnt;
	}

	@Override
	public int removeDocumentFromCollections(XDMDocumentId docId, int[] collectIds) {
		//
		logger.trace("removeDocumentsFromCollections.enter; docId: {}, collectIds: {}", docId, Arrays.toString(collectIds));
		//repo.getHealthManagement().checkClusterState();
		int cnt = updateDocumentCollections(docId, false, collectIds);
		logger.trace("removeDocumentsFromCollections.exit; processed: {}", cnt);
		return cnt;
	}
	
	private int updateDocumentCollections(XDMDocumentId docId, boolean add, int[] collectIds) {
		
		DocumentCollectionUpdater task = new DocumentCollectionUpdater(repo.getClientId(), docId, add, collectIds);
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
