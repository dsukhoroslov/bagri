package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.xdm.client.common.XDMCacheConstants.SQN_DOCUMENT;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.client.common.impl.DocumentManagementBase;
import com.bagri.xdm.client.hazelcast.data.DocumentKey;
import com.bagri.xdm.client.hazelcast.task.doc.CollectionDocumentsProvider;
import com.bagri.xdm.client.hazelcast.task.doc.CollectionDocumentsRemover;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentBeanCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentBeanProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCollectionUpdater;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentMapCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentMapProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentManagementImpl extends DocumentManagementBase implements XDMDocumentManagement {

	private IMap<XDMDocumentKey, XDMDocument> xddCache;
	private IExecutorService execService;
    private RepositoryImpl repo;
	
	public DocumentManagementImpl() {
		super();
	}

	IMap<XDMDocumentKey, XDMDocument> getDocumentCache() {
		return xddCache;
	}
	
	void initialize(RepositoryImpl repo) {
		this.repo = repo;
		HazelcastInstance hzClient = repo.getHazelcastClient();
		xddCache = hzClient.getMap(CN_XDM_DOCUMENT);
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
	}

	@Override
	public XDMDocument getDocument(XDMDocumentId docId) throws XDMException {
		// do this via task/EP ??
		long docKey = docId.getDocumentKey();
		if (docKey == 0) {
			XDMDocumentKey key = getDocumentKey(docId.getDocumentUri());
			docKey = key.getKey();
		}
		XDMDocument doc = xddCache.get(new DocumentKey(docKey));
		if (doc == null) {
			logger.trace("getDocument; can not get document for key: {}; cache size is: {}", 
					docKey, xddCache.size());
			// throw ex?
			repo.getHealthManagement().checkClusterState();
		}
		return doc;
	}
	
	//@Override
	public XDMDocumentKey getDocumentKey(String uri) {
		// do this via EP ?!
   		Predicate<XDMDocumentKey, XDMDocument> f = Predicates.equal("uri", uri);
		Set<XDMDocumentKey> docKeys = xddCache.keySet(f);
		if (docKeys.size() == 0) {
			return null;
		}
		// TODO: check if too many docs ?? must take latest version!
		return docKeys.iterator().next();
	}
	
	@Override
	public Collection<XDMDocumentId> getDocumentIds(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object getDocumentAsBean(XDMDocumentId docId) throws XDMException {
		logger.trace("getDocumentAsBean.enter; got docId: {}", docId);
		Object result = null;
		DocumentBeanProvider xp = new DocumentBeanProvider(repo.getClientId(), docId);
		Future<Object> future = execService.submit(xp);
		try {
			result = future.get();
			logger.trace("getDocumentAsBean.exit; got bean: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentAsBean; error getting result", ex);
			throw new XDMException(ex, XDMException.ecDocument);
		}
	}

	@Override
	public Map<String, Object> getDocumentAsMap(XDMDocumentId docId) throws XDMException {
		logger.trace("getDocumentAsMap.enter; got docId: {}", docId);
		Map<String, Object> result = null;
		DocumentMapProvider xp = new DocumentMapProvider(repo.getClientId(), docId);
		Future<Map<String, Object>> future = execService.submit(xp);
		try {
			result = future.get();
			logger.trace("getDocumentAsMap.exit; got map: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentAsMap; error getting result", ex);
			throw new XDMException(ex, XDMException.ecDocument);
		}
	}

	@Override
	public String getDocumentAsString(XDMDocumentId docId) throws XDMException {
		// actually, I can try just get it from XML cache!
		logger.trace("getDocumentAsString.enter; got docId: {}", docId);
		String result = null;
		DocumentContentProvider xp = new DocumentContentProvider(repo.getClientId(), docId);
		Future<String> future = execService.submit(xp);
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

	@Override
	public XDMDocument storeDocumentFromBean(XDMDocumentId docId, Object bean, Properties props) throws XDMException {
		if (bean == null) {
			throw new XDMException("Document bean can not be null", XDMException.ecDocument);
		}
		logger.trace("storeDocumentFromMap.enter; bean: {}", bean);
		
		DocumentBeanCreator task = new DocumentBeanCreator(repo.getClientId(), repo.getTransactionId(), docId, props, bean);
		return storeDocument(docId, props, task);
	}

	@Override
	public XDMDocument storeDocumentFromMap(XDMDocumentId docId, Map<String, Object> fields, Properties props) throws XDMException {
		if (fields == null) {
			throw new XDMException("Document fields map can not be null", XDMException.ecDocument);
		}
		logger.trace("storeDocumentFromMap.enter; field size: {}", fields.size());
		
		DocumentMapCreator task = new DocumentMapCreator(repo.getClientId(), repo.getTransactionId(), docId, props, fields);
		return storeDocument(docId, props, task);
	}
	
	@Override
	public XDMDocument storeDocumentFromString(XDMDocumentId docId, String content, Properties props) throws XDMException {
		if (content == null) {
			throw new XDMException("Document content can not be null", XDMException.ecDocument);
		}
		logger.trace("storeDocumentFromString.enter; content length: {}", content.length());
		
		DocumentCreator task = new DocumentCreator(repo.getClientId(), repo.getTransactionId(), docId, props, content);
		return storeDocument(docId, props, task);
	}
	
	public XDMDocument storeDocument(XDMDocumentId docId, Properties props, Callable<XDMDocument> creator) throws XDMException {
		logger.trace("storeDocument.enter; docId: {}; props: {}", docId, props);
		repo.getHealthManagement().checkClusterState();
		Future<XDMDocument> future = execService.submit(creator);
		try {
			XDMDocument result = future.get();
			logger.trace("storeDocument.exit; returning: {}", result);
			return (XDMDocument) result;
		} catch (InterruptedException | ExecutionException ex) {
			// the document could be stored anyway..
			logger.error("storeDocument.error", ex);
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
		Future<XDMDocument> future = execService.submit(task);
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
	public Collection<XDMDocumentId> getCollectionDocumentIds(String collection) {

		logger.trace("getCollectionDocumentIds.enter; collection: {}", collection);
		//repo.getHealthManagement().checkClusterState();
		
		CollectionDocumentsProvider task = new CollectionDocumentsProvider(repo.getClientId(), collection);
		Map<Member, Future<Collection<XDMDocumentId>>> results = execService.submitToAllMembers(task);
		Collection<XDMDocumentId> result = new HashSet<XDMDocumentId>();
		for (Map.Entry<Member, Future<Collection<XDMDocumentId>>> entry: results.entrySet()) {
			try {
				result.addAll(entry.getValue().get());
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getCollectionDocumentIds.error; ", ex);
				// process all results first?!
				//throw new XDMException(ex, XDMException.ecDocument);
			}
		}
		logger.trace("getCollectionDocumentIds.exit; returning: {}", result.size());
		return result;
	}
	
	@Override
	public int removeCollectionDocuments(String collection) throws XDMException {
		
		logger.trace("removeCollectionDocuments.enter; collection: {}", collection);
		repo.getHealthManagement().checkClusterState();
		
		CollectionDocumentsRemover task = new CollectionDocumentsRemover(repo.getClientId(), repo.getTransactionId(), collection);
		Map<Member, Future<Integer>> results = execService.submitToAllMembers(task);
		int cnt = 0;
		for (Map.Entry<Member, Future<Integer>> entry: results.entrySet()) {
			try {
				cnt += entry.getValue().get();
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("removeCollectionDocuments.error; ", ex);
				// process all results first?!
				throw new XDMException(ex, XDMException.ecDocument);
			}
		}
		logger.trace("removeCollectionDocuments.exit; removed: {}", cnt);
		return cnt;
	}
	
	@Override
	public int addDocumentToCollections(XDMDocumentId docId, String[] collections) {
		//
		logger.trace("addDocumentsToCollections.enter; docId: {}, collectIds: {}", docId, Arrays.toString(collections));
		//repo.getHealthManagement().checkClusterState();
		int cnt = updateDocumentCollections(docId, true, collections);
		logger.trace("addDocumentsToCollections.exit; processed: {}", cnt);
		return cnt;
	}

	@Override
	public int removeDocumentFromCollections(XDMDocumentId docId, String[] collections) {
		//
		logger.trace("removeDocumentsFromCollections.enter; docId: {}, collectIds: {}", docId, Arrays.toString(collections));
		//repo.getHealthManagement().checkClusterState();
		int cnt = updateDocumentCollections(docId, false, collections);
		logger.trace("removeDocumentsFromCollections.exit; processed: {}", cnt);
		return cnt;
	}
	
	private int updateDocumentCollections(XDMDocumentId docId, boolean add, String[] collections) {
		
		DocumentCollectionUpdater task = new DocumentCollectionUpdater(repo.getClientId(), docId, add, collections);
		Future<Integer> result = execService.submit(task);
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
