package com.bagri.client.hazelcast.impl;

import static com.bagri.core.api.BagriException.ecDocument;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.core.server.api.CacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.bagri.client.hazelcast.DocumentPartKey;
import com.bagri.client.hazelcast.task.doc.*;
import com.bagri.core.DocumentKey;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.impl.DocumentManagementBase;
import com.bagri.core.model.Document;
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
	
	private DocumentKey getDocumentKey(String uri) {
		return new DocumentPartKey(uri.hashCode(), 0, 1);
	}

	@Override
	public Collection<String> getDocumentUris(String pattern, Properties props) throws BagriException {
		logger.trace("getDocumentUris.enter; got pattern: {}", pattern);
		Collection<String> result = null;
		DocumentUrisProvider task = new DocumentUrisProvider(repo.getClientId(), repo.getTransactionId(), pattern, props);
		Future<Collection<String>> future = execService.submit(task);
		try {
			result = future.get();
			logger.trace("getDocumentUris.exit; got results: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentUris; error getting result", ex);
			throw new BagriException(ex, ecDocument);
		}
	}
	
	@Override
	public Document getDocument(String uri) throws BagriException {
		logger.trace("getDocument.enter; got uri: {}", uri);
		DocumentProvider task = new DocumentProvider(repo.getClientId(), uri);
		DocumentKey key = getDocumentKey(uri);
		Document result = (Document) xddCache.executeOnKey(key, task);
		logger.trace("getDocument.exit; got document: {}", result);
		return result;
	}
	
	@Override
	public Object getDocumentAsBean(String uri, Properties props) throws BagriException {
		logger.trace("getDocumentAsBean.enter; got uri: {}", uri);
		DocumentBeanProvider task = new DocumentBeanProvider(repo.getClientId(), uri, props);
		DocumentKey key = getDocumentKey(uri);
		Object result = xddCache.executeOnKey(key, task);
		logger.trace("getDocumentAsBean.exit; got bean: {}", result);
		return result;
	}

	@Override
	public Map<String, Object> getDocumentAsMap(String uri, Properties props) throws BagriException {
		logger.trace("getDocumentAsMap.enter; got uri: {}", uri);
		DocumentMapProvider task = new DocumentMapProvider(repo.getClientId(), uri, props);
		DocumentKey key = getDocumentKey(uri);
		Map<String, Object> result = (Map<String, Object>) xddCache.executeOnKey(key, task);
		logger.trace("getDocumentAsMap.exit; got map: {}", result);
		return result;
	}

	@Override
	public String getDocumentAsString(String uri, Properties props) throws BagriException {
		// actually, I can try just get it from XML cache!
		logger.trace("getDocumentAsString.enter; got uri: {}", uri);
		DocumentContentProvider task = new DocumentContentProvider(repo.getClientId(), uri, props);
		DocumentKey key = getDocumentKey(uri);
		String result = (String) xddCache.executeOnKey(key, task);
		logger.trace("getDocumentAsString.exit; got content of length: {}", result == null ? 0 : result.length());
		return result;
	}

	@Override
	public Document storeDocumentFromBean(String uri, Object bean, Properties props) throws BagriException {
		if (bean == null) {
			throw new BagriException("Document bean can not be null", ecDocument);
		}
		logger.trace("storeDocumentFromMap.enter; uri: {}; bean: {}", uri, bean);
		
		DocumentBeanCreator task = new DocumentBeanCreator(repo.getClientId(), repo.getTransactionId(), uri, props, bean);
		return storeDocument(props, task);
	}

	@Override
	public Document storeDocumentFromMap(String uri, Map<String, Object> fields, Properties props) throws BagriException {
		if (fields == null) {
			throw new BagriException("Document fields map can not be null", ecDocument);
		}
		logger.trace("storeDocumentFromMap.enter; uri: {}; field size: {}", uri, fields.size());
		
		DocumentMapCreator task = new DocumentMapCreator(repo.getClientId(), repo.getTransactionId(), uri, props, fields);
		return storeDocument(props, task);
	}
	
	@Override
	public Document storeDocumentFromString(String uri, String content, Properties props) throws BagriException {
		if (content == null) {
			throw new BagriException("Document content can not be null", ecDocument);
		}
		logger.trace("storeDocumentFromString.enter; uri: {}; content length: {}", uri, content.length());
		
		DocumentCreator task = new DocumentCreator(repo.getClientId(), repo.getTransactionId(), uri, props, content);
		return storeDocument(props, task);
	}
	
	public Document storeDocument(Properties props, Callable<Document> creator) throws BagriException {
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
			throw new BagriException(ex, ecDocument);
		}
	}

	@Override
	public void removeDocument(String uri) throws BagriException {
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
			throw new BagriException(ex, ecDocument);
		}
	}

	@Override
	public Collection<String> getCollections() throws BagriException {
		logger.trace("getCollections.enter; ");
		Collection<String> result = null;
		CollectionsProvider task = new CollectionsProvider(repo.getClientId());
		Future<Collection<String>> future = execService.submit(task);
		try {
			result = future.get();
			logger.trace("getCollections.exit; returning: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getCollections; error getting result", ex);
			throw new BagriException(ex, ecDocument);
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
				//throw new XDMException(ex, ecDocument);
			}
		}
		logger.trace("getCollectionDocumentIds.exit; returning: {}", result.size());
		return result;
	}
	
	@Override
	public int removeCollectionDocuments(String collection) throws BagriException {
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
				throw new BagriException(ex, ecDocument);
			}
		}
		logger.trace("removeCollectionDocuments.exit; removed: {}", cnt);
		return cnt;
	}
	
	@Override
	public int addDocumentToCollections(String uri, String[] collections) {
		logger.trace("addDocumentsToCollections.enter; uri: {}, collectIds: {}", uri, Arrays.toString(collections));
		//repo.getHealthManagement().checkClusterState();
		int cnt = updateDocumentCollections(uri, true, collections);
		logger.trace("addDocumentsToCollections.exit; processed: {}", cnt);
		return cnt;
	}

	@Override
	public int removeDocumentFromCollections(String uri, String[] collections) {
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
