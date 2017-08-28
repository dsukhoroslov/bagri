package com.bagri.client.hazelcast.impl;

import static com.bagri.core.Constants.pn_client_fetchAsynch;
import static com.bagri.core.Constants.pn_client_id;
import static com.bagri.core.api.BagriException.ecDocument;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_CONTENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.core.server.api.CacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.bagri.client.hazelcast.DocumentPartKey;
import com.bagri.client.hazelcast.task.doc.*;
import com.bagri.core.DocumentKey;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.ResultCollection;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.impl.DocumentManagementBase;
import com.bagri.core.model.Document;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

public class DocumentManagementImpl extends DocumentManagementBase implements DocumentManagement {

	private IMap<DocumentKey, Object> cntCache;
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
		cntCache = hzClient.getMap(CN_XDM_CONTENT);
		xddCache = hzClient.getMap(CN_XDM_DOCUMENT);
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
	}
	
	private DocumentKey getDocumentKey(String uri) {
		return new DocumentPartKey(uri.hashCode(), 0, 1);
	}

	@Override
	public Collection<String> getDocumentUris(String pattern, Properties props) throws BagriException {
		logger.trace("getDocumentUris.enter; got pattern: {}; props: {}", pattern, props);
		DocumentUrisProvider task = new DocumentUrisProvider(repo.getClientId(), repo.getTransactionId(), pattern, props);
		Map<Member, Future<Collection<String>>> results = execService.submitToAllMembers(task);
		Collection<String> result = new HashSet<String>();
		for (Map.Entry<Member, Future<Collection<String>>> entry: results.entrySet()) {
			try {
				result.addAll(entry.getValue().get());
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getDocumentUris; error getting result", ex);
				throw new BagriException(ex, ecDocument);
			}
		}
		return result;
		//Future<Collection<String>> future = execService.submit(task);
		//try {
		//	result = future.get();
		//	logger.trace("getDocumentUris.exit; got results: {}", result);
		//	return result;
		//} catch (InterruptedException | ExecutionException ex) {
		//	logger.error("getDocumentUris; error getting result", ex);
		//	throw new BagriException(ex, ecDocument);
		//}
	}
	
	@Override
	@SuppressWarnings("resource")
	public Iterable<?> getDocuments(String pattern, Properties props) throws BagriException {
		logger.trace("getDocuments.enter; got pattern: {}; props: {}", pattern, props);
		CombinedCollectionImpl result = new CombinedCollectionImpl();
		props.setProperty(pn_client_id, repo.getClientId());
		boolean asynch = Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"));
		DocumentsProvider task = new DocumentsProvider(repo.getClientId(), repo.getTransactionId(), pattern, props);
		Map<Member, Future<ResultCollection>> results = execService.submitToAllMembers(task);
		for (Map.Entry<Member, Future<ResultCollection>> entry: results.entrySet()) {
			try {
				ResultCollection cln = entry.getValue().get();
				if (asynch) {
					((QueuedCollectionImpl) cln).init(repo.getHazelcastClient());
				}
				result.addResults(cln);
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getDocuments; error getting result", ex);
				throw new BagriException(ex, ecDocument);
			}
		}
		logger.trace("getDocuments.exit; got results: {}", result);
		return result;
	}
	
	@Override
	public Document getDocument(String uri) throws BagriException {
		logger.trace("getDocument.enter; got uri: {}", uri);
		DocumentProvider task = new DocumentProvider(repo.getClientId(), repo.getTransactionId(), uri, null);
		DocumentKey key = getDocumentKey(uri);
		Document result = (Document) xddCache.executeOnKey(key, task);
		logger.trace("getDocument.exit; got document: {}", result);
		return result;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getDocumentAs(String uri, Properties props) throws BagriException {
		// actually, I can try just get it from Content cache!
		logger.trace("getDocumentAs.enter; got uri: {}; props: {}", uri, props);
		DocumentContentProvider task = new DocumentContentProvider(repo.getClientId(), repo.getTransactionId(), uri, props);
		DocumentKey key = getDocumentKey(uri);
		Object result = xddCache.executeOnKey(key, task);
		//Object result = cntCache.get(key);
		logger.trace("getDocumentAs.exit; got content: {}", result);
		return (T) result;
	}

	@Override
	public <T> Document storeDocumentFrom(String uri, T content, Properties props) throws BagriException {
		logger.trace("storeDocumentFrom.enter; uri: {}; content: {}; props: {}", uri, content, props);
		if (content == null) {
			throw new BagriException("Document content can not be null", ecDocument);
		}
		repo.getHealthManagement().checkClusterState();
		
		DocumentCreator task = new DocumentCreator(repo.getClientId(), repo.getTransactionId(), uri, props, content);
		Future<Document> future = execService.submit(task);
		try {
			Document result = future.get();
			logger.trace("storeDocumentFrom.exit; returning: {}", result);
			return (Document) result;
		} catch (InterruptedException | ExecutionException ex) {
			// the document could be stored anyway..
			logger.error("storeDocumentFrom.error", ex);
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
