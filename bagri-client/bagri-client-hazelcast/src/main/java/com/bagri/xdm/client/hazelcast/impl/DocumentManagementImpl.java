package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.*;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.client.common.impl.XDMDocumentManagementBase;
import com.bagri.xdm.client.hazelcast.data.DocumentKey;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentManagementImpl extends XDMDocumentManagementBase implements XDMDocumentManagement {

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
	public XDMDocument getDocument(long docId) {
		// do this via task/EP ??
		XDMDocument doc = xddCache.get(new DocumentKey(docId));
		if (doc == null) {
			logger.trace("getDocument; can not get document for key: {}; cache size is: {}", 
					docId, xddCache.size());
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
		// todo: check if too many docs ??
		return docKeys.iterator().next();
	}
	
	@Override
	public Iterator<Long> getDocumentIds(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getDocumentAsString(long docId) {
		// actually, I can try just get it from XML cache!
		
		long stamp = System.currentTimeMillis();
		logger.trace("getDocumentAsString.enter; got docId: {}", docId);
		
		String result = null;
		DocumentContentProvider xp = new DocumentContentProvider(repo.getClientId(), docId);
		Future<String> future = execService.submitToKeyOwner(xp, docId);
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentAsString; error getting result", ex);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getDocumentAsString.exit; got template results: {}; time taken {}", 
				result == null ? 0 : result.length(), stamp);
		return result;
	}

	public XDMDocument storeDocument(String xml) {

		return storeDocumentFromString(0, null, xml);
	}

	//public XDMDocument storeDocument(String uri, String xml) {
	//
	//	long docId = docGen.next();
	//	return storeDocument(docId, uri, xml);
	//}

	@Override
	public XDMDocument storeDocumentFromString(long docId, String uri, String xml) {
		
		if (xml == null) {
			throw new IllegalArgumentException("XML can not be null");
		}
		logger.trace("storeDocument.enter; docId: {}, uri: {}; xml: {}", docId, uri, xml.length());

		if (docId == 0 && uri == null) {
			docId = XDMDocumentKey.toKey(docGen.next(), 1);
		}
		// todo: override existing document -> create a new version ?
		
		DocumentCreator task = new DocumentCreator(repo.getClientId(), docId, 
				repo.getTransactionId(), uri, xml);
		Future<XDMDocument> future = execService.submitToKeyOwner(task, docId);
		try {
			XDMDocument result = future.get();
			logger.trace("storeDocument.exit; returning: {}", result);
			return (XDMDocument) result;
		} catch (InterruptedException | ExecutionException ex) {
			// the document could be stored anyway..
			logger.error("storeDocument.error", ex);
		}
		return null;
	}
	
	@Override
	public void removeDocument(long docId) {
		
		long stamp = System.currentTimeMillis();
		logger.trace("removeDocument.enter; docId: {}", docId);
		//XDMDocumentRemover proc = new XDMDocumentRemover();
		//Object result = xddCache.executeOnKey(docId, proc);
		
		DocumentRemover task = new DocumentRemover(repo.getClientId(), docId, repo.getTransactionId());
		Future<XDMDocument> future = execService.submitToKeyOwner(task, docId);
		try {
			XDMDocument result = future.get();
			logger.trace("removeDocument.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			//return (XDMDocument) result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("removeDocument.error: ", ex);
		}
	}

}
