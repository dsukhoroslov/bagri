package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.api.XDMException.ecDocument;
import static com.bagri.xdm.cache.api.CacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.cache.api.CacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.impl.DocumentManagementBase;
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
import com.bagri.xdm.client.hazelcast.task.doc.DocumentUrisProvider;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.domain.Document;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

public class DocumentManagementImpl extends DocumentManagementBase implements DocumentManagement {

	private IMap<DocumentKey, Document> xddCache;
	private IExecutorService execService;
    private SchemaRepositoryImpl repo;
	
	public DocumentManagementImpl() {
		super();
	}

	IMap<DocumentKey, Document> getDocumentCache() {
		return xddCache;
	}
	
	void initialize(SchemaRepositoryImpl repo) {
		this.repo = repo;
		HazelcastInstance hzClient = repo.getHazelcastClient();
		xddCache = hzClient.getMap(CN_XDM_DOCUMENT);
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
	}

	@Override
	public Document getDocument(String uri) throws XDMException {
		logger.trace("getDocument.enter; got uri: {}", uri);
		Document result = null;
		DocumentProvider task = new DocumentProvider(repo.getClientId(), uri);
		Future<Document> future = execService.submit(task);
		try {
			result = future.get();
			logger.trace("getDocument.exit; got document: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocument; error getting result", ex);
			throw new XDMException(ex, ecDocument);
		}
	}
	
	@Override
	public Collection<String> getDocumentUris(String pattern) throws XDMException {
		logger.trace("getDocumentUris.enter; got pattern: {}", pattern);
		Collection<String> result = null;
		DocumentUrisProvider task = new DocumentUrisProvider(repo.getClientId(), repo.getTransactionId(), pattern);
		Future<Collection<String>> future = execService.submit(task);
		try {
			result = future.get();
			logger.trace("getDocumentUris.exit; got results: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentUris; error getting result", ex);
			throw new XDMException(ex, ecDocument);
		}
	}
	
	@Override
	public Object getDocumentAsBean(String uri, Properties props) throws XDMException {
		logger.trace("getDocumentAsBean.enter; got uri: {}", uri);
		Object result = null;
		DocumentBeanProvider task = new DocumentBeanProvider(repo.getClientId(), uri, props);
		Future<Object> future = execService.submit(task);
		try {
			result = future.get();
			logger.trace("getDocumentAsBean.exit; got bean: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentAsBean; error getting result", ex);
			throw new XDMException(ex, ecDocument);
		}
	}

	@Override
	public Map<String, Object> getDocumentAsMap(String uri, Properties props) throws XDMException {
		logger.trace("getDocumentAsMap.enter; got uri: {}", uri);
		Map<String, Object> result = null;
		DocumentMapProvider task = new DocumentMapProvider(repo.getClientId(), uri, props);
		Future<Map<String, Object>> future = execService.submit(task);
		try {
			result = future.get();
			logger.trace("getDocumentAsMap.exit; got map: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentAsMap; error getting result", ex);
			throw new XDMException(ex, ecDocument);
		}
	}

	@Override
	public String getDocumentAsString(String uri, Properties props) throws XDMException {
		// actually, I can try just get it from XML cache!
		logger.trace("getDocumentAsString.enter; got uri: {}", uri);
		String result = null;
		DocumentContentProvider task = new DocumentContentProvider(repo.getClientId(), uri, props);
		Future<String> future = execService.submit(task);
		try {
			result = future.get();
			logger.trace("getDocumentAsString.exit; got content of length: {}", result == null ? 0 : result.length());
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentAsString; error getting result", ex);
			throw new XDMException(ex, ecDocument);
		}
	}

	@Override
	public Document storeDocumentFromBean(String uri, Object bean, Properties props) throws XDMException {
		if (bean == null) {
			throw new XDMException("Document bean can not be null", ecDocument);
		}
		logger.trace("storeDocumentFromMap.enter; uri: {}; bean: {}", uri, bean);
		
		DocumentBeanCreator task = new DocumentBeanCreator(repo.getClientId(), repo.getTransactionId(), uri, props, bean);
		return storeDocument(props, task);
	}

	@Override
	public Document storeDocumentFromMap(String uri, Map<String, Object> fields, Properties props) throws XDMException {
		if (fields == null) {
			throw new XDMException("Document fields map can not be null", ecDocument);
		}
		logger.trace("storeDocumentFromMap.enter; uri: {}; field size: {}", uri, fields.size());
		
		DocumentMapCreator task = new DocumentMapCreator(repo.getClientId(), repo.getTransactionId(), uri, props, fields);
		return storeDocument(props, task);
	}
	
	@Override
	public Document storeDocumentFromString(String uri, String content, Properties props) throws XDMException {
		if (content == null) {
			throw new XDMException("Document content can not be null", ecDocument);
		}
		logger.trace("storeDocumentFromString.enter; uri: {}; content length: {}", uri, content.length());
		
		DocumentCreator task = new DocumentCreator(repo.getClientId(), repo.getTransactionId(), uri, props, content);
		return storeDocument(props, task);
	}
	
	public Document storeDocument(Properties props, Callable<Document> creator) throws XDMException {
		logger.trace("storeDocument.enter; props: {}", props);
		repo.getHealthManagement().checkClusterState();
		Future<Document> future = execService.submit(creator);
		try {
			Document result = future.get();
			logger.trace("storeDocument.exit; returning: {}", result);
			return (Document) result;
		} catch (InterruptedException | ExecutionException ex) {
			// the document could be stored anyway..
			logger.error("storeDocument.error", ex);
			throw new XDMException(ex, ecDocument);
		}
	}

	@Override
	public void removeDocument(String uri) throws XDMException {
		
		logger.trace("removeDocument.enter; uri: {}", uri);
		repo.getHealthManagement().checkClusterState();
		//XDMDocumentRemover proc = new XDMDocumentRemover();
		//Object result = xddCache.executeOnKey(docId, proc);
		
		DocumentRemover task = new DocumentRemover(repo.getClientId(), repo.getTransactionId(), uri);
		Future<Document> future = execService.submit(task);
		try {
			Document result = future.get();
			logger.trace("removeDocument.exit; returning: {}", result);
			//return (XDMDocument) result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("removeDocument.error: ", ex);
			throw new XDMException(ex, ecDocument);
		}
	}

	@Override
	public Collection<String> getCollections() throws XDMException {
		// TODO Auto-generated method stub
		return null;
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
				//throw new XDMException(ex, ecDocument);
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
				throw new XDMException(ex, ecDocument);
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
			//throw new XDMException(ex, ecDocument);
		}
		return cnt;
	}

}
