package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.client.common.impl.DocumentManagementBase;
import com.bagri.xdm.client.hazelcast.task.doc.CollectionDocumentsProvider;
import com.bagri.xdm.client.hazelcast.task.doc.CollectionDocumentsRemover;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentBeanCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentBeanProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCollectionUpdater;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentMapCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentMapProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover;
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
	public XDMDocument getDocument(String uri) throws XDMException {
		logger.trace("getDocument.enter; got uri: {}", uri);
		XDMDocument result = null;
		DocumentProvider xp = new DocumentProvider(repo.getClientId(), uri);
		Future<XDMDocument> future = execService.submitToKeyOwner(xp, uri);
		try {
			result = future.get();
			logger.trace("getDocument.exit; got document: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocument; error getting result", ex);
			throw new XDMException(ex, XDMException.ecDocument);
		}
	}
	
	//@Override
	//public XDMDocumentKey getDocumentKey(String uri) {
		// do this via EP ?!
   	//	Predicate<XDMDocumentKey, XDMDocument> f = Predicates.equal("uri", uri);
	//	Set<XDMDocumentKey> docKeys = xddCache.keySet(f);
	//	if (docKeys.size() == 0) {
	//		return null;
	//	}
		// TODO: check if too many docs ?? must take latest version!
	//	return docKeys.iterator().next();
	//}
	
	@Override
	public Collection<String> getDocumentUris(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object getDocumentAsBean(String uri) throws XDMException {
		logger.trace("getDocumentAsBean.enter; got uri: {}", uri);
		Object result = null;
		DocumentBeanProvider xp = new DocumentBeanProvider(repo.getClientId(), uri);
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
	public Map<String, Object> getDocumentAsMap(String uri) throws XDMException {
		logger.trace("getDocumentAsMap.enter; got uri: {}", uri);
		Map<String, Object> result = null;
		DocumentMapProvider xp = new DocumentMapProvider(repo.getClientId(), uri);
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
	public String getDocumentAsString(String uri) throws XDMException {
		// actually, I can try just get it from XML cache!
		logger.trace("getDocumentAsString.enter; got uri: {}", uri);
		String result = null;
		DocumentContentProvider xp = new DocumentContentProvider(repo.getClientId(), uri);
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
	public XDMDocument storeDocumentFromBean(String uri, Object bean, Properties props) throws XDMException {
		if (bean == null) {
			throw new XDMException("Document bean can not be null", XDMException.ecDocument);
		}
		logger.trace("storeDocumentFromMap.enter; uri: {}; bean: {}", uri, bean);
		
		DocumentBeanCreator task = new DocumentBeanCreator(repo.getClientId(), repo.getTransactionId(), uri, props, bean);
		return storeDocument(props, task);
	}

	@Override
	public XDMDocument storeDocumentFromMap(String uri, Map<String, Object> fields, Properties props) throws XDMException {
		if (fields == null) {
			throw new XDMException("Document fields map can not be null", XDMException.ecDocument);
		}
		logger.trace("storeDocumentFromMap.enter; uri: {}; field size: {}", uri, fields.size());
		
		DocumentMapCreator task = new DocumentMapCreator(repo.getClientId(), repo.getTransactionId(), uri, props, fields);
		return storeDocument(props, task);
	}
	
	@Override
	public XDMDocument storeDocumentFromString(String uri, String content, Properties props) throws XDMException {
		if (content == null) {
			throw new XDMException("Document content can not be null", XDMException.ecDocument);
		}
		logger.trace("storeDocumentFromString.enter; uri: {}; content length: {}", uri, content.length());
		
		DocumentCreator task = new DocumentCreator(repo.getClientId(), repo.getTransactionId(), uri, props, content);
		return storeDocument(props, task);
	}
	
	public XDMDocument storeDocument(Properties props, Callable<XDMDocument> creator) throws XDMException {
		logger.trace("storeDocument.enter; props: {}", props);
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
	public void removeDocument(String uri) throws XDMException {
		
		logger.trace("removeDocument.enter; uri: {}", uri);
		repo.getHealthManagement().checkClusterState();
		//XDMDocumentRemover proc = new XDMDocumentRemover();
		//Object result = xddCache.executeOnKey(docId, proc);
		
		DocumentRemover task = new DocumentRemover(repo.getClientId(), repo.getTransactionId(), uri);
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
	public Collection<String> getCollectionDocumentUris(String collection) {

		logger.trace("getCollectionDocumentIds.enter; collection: {}", collection);
		//repo.getHealthManagement().checkClusterState();
		
		CollectionDocumentsProvider task = new CollectionDocumentsProvider(repo.getClientId(), collection);
		Map<Member, Future<Collection<String>>> results = execService.submitToAllMembers(task);
		Collection<String> result = new HashSet<String>();
		for (Map.Entry<Member, Future<Collection<String>>> entry: results.entrySet()) {
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
	public int addDocumentToCollections(String uri, String[] collections) {
		//
		logger.trace("addDocumentsToCollections.enter; uri: {}, collectIds: {}", uri, Arrays.toString(collections));
		//repo.getHealthManagement().checkClusterState();
		int cnt = updateDocumentCollections(uri, true, collections);
		logger.trace("addDocumentsToCollections.exit; processed: {}", cnt);
		return cnt;
	}

	@Override
	public int removeDocumentFromCollections(String uri, String[] collections) {
		//
		logger.trace("removeDocumentsFromCollections.enter; uri: {}, collectIds: {}", uri, Arrays.toString(collections));
		//repo.getHealthManagement().checkClusterState();
		int cnt = updateDocumentCollections(uri, false, collections);
		logger.trace("removeDocumentsFromCollections.exit; processed: {}", cnt);
		return cnt;
	}
	
	private int updateDocumentCollections(String uri, boolean add, String[] collections) {
		
		DocumentCollectionUpdater task = new DocumentCollectionUpdater(repo.getClientId(), uri, add, collections);
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
