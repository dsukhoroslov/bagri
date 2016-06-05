package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.query.PathBuilder.*;
import static com.bagri.common.config.XDMConfigConstants.*;
import static com.bagri.common.util.FileUtils.def_encoding;
import static com.bagri.common.util.XMLUtils.*;
import static com.bagri.xdm.common.XDMConstants.*;
import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;
import static com.bagri.xdm.cache.api.XDMCacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.xdm.common.XDMConstants.pn_client_txTimeout;
import static com.bagri.xdm.domain.XDMDocument.dvFirst;
import static com.bagri.xdm.domain.XDMDocument.clnDefault;
import static com.bagri.xdm.system.XDMDataFormat.df_xml;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Source;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.util.PropUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.impl.DocumentManagementServer;
import com.bagri.xdm.cache.hazelcast.predicate.CollectionPredicate;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMKeyFactory;
import com.bagri.xdm.common.XDMParser;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMFragmentedDocument;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.system.XDMCollection;
import com.bagri.xdm.system.XDMFragment;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xdm.system.XDMTriggerAction.Order;
import com.bagri.xdm.system.XDMTriggerAction.Scope;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentManagementImpl extends DocumentManagementServer {
	
	private XDMKeyFactory factory;
	private RepositoryImpl repo;
    private HazelcastInstance hzInstance;
    private IndexManagementImpl indexManager;
    private TransactionManagementImpl txManager;
    private TriggerManagementImpl triggerManager;

    private IdGenerator<Long> docGen;
    private Map<XDMDocumentKey, Source> srcCache;
    private IMap<XDMDocumentKey, String> cntCache;
	private IMap<XDMDocumentKey, XDMDocument> xddCache;
    private IMap<XDMDataKey, XDMElements> xdmCache;

    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;
    
    public void setRepository(RepositoryImpl repo) {
    	this.repo = repo;
    	this.factory = repo.getFactory();
    	//this.model = repo.getModelManagement();
    	this.txManager = (TransactionManagementImpl) repo.getTxManagement();
    	this.triggerManager = (TriggerManagementImpl) repo.getTriggerManagement();
    }
    
    IMap<XDMDocumentKey, String> getContentCache() {
    	return cntCache;
    }

    IMap<XDMDocumentKey, XDMDocument> getDocumentCache() {
    	return xddCache;
    }

    IMap<XDMDataKey, XDMElements> getElementCache() {
    	return xdmCache;
    }
    
    public void setDocumentIdGenerator(IdGenerator<Long> docGen) {
    	this.docGen = docGen;
    }
    
    public void setXddCache(IMap<XDMDocumentKey, XDMDocument> cache) {
    	this.xddCache = cache;
    }

    public void setXdmCache(IMap<XDMDataKey, XDMElements> cache) {
    	this.xdmCache = cache;
    }

    public void setContentCache(IMap<XDMDocumentKey, String> cache) {
    	this.cntCache = cache;
    	this.srcCache = new ConcurrentHashMap<XDMDocumentKey, Source>();
    }
    
    //@Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
	}
	
    public void setIndexManager(IndexManagementImpl indexManager) {
    	this.indexManager = indexManager;
    }
    
    public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
    	this.queue = queue;
    }

    public void setStatsEnabled(boolean enable) {
    	this.enableStats = enable;
    }
    
    private Set<XDMDataKey> getDocumentElementKeys(String path, long[] fragments, int docType) {
    	Set<Integer> parts = model.getPathElements(docType, path);
    	Set<XDMDataKey> keys = new HashSet<XDMDataKey>(parts.size()*fragments.length);
    	// not all the path keys exists as data key for particular document!
    	for (long docKey: fragments) {
	    	for (Integer part: parts) {
	    		keys.add(factory.newXDMDataKey(docKey, part));
	    	}
    	}
    	return keys;
    }
    
    public Collection<XDMElements> getDocumentElements(String uri) {
		XDMDocument doc = getDocument(uri);
		if (doc == null) {
			return null;
		}

		int typeId = doc.getTypeId();
		Set<XDMDataKey> keys = getDocumentElementKeys(model.getDocumentRoot(typeId), doc.getFragments(), typeId);
		Map<XDMDataKey, XDMElements> elements = xdmCache.getAll(keys);
		return elements.values();
    }
    
	public boolean checkDocumentCollectionCommited(long docKey, int clnId) throws XDMException {
		
		// TODO: make this behavior configurable! 
		// check if any docs were removed
		//if (txManager.getCurrentTxId() == TX_NO) {
		//	return xddCache.containsKey(factory.newXDMDocumentKey(docId));
		//}
		
		XDMDocument doc = getDocument(docKey);
		if (doc != null) {
			if (!doc.hasCollection(clnId)) {
				return false;
			}
			if (doc.getTxFinish() > TX_NO && txManager.isTxVisible(doc.getTxFinish())) {
				return false;
			}
			return txManager.isTxVisible(doc.getTxStart());
		}
		return false;
	}

	public boolean checkDocumentCommited(long docKey) throws XDMException {
		
		// TODO: make this behavior configurable! 
		// check if any docs were removed
		//if (txManager.getCurrentTxId() == TX_NO) {
		//	return xddCache.containsKey(factory.newXDMDocumentKey(docId));
		//}
		
		XDMDocument doc = getDocument(docKey);
		if (doc != null) {
			if (doc.getTxFinish() > TX_NO && txManager.isTxVisible(doc.getTxFinish())) {
				return false;
			}
			return txManager.isTxVisible(doc.getTxStart());
		}
		return false;
	}

	private String getDataFormat(Properties props) {
		return PropUtils.getProperty(props, xdm_document_data_format, df_xml);
	}
	
	int indexElements(int docType, int pathId) throws XDMException {
		Set<XDMDocumentKey> docKeys = getDocumentsOfType(docType);
		String path = model.getPath(pathId).getPath();
		int cnt = 0;
		for (XDMDocumentKey docKey: docKeys) {
			XDMDataKey xdk = factory.newXDMDataKey(docKey.getKey(), pathId);
			XDMElements elts = xdmCache.get(xdk);
			if (elts != null) {
				for (XDMElement elt: elts.getElements()) {
					indexManager.addIndex(docKey.getKey(), pathId, path, elt.getValue());
					cnt++;
				}
			}
		}
		return cnt;
	}

	int deindexElements(int docType, int pathId) {
		Set<XDMDocumentKey> docKeys = getDocumentsOfType(docType);
		int cnt = 0;
		for (XDMDocumentKey docKey: docKeys) {
			XDMDataKey xdk = factory.newXDMDataKey(docKey.getKey(), pathId);
			XDMElements elts = xdmCache.get(xdk);
			if (elts != null) {
				for (XDMElement elt: elts.getElements()) {
					indexManager.removeIndex(docKey.getKey(), pathId, elt.getValue());
					cnt++;
				}
			}
		}
		return cnt;
	}

	private int deindexElements(long docKey, int pathId) {
		int cnt = 0;
		XDMDataKey xdk = factory.newXDMDataKey(docKey, pathId);
		XDMElements elts = xdmCache.get(xdk);
		if (elts != null) {
			for (XDMElement elt: elts.getElements()) {
				indexManager.removeIndex(docKey, pathId, elt.getValue());
				cnt++;
			}
		}
		logger.trace("deindexElements.exit; deindexed elements: {} for docKey: {}, pathId: {}", cnt, docKey, pathId);
		return cnt;
	}
	
	@SuppressWarnings("unchecked")
	private Set<XDMDocumentKey> getDocumentsOfType(int docType) {
   		Predicate<XDMDocumentKey, XDMDocument> f = Predicates.and(Predicates.equal("typeId", docType), 
   				Predicates.equal("txFinish", 0L));
		return xddCache.keySet(f);
	}
	
	public XDMDocument getDocument(long docKey) {
		XDMDocument doc = getDocument(factory.newXDMDocumentKey(docKey)); 
		//logger.trace("getDocument; returning: {}", doc);
		return doc;
	}
	
	private XDMDocument getDocument(XDMDocumentKey docKey) {
		return xddCache.get(docKey); 
	}

	@Override
	public XDMDocument getDocument(String uri) {
		XDMDocument doc = null;
		XDMDocumentKey docKey = getDocumentKey(uri, false, false);
		if (docKey != null) {
			doc = getDocument(docKey);
		}
		//logger.trace("getDocument; returning: {}", doc);
		return doc;
	}

    private XDMDocumentKey getDocumentKey(String uri, boolean next, boolean acceptClosed) {
    	Set<XDMDocumentKey> keys = xddCache.localKeySet(Predicates.equal("uri", uri));
    	if (keys.isEmpty()) {
    		if (next) {
    			XDMDocumentKey key = factory.newXDMDocumentKey(uri, 0, dvFirst);
    			// TODO: bad case: most of the time it'll hit database!
    			// try to use some internal service (PopulationManager?) instead!
    			while (xddCache.containsKey(key)) {
    				key = factory.newXDMDocumentKey(uri, key.getRevision() + 1, dvFirst);
    			}
    			return key;
    		}
    		return null;
    	}
    	XDMDocumentKey last = Collections.max(keys, new Comparator<XDMDocumentKey>() {

			@Override
			public int compare(XDMDocumentKey key1, XDMDocumentKey key2) {
				return key1.getVersion() - key2.getVersion();
			}
    		
    	});
    	if (next) {
    		return factory.newXDMDocumentKey(uri, last.getRevision(), last.getVersion() + 1);
    	}
    	if (acceptClosed) {
    		return last;
    	}
    	XDMDocument lastDoc = xddCache.get(last);
    	try {
    		if (lastDoc.getTxFinish() == TX_NO || !txManager.isTxVisible(lastDoc.getTxFinish())) {
    			return last;
    		}
    		// shouldn't we return previous version otherwise?
    	} catch (XDMException ex) {
    		logger.error("getDocumentKey.error", ex);
    		// ??
    	}
    	logger.info("getDocumentKey; the latest document version is finished already: {}", lastDoc);
    	return null;
    }
 	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<String> getDocumentUris(String pattern) {
		// TODO: search by uri only so far;
		// implement other search types: by dates, owner, etc..
		logger.trace("getDocumentUris.enter; got pattern: {}", pattern);
   		Predicate<XDMDocumentKey, XDMDocument> f = Predicates.and(Predicates.regex("uri", pattern), 
   				Predicates.equal("txFinish", TX_NO));
		Set<XDMDocumentKey> docKeys = xddCache.keySet(f);

		// should also check if doc's start transaction is committed..
		List<String> result = new ArrayList<>(docKeys.size());
		for (XDMDocumentKey docKey: docKeys) {
			result.add(pattern);
		}
		logger.trace("getDocumentUris.exit; returning: {}", result.size());
		return result;
	}
	
	@Override
	public Collection<String> buildDocument(Set<Long> docKeys, String template, Map<String, String> params) throws XDMException {
        logger.trace("buildDocument.enter; docKeys: {}", docKeys.size());
		long stamp = System.currentTimeMillis();
        Collection<String> result = new ArrayList<String>(docKeys.size());
		
        int typeId = -1;
        String root = null;
		for (Iterator<Long> itr = docKeys.iterator(); itr.hasNext(); ) {
			XDMDocumentKey docKey = factory.newXDMDocumentKey(itr.next());
			if (hzInstance.getPartitionService().getPartition(docKey).getOwner().localMember()) {
				XDMDocument doc = xddCache.get(docKey);
				if (doc == null) {
					logger.info("buildDocument; lost document for key {}", docKey);
					continue;
				}

				StringBuilder buff = new StringBuilder(template);
				for (Map.Entry<String, String> param: params.entrySet()) {
					String key = param.getKey();
					String path = param.getValue();
					if (doc.getTypeId() != typeId) {
						typeId = doc.getTypeId();
						root = model.getDocumentRoot(typeId); 
					}
					String xml = null;
					if (path.equals(root)) {
						xml = cntCache.get(docKey);
					}
					if (xml == null) {
				        logger.trace("buildDocument; no content found for doc key: {}", docKey);
						xml = buildElement(path, doc.getFragments(), doc.getTypeId());
					}
					int pos = 0;
					while (true) {
						int idx = buff.indexOf(key, pos);
						if (idx < 0) break;
						buff.replace(idx, idx + key.length(), xml);
						pos = idx + xml.length();
					}
				}
				result.add(buff.toString());
			} else {
				// remove is not supported by the HZ iterator provided! 
				// actually, don't think we have to do it at all..
				//itr.remove();
		        logger.debug("buildDocument; docId {} is not local, processing skipped", docKey);
			}
		}
        
		stamp = System.currentTimeMillis() - stamp;
        logger.trace("buildDocument.exit; time taken: {}; returning: {}", stamp, result.size()); 
        return result;
	}
    
	private String buildElement(String path, long[] fragments, int docType) throws XDMException {
        logger.trace("buildElement.enter; got path: {}; docType: {}", path, docType); 
    	Set<XDMDataKey> xdKeys = getDocumentElementKeys(path, fragments, docType);
    	String dataFormat = df_xml;
    	String content = repo.getBuilder(dataFormat).buildString(xdmCache.getAll(xdKeys));
        logger.trace("buildXml.exit; returning xml length: {}", content.length()); 
       	return content;
    }
    
	@Override
	public Object getDocumentAsBean(String uri) throws XDMException {
		String xml = getDocumentAsString(uri);
		if (xml == null) {
			return null;
		}
		try {
			return beanFromXML(xml);
		} catch (IOException ex) {
			throw new XDMException(ex.getMessage(), XDMException.ecInOut);
		}
	}

	@Override
	public Map<String, Object> getDocumentAsMap(String uri) throws XDMException {
		String xml = getDocumentAsString(uri);
		if (xml == null) {
			return null;
		}
		return mapFromXML(xml);
	}

	@Override
	public String getDocumentAsString(String uri) throws XDMException {
		XDMDocumentKey docKey = getDocumentKey(uri, false, false);
		if (docKey == null) {
			//throw new XDMException("No document found for document Id: " + docId, XDMException.ecDocument);
			logger.info("getDocumentAsString; can not find active document for uri: {}", uri);
			return null;
		}
		return getDocumentAsString(docKey);
	}

	@Override
	public String getDocumentAsString(long docKey) throws XDMException {
		XDMDocumentKey xdmKey = factory.newXDMDocumentKey(docKey);
		return getDocumentAsString(xdmKey);
	}
	
	public String getDocumentAsString(XDMDocumentKey docKey) throws XDMException {
		
		String content = cntCache.get(docKey);
		if (content == null) {
			XDMDocument doc = getDocument(docKey);
			if (doc == null) {
				logger.info("getDocumentAsString; no document found for key: {}", docKey);
				return null;
			}
			
			// if docId is not local then buildDocument returns null!
			// query docId owner node for the XML instead
			if (hzInstance.getPartitionService().getPartition(docKey).getOwner().localMember()) {
				String root = model.getDocumentRoot(doc.getTypeId());
				Map<String, String> params = new HashMap<String, String>();
				params.put(":doc", root);
				Collection<String> results = buildDocument(Collections.singleton(docKey.getKey()), ":doc", params);
				if (!results.isEmpty()) {
					content = results.iterator().next();
					cntCache.set(docKey, content);
				}
			} else {
				DocumentContentProvider xp = new DocumentContentProvider(repo.getClientId(), doc.getUri()); 
				IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
				Future<String> future = execService.submitToKeyOwner(xp, doc.getUri());
				try {
					content = future.get();
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("getDocumentAsString; error getting result", ex);
					throw new XDMException(ex, XDMException.ecDocument);
				}
			}
		}
		return content;
	}

	//@Override
	//public Source getDocumentAsSource(String uri) {
	//	return srcCache.get(factory.newXDMDocumentKey(docId.getDocumentKey()));
	//}
	
	private XDMCollection getTypedCollection(XDMSchema schema, String typePath) {
		for (XDMCollection collect: schema.getCollections()) {
			String cPath = collect.getDocumentType();
			// TODO: very inefficient to normalize collections over and over again!
			cPath = model.normalizePath(cPath);
			if (cPath != null && typePath.equals(cPath)) {
				return collect;
			}
		}
		return null;
	}
	
	public String checkDefaultDocumentCollection(XDMDocument doc) {
		String typePath = model.getDocumentRoot(doc.getTypeId());
		XDMCollection cln = getTypedCollection(repo.getSchema(), typePath);
		logger.trace("checkDefaultDocumentCollection; got collection: {} for typePath: {}", cln, typePath);
		if (cln != null) {
			doc.addCollection(cln.getId());
			return cln.getName();
		}
		return null;
	}
	
	public XDMDocument createDocument(XDMDocumentKey docKey, String uri, String content, Properties props) throws XDMException {
		logger.trace("createDocument.enter; uri: {}; props: {}", uri, props);
		String dataFormat = getDataFormat(props);
		if (dataFormat == null) {
			dataFormat = uri.substring(uri.lastIndexOf(".") + 1);
		}

		int[] collections = null; 
		if (props != null) {
			String prop = props.getProperty(xdm_document_collections);
			if (prop != null) {
				StringTokenizer tc = new StringTokenizer(prop, ", ", false);
				collections = new int[tc.countTokens()];
				int idx = 0;
				while (tc.hasMoreTokens()) {
					String clName = tc.nextToken();
					XDMCollection cln = repo.getSchema().getCollection(clName);
					if (cln != null) {
						collections[idx] = cln.getId();
					}
					idx++;
				}
			}
		}

		XDMDocument doc = createDocument(docKey, uri, content, dataFormat, new Date(), repo.getUserName(), txManager.getCurrentTxId(), collections, false);
		
		Scope scope;
		if (docKey.getVersion() == dvFirst) {
			scope = Scope.insert;
			triggerManager.applyTrigger(doc, Order.before, scope);
		} else {
			scope = Scope.update;
			// trigger has been already invoked in storeDocument..
		}
		xddCache.set(docKey, doc);
		cntCache.set(docKey, content);
		triggerManager.applyTrigger(doc, Order.after, scope);

		logger.trace("createDocument.exit; returning: {}", doc);
		return doc;
	}

	@SuppressWarnings("unchecked")
	public XDMDocument createDocument(XDMDocumentKey docKey, String uri, String content, String dataFormat, Date createdAt, String createdBy, 
			long txStart, int[] collections, boolean addContent) throws XDMException {
		
		XDMParser parser = repo.getParser(dataFormat);
		List<XDMData> data = parser.parse(content);

		Object[] ids = loadElements(docKey.getKey(), data);
		List<Long> fragments = (List<Long>) ids[0];
		if (fragments == null) {
			logger.warn("createDocument.exit; the document is not valid as it has no root element");
			throw new XDMException("invalid document", XDMException.ecDocument);
		} 
		int docType = fragments.get(0).intValue();
		XDMDocument doc;
		if (fragments.size() == 1) {
			doc = new XDMDocument(docKey.getKey(), uri, docType, txStart, TX_NO, createdAt, createdBy, def_encoding, content.length(), data.size());
		} else {
			doc = new XDMFragmentedDocument(docKey.getKey(), uri, docType, txStart, TX_NO, createdAt, createdBy, def_encoding, content.length(), data.size());
			long[] fa = new long[fragments.size()];
			fa[0] = docKey.getKey();
			for (int i=1; i < fragments.size(); i++) {
				fa[i] = fragments.get(i);
			}
			((XDMFragmentedDocument) doc).setFragments(fa);
		}

		List<String> clns = new ArrayList<>();
		if (collections != null && collections.length > 0) {
			doc.setCollections(collections);
			for (XDMCollection cln: repo.getSchema().getCollections()) {
				for (int clnId: collections) {
					if (clnId == cln.getId()) {
						clns.add(cln.getName());
						break;
					}
				}
			}
		}

		if (clns.size() == 0) {
			String cln = checkDefaultDocumentCollection(doc);
			if (cln != null) {
				clns.add(cln);
			}
		}
		
		if (addContent) {
			cntCache.set(docKey, content);
		}

		// invalidate cached query results. always do this, even on load?
		Set<Integer> paths = (Set<Integer>) ids[1];
		((QueryManagementImpl) repo.getQueryManagement()).invalidateQueryResults(paths);

		// update statistics
		for (String cln: clns) {
			updateStats(cln, true, data.size(), doc.getFragments().length);
			//updateStats(cln, true, paths.size(), doc.getFragments().length);
		}
		updateStats(null, true, data.size(), doc.getFragments().length);
		return doc;
	}
	
	private Object[] loadElements(long docKey, List<XDMData> data) throws XDMException {
		
		long stamp = System.currentTimeMillis();
		XDMData root = getDataRoot(data);
		if (root != null) {
			int docType = model.translateDocumentType(root.getPath());
			// normalize it ASAP !?
			model.normalizeDocumentType(docType);
			Map<XDMDataKey, XDMElements> elements = new HashMap<XDMDataKey, XDMElements>(data.size());
			
			Set<Integer> fragments = new HashSet<>();
			for (XDMFragment fragment: repo.getSchema().getFragments()) {
				int fType = model.getDocumentType(fragment.getDocumentType());
				if (fType == docType) {
					XDMPath path = model.getPath(fragment.getPath());
					if (path != null) {
						fragments.add(path.getPathId());
					} else if (isRegexPath(fragment.getPath())) {
						String nPath = model.normalizePath(fragment.getPath());
						fragments.addAll(model.translatePathFromRegex(docType, regexFromPath(nPath)));
					} else {	
						logger.info("loadElements; path not found for fragment: {}; docType: {} ({})", 
								fragment, root.getPath(), docType);
					}
				}
			}
			logger.debug("loadElements; fragments found: {}; for docType: {} ({}); docKey: {}", 
					fragments, root.getPath(), docType, docKey);
			
			long fraPath = docKey;
			long fraPost = 0;
			int size = 1;
			if (fragments.size() > 0) {
				size = data.size() / fragments.size();
			}
			Set<Integer> pathIds = new HashSet<>(size);
			List<Long> fragIds = new ArrayList<>(size);
			fragIds.add(new Long(docType));
			for (XDMData xdm: data) {
				if (fragments.contains(xdm.getPathId())) {
					// TODO: why don't we shift it?
					//XDMDocumentKey kk = factory.newXDMDocumentKey(docGen.next(), 0);
					//fraPath = kk.getKey();
					int hash = docGen.next().intValue(); 
					fraPath = XDMDocumentKey.toKey(hash, 0, 0);
					fragIds.add(fraPath);
					//fraPost = xdm.getPostId();
					fraPost = model.getPath(xdm.getPath()).getPostId();
				} else if (fraPost > 0 && xdm.getPathId() > fraPost) {
					fraPath = docKey;
					fraPost = 0;
				}
				pathIds.add(xdm.getPathId());
				XDMDataKey xdk = factory.newXDMDataKey(fraPath, xdm.getPathId());
				//logger.info("loadElements; got key: {}; fraPath: {}; fraPost: {}, partition: {}", 
				//		xdk, fraPath, fraPost, hzInstance.getPartitionService().getPartition(xdk));
				XDMElements xdes = elements.get(xdk);
				if (xdes == null) {
					xdes = new XDMElements(xdk.getPathId(), null);
					elements.put(xdk, xdes);
				}
				xdes.addElement(xdm.getElement());
				indexManager.addIndex(docKey, xdm.getPathId(), xdm.getPath(), xdm.getValue());
			}
			xdmCache.putAll(elements);
			
			stamp = System.currentTimeMillis() - stamp;
			logger.debug("loadElements; cached {} elements for docKey: {}; fragments: {}; time taken: {}", 
					elements.size(), docKey, fragIds.size(), stamp);
			//model.normalizeDocumentType(docType);
			Object[] result = new Object[2];
			result[0] = fragIds;
			result[1] = pathIds;
			return result;
		}
		return null;
	}
	
	@Override
	public XDMDocument storeDocumentFromBean(String uri, Object bean, Properties props) throws XDMException {
		try {
			String xml = beanToXML(bean);
			if (xml == null || xml.trim().length() == 0) {
				throw new XDMException("Can not convert bean [" + bean + "] to XML", XDMException.ecDocument);
			}
			logger.trace("storeDocumentFromBean; converted bean: {}", xml); 
			
			if (props != null) {
				props.setProperty(xdm_document_data_format, df_xml);
			}
			return storeDocumentFromString(uri, xml, props);
		} catch (IOException ex) {
			throw new XDMException(ex.getMessage(), XDMException.ecInOut);
		}
	}

	@Override
	public XDMDocument storeDocumentFromMap(String uri, Map<String, Object> fields, Properties props) throws XDMException {
		String xml = mapToXML(fields);
		if (xml == null || xml.trim().length() == 0) {
			throw new XDMException("Can not convert map [" + fields + "] to XML", XDMException.ecDocument);
		}
		logger.trace("storeDocumentFromMap; converted map: {}", xml); 
		
		if (props != null) {
			props.setProperty(xdm_document_data_format, df_xml);
		}
		return storeDocumentFromString(uri, xml, props);
	}
	
	//@Override
	//public XDMDocument storeDocumentFromSource(String uri, Source source, Properties props) {
	//	srcCache.put(factory.newXDMDocumentKey(docId.getDocumentKey()), source);
	//	return xddCache.get(docId);
	//}
	
	@Override
	public XDMDocument storeDocumentFromString(String uri, String content, Properties props) throws XDMException {
		logger.trace("storeDocumentFromString.enter; uri: {}; content: {}; props: {}", uri, content.length(), props);
		if (uri == null) {
			throw new XDMException("Empty URI passed", XDMException.ecDocument); 
		}

		boolean update = false;
		String storeMode = PropUtils.getProperty(props, pn_client_storeMode, pv_client_storeMode_merge);
		
		XDMDocumentKey docKey = getDocumentKey(uri, false, true);
		if (docKey == null) {
			if (pv_client_storeMode_update.equals(storeMode)) {
				throw new XDMException("No document found for update. " +  uri, XDMException.ecDocument); 
			}
			docKey = factory.newXDMDocumentKey(uri, 0, dvFirst);
		} else {
			if (pv_client_storeMode_insert.equals(storeMode)) {
				throw new XDMException("Document with URI '" + uri + "' already exists; docKey: " + docKey, 
						XDMException.ecDocument); 
			}
		    XDMDocument doc = getDocument(docKey);
			update = (doc != null && (doc.getTxFinish() == TX_NO || !txManager.isTxVisible(doc.getTxFinish())));
		}
		
		String value = PropUtils.getProperty(props, pn_client_txTimeout, null);
		long timeout = txManager.getTransactionTimeout(); 
		if (value != null) {
			timeout = Long.parseLong(value);
		}
		boolean locked = lockDocument(docKey, timeout);
		if (locked) {
			try {
				XDMDocumentKey newKey = docKey;
				if (update) {
				    XDMDocument doc = getDocument(newKey);
				    if (doc != null) {
				    	if (doc.getTxFinish() > TX_NO && txManager.isTxVisible(doc.getTxFinish())) {
				    		throw new XDMException("Document with key: " + doc.getDocumentKey() + 
				    				", version: " + doc.getVersion() + " has been concurrently updated", 
				    				XDMException.ecDocument);
				    	}
				    	logger.trace("storeDocumentFromString; going to update document: {}", doc);
				    	// we must finish old Document and create a new one!
						triggerManager.applyTrigger(doc, Order.before, Scope.update);
				    	doc.finishDocument(txManager.getCurrentTxId());
				    	// do this asynch after tx?
				    	((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(newKey.getKey());
				    	xddCache.set(docKey, doc);
						newKey = getDocumentKey(uri, true, false);
				    	// shouldn't we lock the newKey too?
				    }
				}
				XDMDocument result = createDocument(newKey, uri, content, props);
				if (update) {
					txManager.updateCounters(0, 1, 0);
				} else {
					txManager.updateCounters(1, 0, 0);
				}
			    return result;
			} catch (XDMException ex) {
				throw ex;
			} catch (Exception ex) {
				logger.error("storeDocumentFromString.error; uri: " + uri, ex);
				throw new XDMException(ex, XDMException.ecDocument);
			} finally {
				unlockDocument(docKey);
			}
		} else {
    		throw new XDMException("Was not able to aquire lock on Document: " + docKey + 
    				", timeout: " + txManager.getTransactionTimeout(), XDMException.ecDocument);
		}
	}

	@Override
	public void removeDocument(String uri) throws XDMException {
		logger.trace("removeDocument.enter; uri: {}", uri);
		//XDMDocumentKey docKey = getDocumentKey(docId);
	    //if (docKey == null) {
    	//	throw new XDMException("No document found for document Id: " + docId, XDMException.ecDocument);
	    //}
		if (uri == null) {
			throw new XDMException("No Document URI passed", XDMException.ecDocument); 
		}
		
		XDMDocumentKey docKey = getDocumentKey(uri, false, false);
		if (docKey == null) {
			logger.info("removeDocument; no active document found for uri: {}", uri);
			return;
		}
		
	    boolean removed = false;
		boolean locked = lockDocument(docKey, txManager.getTransactionTimeout());
		if (locked) {
			try {
			    XDMDocument doc = getDocument(docKey);
			    if (doc != null && (doc.getTxFinish() == TX_NO || !txManager.isTxVisible(doc.getTxFinish()))) {
					triggerManager.applyTrigger(doc, Order.before, Scope.delete); 
			    	doc.finishDocument(txManager.getCurrentTxId()); 
			    	xddCache.set(docKey, doc);
			    	((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(doc.getDocumentKey());
			    	triggerManager.applyTrigger(doc, Order.after, Scope.delete); 
			    	txManager.updateCounters(0, 0, 1);
				    removed = true;
			    }
			} catch (XDMException ex) {
				throw ex;
			} catch (Exception ex) {
				logger.error("removeDocument.error; uri: " + uri, ex);
				throw new XDMException(ex, XDMException.ecDocument);
			} finally {
				unlockDocument(docKey);
			}
		} else {
    		throw new XDMException("Was not able to aquire lock on Document: " + docKey + 
    				", timeout: " + txManager.getTransactionTimeout(), XDMException.ecDocument);
		}
		logger.trace("removeDocument.exit; removed: {}", removed);
	}
	
	public void cleanDocument(XDMDocumentKey docKey, boolean complete) {
		logger.trace("cleanDocument.enter; docKey: {}, complete: {}", docKey, complete);
	    XDMDocument doc = getDocument(docKey);
	    boolean cleaned = false;
	    if (doc != null) {
			cntCache.delete(docKey);
			srcCache.remove(docKey);
	    	int size = deleteDocumentElements(doc.getFragments(), doc.getTypeId());
	    	Collection<Integer> pathIds = indexManager.getTypeIndexes(doc.getTypeId(), true);
	    	for (int pathId: pathIds) {
	    		deindexElements(docKey.getKey(), pathId);
	    	}
	    	if (complete) {
	    		xddCache.delete(docKey);
	    	}
	    	cleaned = true;
	    	
			// update statistics
			for (XDMCollection cln: repo.getSchema().getCollections()) {
				if (doc.hasCollection(cln.getId())) { 
					updateStats(cln.getName(), false, size, doc.getFragments().length);
				}
			}
			updateStats(null, false, size, doc.getFragments().length);
	    }
    	((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(docKey.getKey());
		logger.trace("cleanDocument.exit; cleaned: {}", cleaned);
	}

	public void evictDocument(XDMDocumentKey xdmKey, XDMDocument xdmDoc) {
		logger.trace("evictDocument.enter; xdmKey: {}, xdmDoc: {}", xdmKey, xdmDoc);
		cntCache.delete(xdmKey);
		srcCache.remove(xdmKey);
    	int size = deleteDocumentElements(xdmDoc.getFragments(), xdmDoc.getTypeId());

    	//Collection<Integer> pathIds = indexManager.getTypeIndexes(xdmDoc.getTypeId(), true);
    	//for (int pathId: pathIds) {
    	//	deindexElements(docKey.getKey(), pathId);
    	//}
    	
		// update statistics
		//for (XDMCollection cln: repo.getSchema().getCollections()) {
		//	if (doc.hasCollection(cln.getId())) { 
		//		updateStats(cln.getName(), false, size, doc.getFragments().length);
		//	}
		//}
		//updateStats(null, false, size, doc.getFragments().length);
		logger.trace("evictDocument.exit; evicted: {}", size);
	}
	
	private int deleteDocumentElements(long[] fragments, int typeId) {

    	int cnt = 0;
    	//Set<XDMDataKey> localKeys = xdmCache.localKeySet();
    	Collection<XDMPath> allPaths = model.getTypePaths(typeId);
		logger.trace("deleteDocumentElements; got {} possible paths to remove; xdmCache size: {}", 
				allPaths.size(), xdmCache.size());
		int iCnt = 0;
		for (long docId: fragments) {
	        for (XDMPath path: allPaths) {
	        	int pathId = path.getPathId();
	        	XDMDataKey dKey = factory.newXDMDataKey(docId, pathId);
	        	if (indexManager.isPathIndexed(pathId)) {
		       		XDMElements elts = xdmCache.remove(dKey);
		       		if (elts != null) {
		       			for (XDMElement elt: elts.getElements()) {
		       				indexManager.removeIndex(docId, pathId, elt.getValue());
		       				iCnt++;
		       			}
		       		}
	        	} else {
	        		xdmCache.delete(dKey);
	        	}
	   			cnt++;
	        }
		}
		logger.trace("deleteDocumentElements; deleted keys: {}; indexes: {}; xdmCache size after delete: {}",
				cnt, iCnt, xdmCache.size());
		return cnt;
	}

	public void rollbackDocument(XDMDocumentKey docKey) {
		logger.trace("rollbackDocument.enter; docKey: {}", docKey);
		boolean rolled = false;
	    XDMDocument doc = getDocument(docKey);
	    if (doc != null) {
	    	doc.finishDocument(TX_NO);
	    	xddCache.set(docKey, doc);
	    	rolled = true;
	    }
		logger.trace("rollbackDocument.exit; rolled back: {}", rolled);
	}
	
	@Override
	public Collection<String> getCollectionDocumentUris(String collection) throws XDMException {
		Set<XDMDocumentKey> docKeys;
		if (collection == null) {
			docKeys = xddCache.localKeySet();
		} else {
			XDMCollection cln = repo.getSchema().getCollection(collection);
			if (cln == null) {
				return null;
			}
			//int size = xddCache.size();
			Predicate<XDMDocumentKey, XDMDocument> clp = new CollectionPredicate(cln.getId());
			docKeys = xddCache.localKeySet(clp);
			// TODO: investigate it; the localKeySet returns extra empty key for some reason!
		}
		// TODO: use props to fetch docs in batches. otherwise we can get OOM here!
		Map<XDMDocumentKey, XDMDocument> docs = xddCache.getAll(docKeys);
		Set<String> result = new HashSet<>(docs.size());
		for (XDMDocument doc: docs.values()) {
		    if (doc.getTxFinish() == TX_NO || !txManager.isTxVisible(doc.getTxFinish())) {
				// check doc visibility via
				result.add(doc.getUri());
			}
		}
		return result;
	}

	Collection<Long> getCollectionDocumentKeys(int collectId) {
		//
		Set<XDMDocumentKey> docKeys;
		if (collectId == clnDefault) {
			// TODO: local or global keySet ?!
			docKeys = xddCache.keySet();
		} else {
			Predicate<XDMDocumentKey, XDMDocument> clp = new CollectionPredicate(collectId);
			// TODO: local or global keySet ?!
			docKeys = xddCache.keySet(clp);
		}
		List<Long> result = new ArrayList<>(docKeys.size());
		for (XDMDocumentKey key: docKeys) {
			result.add(key.getKey());
		}
		return result;
	}
	
	@Override
	public int removeCollectionDocuments(String collection) throws XDMException {
		logger.trace("removeCollectionDocuments.enter; collection: {}", collection);
		int cnt = 0;
		// remove local documents only?!
		Collection<String> uris = getCollectionDocumentUris(collection);
		for (String uri: uris) {
			removeDocument(uri);
			cnt++;
		}
		logger.trace("removeCollectionDocuments.exit; removed: {}", cnt);
		return cnt;
	}
	
	@Override
	public int addDocumentToCollections(String uri, String[] collections) {
		logger.trace("addDocumentsToCollections.enter; got uri: {}; collectIds: {}", uri, Arrays.toString(collections));
		int addCount = 0;
		int unkCount = 0;
		XDMDocument doc = getDocument(uri);
		if (doc != null) {
			// TODO: cache size in the doc itself? yes, done
			// but must fix stats to account this size 
			int size = 0;
			for (XDMCollection cln: repo.getSchema().getCollections()) {
				for (String collection: collections) {
					if (collection.equals(cln.getName())) {
						if (doc.addCollection(cln.getId())) {
							addCount++;
							updateStats(cln.getName(), true, size, doc.getFragments().length);
						}						
						break;
					}
				}
			}
			if (addCount > 0) {
				xddCache.set(factory.newXDMDocumentKey(doc.getDocumentKey()), doc);
			}
		} else {
			unkCount++;
		}
		logger.trace("addDocumentsToCollections.exit; added: {}; unknown: {}", addCount, unkCount);
		return addCount;
	}

	@Override
	public int removeDocumentFromCollections(String uri, String[] collections) {
		logger.trace("removeDocumentsFromCollections.enter; got uri: {}; collectIds: {}", uri, Arrays.toString(collections));
		int remCount = 0;
		int unkCount = 0;
		XDMDocument doc = getDocument(uri);
		if (doc != null) {
			int size = 0;
			for (XDMCollection cln: repo.getSchema().getCollections()) {
				for (String collection: collections) {
					if (collection.equals(cln.getName())) {
						if (doc.removeCollection(cln.getId())) {
							remCount++;
							updateStats(cln.getName(), false, size, doc.getFragments().length);
						}
						break;
					}
				}
			}
			if (remCount > 0) {
				xddCache.set(factory.newXDMDocumentKey(doc.getDocumentKey()), doc);
			}
		} else {
			unkCount++;
		}
		logger.trace("removeDocumentsFromCollections.exit; removed: {}; unknown: {}", remCount, unkCount);
		return remCount;
	}
	
	private boolean lockDocument(XDMDocumentKey docKey, long timeout) { //throws XDMException {
		
		boolean locked = false;
		if (timeout > 0) {
			try {
				locked = xddCache.tryLock(docKey, timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
				logger.error("lockDocument.error", ex);
				//throw new XDMException(ex);
			}
		} else {
			locked = xddCache.tryLock(docKey);
		}
		return locked;
	}

	private void unlockDocument(XDMDocumentKey docKey) {

		xddCache.unlock(docKey);
	}

	private void updateStats(String name, boolean add, int elements, int fragments) {
		if (enableStats) {
			if (!queue.offer(new StatisticsEvent(name, add, new Object[] {fragments, elements}))) {
				logger.warn("updateStats; queue is full!!");
			}
		}
	}

}
