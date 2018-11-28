package com.bagri.client.hazelcast.impl;

import static com.bagri.core.Constants.pn_client_fetchAsynch;
import static com.bagri.core.Constants.pn_client_fetchSize;
import static com.bagri.core.Constants.pn_client_id;
import static com.bagri.core.Constants.pn_client_txId;
import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.core.api.BagriException.ecDocument;
//import static com.bagri.core.server.api.CacheConstants.CN_XDM_CONTENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.core.server.api.CacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.DocumentPartKey;
import com.bagri.client.hazelcast.task.doc.*;
import com.bagri.core.DocumentKey;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.model.Document;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

public class DocumentManagementImpl implements DocumentManagement {

    private final static transient Logger logger = LoggerFactory.getLogger(DocumentManagementImpl.class);
	
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
		int hash = repo.getDistributionStrategy().getDistributionHash(uri);
		return new DocumentPartKey(hash, 0, 1);
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
	public DocumentAccessor getDocument(String uri, Properties props) throws BagriException {
		logger.trace("getDocument.enter; got uri: {}; props: {}", uri, props);
		props = checkDocumentProperties(props);
		DocumentProvider task = new DocumentProvider(repo.getClientId(), repo.getTransactionId(), props, uri, repo.getDistributionStrategy());
		DocumentKey key = getDocumentKey(uri);
		Object result = xddCache.executeOnKey(key, task);
		logger.trace("getDocument.exit; got content: {}", result);
		return (DocumentAccessor) result;
	}
	
	@Override
	@SuppressWarnings("resource")
	public ResultCursor<DocumentAccessor> getDocuments(String pattern, Properties props) throws BagriException {
		logger.trace("getDocuments.enter; got pattern: {}; props: {}", pattern, props);
		props = checkDocumentProperties(props);
		DocumentsProvider task = new DocumentsProvider(repo.getClientId(), repo.getTransactionId(), props, pattern);
		ResultCursor<DocumentAccessor> result = runIterableDocumentTask(task, props);
		logger.trace("getDocuments.exit; got results: {}", result);
		return result;
	}
	
	@Override
	public <T> DocumentAccessor storeDocument(String uri, T content, Properties props) throws BagriException {
		logger.trace("storeDocument.enter; uri: {}; content: {}; props: {}", uri, content, props);
		if (content == null) {
			throw new BagriException("Document content can not be null", ecDocument);
		}
		repo.getHealthManagement().checkClusterState();
		props = checkDocumentProperties(props);
		DocumentCreator task = new DocumentCreator(repo.getClientId(), repo.getTransactionId(), props, uri, repo.getDistributionStrategy(), content);
		task.setRepository(repo);
		DocumentAccessor result = runSimpleDocumentTask(task, props);
		logger.trace("storeDocument.exit; returning: {}", result);
		return result;
	}

	@Override
	public <T> ResultCursor<DocumentAccessor> storeDocuments(Map<String, T> documents, Properties props) throws BagriException {
		logger.trace("storeDocuments.enter; documents: {}; props: {}", documents, props);
		if (documents == null || documents.isEmpty()) {
			throw new BagriException("Empty Document collection provided", ecDocument);
		}
		repo.getHealthManagement().checkClusterState();
		props = checkDocumentProperties(props);
		DocumentsCreator task = new DocumentsCreator(repo.getClientId(), repo.getTransactionId(), props, (Map<String, Object>) documents);
		// TODO: split documents between members properly..
		ResultCursor<DocumentAccessor> result = runIterableDocumentTask(task, props);
		logger.trace("storeDocuments.exit; results: {}", result);
		return result;
	}
	
	@Override
	public DocumentAccessor removeDocument(String uri, Properties props) throws BagriException {
		logger.trace("removeDocument.enter; uri: {}", uri);
		repo.getHealthManagement().checkClusterState();
		props = checkDocumentProperties(props);
		DocumentRemover task = new DocumentRemover(repo.getClientId(), repo.getTransactionId(), props, uri, repo.getDistributionStrategy());
		DocumentAccessor result = runSimpleDocumentTask(task, props);
		logger.trace("removeDocument.exit; returning: {}", result);
		return result;
	}

	@Override
	public ResultCursor<DocumentAccessor> removeDocuments(String pattern, Properties props) throws BagriException {
		logger.trace("removeDocuments.enter; pattern: {}", pattern);
		repo.getHealthManagement().checkClusterState();
		props = checkDocumentProperties(props);
		DocumentsRemover task = new DocumentsRemover(repo.getClientId(), repo.getTransactionId(), props, pattern);
		ResultCursor<DocumentAccessor> result = runIterableDocumentTask(task, props);
		logger.trace("removeDocuments.exit; results: {}", result);
		return result;
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
	
	private DocumentAccessor runSimpleDocumentTask(Callable<DocumentAccessor> task, Properties props) throws BagriException {
		//checkDocumentProperties(props);
		Future<DocumentAccessor> future = execService.submit(task);
		try {
			DocumentAccessor result = future.get();
			logger.trace("storeDocument.exit; returning: {}", result);
			return (DocumentAccessor) result;
		} catch (InterruptedException | ExecutionException ex) {
			// the document could be processed anyway..
			logger.error("runSimpleDocumentTask.error", ex);
			throw new BagriException(ex, ecDocument);
		}
	}
	
	@SuppressWarnings("resource")
	private ResultCursor<DocumentAccessor> runIterableDocumentTask(Callable<ResultCursor<DocumentAccessor>> task, Properties props) throws BagriException {
		ResultCursor<DocumentAccessor> result;
		//checkDocumentProperties(props);
		boolean asynch = Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"));
		Map<Member, Future<ResultCursor<DocumentAccessor>>> results = execService.submitToAllMembers(task);
		try {
			if (asynch) {
				// get the fastest result somehow..
				// no need to use combined cursor as results from all members 
				// will go to the queue anyway 
				result = results.values().iterator().next().get();
				((QueuedCursorImpl<DocumentAccessor>) result).init(repo.getHazelcastClient());
			} else {
				if (repo.getHazelcastClient().getCluster().getMembers().size() > 1) { 
					int fetchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
					CombinedCursorImpl<DocumentAccessor> comb = new CombinedCursorImpl<>(fetchSize);
					for (Map.Entry<Member, Future<ResultCursor<DocumentAccessor>>> entry: results.entrySet()) {
						comb.addResults(entry.getValue().get());
					}
					result = comb;
				} else {
					result = results.values().iterator().next().get();
				}
			}
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("runIterableDocumentTask; error getting result", ex);
			throw new BagriException(ex, ecDocument);
		}
		return result;
	}

	private int updateDocumentCollections(String uri, boolean add, String[] collections) {
		Properties props = checkDocumentProperties(null);
		DocumentCollectionUpdater task = new DocumentCollectionUpdater(repo.getClientId(), props, uri, repo.getDistributionStrategy(), add, collections);
		DocumentKey key = getDocumentKey(uri);
		return (Integer) xddCache.executeOnKey(key, task);
	}

}
