package com.bagri.client.hazelcast.impl;

import static com.bagri.core.Constants.pn_client_fetchAsynch;
import static com.bagri.core.Constants.pn_client_id;
import static com.bagri.core.Constants.pn_client_txId;
import static com.bagri.core.Constants.pn_client_txLevel;
import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.core.api.BagriException.ecDocument;
//import static com.bagri.core.server.api.CacheConstants.CN_XDM_CONTENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.core.server.api.CacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.Arrays;
import java.util.Collection;
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
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.impl.DocumentManagementBase;
import com.bagri.core.model.Document;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

public class DocumentManagementImpl extends DocumentManagementBase implements DocumentManagement {

	//private IMap<DocumentKey, Object> cntCache;
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
		//cntCache = hzClient.getMap(CN_XDM_CONTENT);
		xddCache = hzClient.getMap(CN_XDM_DOCUMENT);
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
	}
	
	private DocumentKey getDocumentKey(String uri) {
		return new DocumentPartKey(uri.hashCode(), 0, 1);
	}

	private Properties checkDocumentProperties(Properties props) {
		if (props == null) {
			props = new Properties();
		}
		props.setProperty(pn_client_id, repo.getClientId());
		props.setProperty(pn_client_txId, String.valueOf(repo.getTransactionId()));
		props.setProperty(pn_schema_name, repo.getSchemaName());
		//if (defTxLevel != null) {
		//	props.setProperty(pn_client_txLevel, defTxLevel);
		//}
		return props;
	}
	
	@Override
	@SuppressWarnings("resource")
	public Iterable<String> getDocumentUris(String pattern, Properties props) throws BagriException {
		logger.trace("getDocumentUris.enter; got pattern: {}; props: {}", pattern, props);
		CombinedCollectionImpl<String> result = new CombinedCollectionImpl<>();
		checkDocumentProperties(props);
		boolean asynch = Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"));
		DocumentUrisProvider task = new DocumentUrisProvider(repo.getClientId(), repo.getTransactionId(), props, pattern);
		Map<Member, Future<ResultCollection<String>>> results = execService.submitToAllMembers(task);
		for (Map.Entry<Member, Future<ResultCollection<String>>> entry: results.entrySet()) {
			try {
				ResultCollection<String> cln = entry.getValue().get();
				if (asynch) {
					((QueuedCollectionImpl<String>) cln).init(repo.getHazelcastClient());
				}
				result.addResults(cln);
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getDocumentUris; error getting result", ex);
				throw new BagriException(ex, ecDocument);
			}
		}
		logger.trace("getDocumentUris.exit; got results: {}", result);
		return result;
	}
	
	@Override
	public DocumentAccessor getDocument(String uri, Properties props) throws BagriException {
		logger.trace("getDocument.enter; got uri: {}; props: {}", uri, props);
		checkDocumentProperties(props);
		DocumentProvider task = new DocumentProvider(repo.getClientId(), repo.getTransactionId(), props, uri);
		DocumentKey key = getDocumentKey(uri);
		Object result = xddCache.executeOnKey(key, task);
		logger.trace("getDocument.exit; got content: {}", result);
		return (DocumentAccessor) result;
	}

	@Override
	@SuppressWarnings("resource")
	public Iterable<DocumentAccessor> getDocuments(String pattern, Properties props) throws BagriException {
		logger.trace("getDocuments.enter; got pattern: {}; props: {}", pattern, props);
		CombinedCollectionImpl<DocumentAccessor> result = new CombinedCollectionImpl<>();
		checkDocumentProperties(props);
		boolean asynch = Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"));
		DocumentsProvider task = new DocumentsProvider(repo.getClientId(), repo.getTransactionId(), props, pattern);
		Map<Member, Future<ResultCollection<DocumentAccessor>>> results = execService.submitToAllMembers(task);
		for (Map.Entry<Member, Future<ResultCollection<DocumentAccessor>>> entry: results.entrySet()) {
			try {
				ResultCollection<DocumentAccessor> cln = entry.getValue().get();
				if (asynch) {
					((QueuedCollectionImpl<DocumentAccessor>) cln).init(repo.getHazelcastClient());
				}
				result.addResults(cln);
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getDocuments; error getting result", ex);
				throw new BagriException(ex, ecDocument);
			}
		}
		logger.trace("getDocuments.exit; got results: {}", result);
		return (Iterable<DocumentAccessor>) result;
	}
	
	@Override
	public <T> DocumentAccessor storeDocument(String uri, T content, Properties props) throws BagriException {
		logger.trace("storeDocument.enter; uri: {}; content: {}; props: {}", uri, content, props);
		if (content == null) {
			throw new BagriException("Document content can not be null", ecDocument);
		}
		repo.getHealthManagement().checkClusterState();
		checkDocumentProperties(props);
		
		DocumentCreator task = new DocumentCreator(repo.getClientId(), repo.getTransactionId(), props, uri, content);
		task.setRepository(repo);
		Future<DocumentAccessor> future = execService.submit(task);
		try {
			DocumentAccessor result = future.get();
			logger.trace("storeDocument.exit; returning: {}", result);
			return (DocumentAccessor) result;
		} catch (InterruptedException | ExecutionException ex) {
			// the document could be stored anyway..
			logger.error("storeDocument.error", ex);
			throw new BagriException(ex, ecDocument);
		}
	}

	@Override
	public <T> Iterable<DocumentAccessor> storeDocuments(Map<String, T> documents, Properties props) throws BagriException {
		logger.trace("storeDocuments.enter; documents: {}; props: {}", documents, props);
		if (documents == null || documents.isEmpty()) {
			throw new BagriException("Empty Document collection provided", ecDocument);
		}
		repo.getHealthManagement().checkClusterState();
		checkDocumentProperties(props);

		CombinedCollectionImpl<DocumentAccessor> result = new CombinedCollectionImpl<>();
		boolean asynch = Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"));
		DocumentsCreator task = new DocumentsCreator(repo.getClientId(), repo.getTransactionId(), props, (Map<String, Object>) documents);
		// TODO: split documents between members properly..
		Map<Member, Future<ResultCollection<DocumentAccessor>>> results = execService.submitToAllMembers(task);
		for (Map.Entry<Member, Future<ResultCollection<DocumentAccessor>>> entry: results.entrySet()) {
			try {
				ResultCollection<DocumentAccessor> cln = entry.getValue().get();
				if (asynch) {
					((QueuedCollectionImpl<DocumentAccessor>) cln).init(repo.getHazelcastClient());
				}
				result.addResults(cln);
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("storeDocuments; error getting result", ex);
				throw new BagriException(ex, ecDocument);
			}
		}
		logger.trace("storeDocuments.exit; results: {}", result);
		return (Iterable<DocumentAccessor>) result;
	}
	
	@Override
	public DocumentAccessor removeDocument(String uri, Properties props) throws BagriException {
		logger.trace("removeDocument.enter; uri: {}", uri);
		repo.getHealthManagement().checkClusterState();
		//XDMDocumentRemover proc = new XDMDocumentRemover();
		//Object result = xddCache.executeOnKey(docId, proc);
		checkDocumentProperties(props);
		
		DocumentRemover task = new DocumentRemover(repo.getClientId(), repo.getTransactionId(), props, uri);
		Future<DocumentAccessor> future = execService.submit(task);
		try {
			DocumentAccessor result = future.get();
			logger.trace("removeDocument.exit; returning: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("removeDocument.error: ", ex);
			throw new BagriException(ex, ecDocument);
		}
	}

	@Override
	public Iterable<DocumentAccessor> removeDocuments(String pattern, Properties props) throws BagriException {
		logger.trace("removeDocuments.enter; pattern: {}", pattern);
		repo.getHealthManagement().checkClusterState();
		checkDocumentProperties(props);

		CombinedCollectionImpl<DocumentAccessor> result = new CombinedCollectionImpl<>();
		boolean asynch = Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"));
		DocumentsRemover task = new DocumentsRemover(repo.getClientId(), repo.getTransactionId(), props, pattern);
		Map<Member, Future<ResultCollection<DocumentAccessor>>> results = execService.submitToAllMembers(task);
		for (Map.Entry<Member, Future<ResultCollection<DocumentAccessor>>> entry: results.entrySet()) {
			try {
				ResultCollection<DocumentAccessor> cln = entry.getValue().get();
				if (asynch) {
					((QueuedCollectionImpl<DocumentAccessor>) cln).init(repo.getHazelcastClient());
				}
				result.addResults(cln);
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("removeDocuments.error; ", ex);
				// process all results first?!
				throw new BagriException(ex, ecDocument);
			}
		}
		logger.trace("removeDocuments.exit; results: {}", result);
		return (Iterable<DocumentAccessor>) result;
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
		Properties props = checkDocumentProperties(null);
		DocumentCollectionUpdater task = new DocumentCollectionUpdater(repo.getClientId(), props, uri, add, collections);
		DocumentKey key = getDocumentKey(uri);
		return (Integer) xddCache.executeOnKey(key, task);
	}

}
