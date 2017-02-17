package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.api.TransactionManagement.TX_NO;
import static com.bagri.core.model.Document.clnDefault;
import static com.bagri.core.model.Document.dvFirst;
import static com.bagri.core.query.PathBuilder.*;
import static com.bagri.core.server.api.CacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.core.system.DataFormat.df_xml;
import static com.bagri.support.util.FileUtils.def_encoding;
import static com.bagri.support.util.XMLUtils.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.bagri.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.core.DataKey;
import com.bagri.core.DocumentKey;
import com.bagri.core.KeyFactory;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.Document;
import com.bagri.core.model.Element;
import com.bagri.core.model.Elements;
import com.bagri.core.model.FragmentedDocument;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.DocumentManagement;
import com.bagri.core.server.api.impl.DocumentManagementBase;
import com.bagri.core.system.Collection;
import com.bagri.core.system.DataFormat;
import com.bagri.core.system.Fragment;
import com.bagri.core.system.Schema;
import com.bagri.core.system.TriggerAction.Order;
import com.bagri.core.system.TriggerAction.Scope;
import com.bagri.server.hazelcast.predicate.CollectionPredicate;
import com.bagri.server.hazelcast.predicate.DocumentPredicateBuilder;
import com.bagri.support.idgen.IdGenerator;
import com.bagri.support.stats.StatisticsEvent;
import com.bagri.support.util.PropUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentManagementImpl extends DocumentManagementBase implements DocumentManagement {
	
	private KeyFactory factory;
	private SchemaRepositoryImpl repo;
    private HazelcastInstance hzInstance;
    private IndexManagementImpl indexManager;
    private TransactionManagementImpl txManager;
    private TriggerManagementImpl triggerManager;

    private IdGenerator<Long> docGen;
    //private Map<XDMDocumentKey, Source> srcCache;
    private IMap<DocumentKey, String> cntCache;
	private IMap<DocumentKey, Document> xddCache;
    private IMap<DataKey, Elements> xdmCache;

    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;
    
    public void setRepository(SchemaRepositoryImpl repo) {
    	this.repo = repo;
    	this.factory = repo.getFactory();
    	//this.model = repo.getModelManagement();
    	this.txManager = (TransactionManagementImpl) repo.getTxManagement();
    	this.triggerManager = (TriggerManagementImpl) repo.getTriggerManagement();
    }
    
    IMap<DocumentKey, String> getContentCache() {
    	return cntCache;
    }

    IMap<DocumentKey, Document> getDocumentCache() {
    	return xddCache;
    }

    IMap<DataKey, Elements> getElementCache() {
    	return xdmCache;
    }
    
    public void setDocumentIdGenerator(IdGenerator<Long> docGen) {
    	this.docGen = docGen;
    }
    
    public void setContentCache(IMap<DocumentKey, String> cache) {
    	this.cntCache = cache;
    	//this.srcCache = new ConcurrentHashMap<XDMDocumentKey, Source>();
    }
    
    public void setXddCache(IMap<DocumentKey, Document> cache) {
    	this.xddCache = cache;
    }

    public void setXdmCache(IMap<DataKey, Elements> cache) {
    	this.xdmCache = cache;
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
    
    private Set<DataKey> getDocumentElementKeys(String path, long[] fragments, int docType) {
    	Set<Integer> parts = model.getPathElements(docType, path);
    	Set<DataKey> keys = new HashSet<DataKey>(parts.size()*fragments.length);
    	// not all the path keys exists as data key for particular document!
    	for (long docKey: fragments) {
	    	for (Integer part: parts) {
	    		keys.add(factory.newDataKey(docKey, part));
	    	}
    	}
    	return keys;
    }
    
    public java.util.Collection<Elements> getDocumentElements(String uri) {
		Document doc = getDocument(uri);
		if (doc == null) {
			return null;
		}

		int typeId = doc.getTypeId();
		Set<DataKey> keys = getDocumentElementKeys(model.getDocumentRoot(typeId), doc.getFragments(), typeId);
		Map<DataKey, Elements> elements = xdmCache.getAll(keys);
		return elements.values();
    }
    
	public String checkDocumentCommited(long docKey, int clnId) throws BagriException {
		
		// TODO: make this behavior configurable! 
		// check if any docs were removed
		//if (txManager.getCurrentTxId() == TX_NO) {
		//	return xddCache.containsKey(factory.newXDMDocumentKey(docId));
		//}
		
		Document doc = getDocument(docKey);
		if (doc != null) {
			if (clnId > 0 && !doc.hasCollection(clnId)) {
				return null;
			}
			if (doc.getTxFinish() > TX_NO && txManager.isTxVisible(doc.getTxFinish())) {
				return null;
			}
			if (txManager.isTxVisible(doc.getTxStart())) {
				return doc.getUri();
			}
		}
		return null;
	}

	int indexElements(int docType, int pathId) throws BagriException {
		Set<DocumentKey> docKeys = getDocumentsOfType(docType);
		String path = model.getPath(pathId).getPath();
		int cnt = 0;
		for (DocumentKey docKey: docKeys) {
			DataKey xdk = factory.newDataKey(docKey.getKey(), pathId);
			Elements elts = xdmCache.get(xdk);
			if (elts != null) {
				for (Element elt: elts.getElements()) {
					indexManager.addIndex(docKey.getKey(), pathId, path, elt.getValue());
					cnt++;
				}
			}
		}
		return cnt;
	}

	int deindexElements(int docType, int pathId) {
		Set<DocumentKey> docKeys = getDocumentsOfType(docType);
		int cnt = 0;
		for (DocumentKey docKey: docKeys) {
			DataKey xdk = factory.newDataKey(docKey.getKey(), pathId);
			Elements elts = xdmCache.get(xdk);
			if (elts != null) {
				for (Element elt: elts.getElements()) {
					indexManager.removeIndex(docKey.getKey(), pathId, elt.getValue());
					cnt++;
				}
			}
		}
		return cnt;
	}

	private int deindexElements(long docKey, int pathId) {
		int cnt = 0;
		DataKey xdk = factory.newDataKey(docKey, pathId);
		Elements elts = xdmCache.get(xdk);
		if (elts != null) {
			for (Element elt: elts.getElements()) {
				indexManager.removeIndex(docKey, pathId, elt.getValue());
				cnt++;
			}
		}
		logger.trace("deindexElements.exit; deindexed elements: {} for docKey: {}, pathId: {}", cnt, docKey, pathId);
		return cnt;
	}
	
	@SuppressWarnings("unchecked")
	private Set<DocumentKey> getDocumentsOfType(int docType) {
   		Predicate<DocumentKey, Document> f = Predicates.and(Predicates.equal("typeId", docType), 
   				Predicates.equal("txFinish", 0L));
		return xddCache.keySet(f);
	}
	
	public Document getDocument(long docKey) {
		Document doc = getDocument(factory.newDocumentKey(docKey)); 
		//logger.trace("getDocument; returning: {}", doc);
		return doc;
	}
	
	private Document getDocument(DocumentKey docKey) {
		return xddCache.get(docKey); 
	}

	@Override
	public Document getDocument(String uri) {
		Document doc = null;
		DocumentKey docKey = getDocumentKey(uri, false, false);
		if (docKey != null) {
			doc = getDocument(docKey);
		}
		//logger.trace("getDocument; returning: {}", doc);
		return doc;
	}

    private DocumentKey getDocumentKey(String uri, boolean next, boolean acceptClosed) {
    	Set<DocumentKey> keys = xddCache.localKeySet(Predicates.equal("uri", uri));
    	if (keys.isEmpty()) {
			DocumentKey key = factory.newDocumentKey(uri, 0, dvFirst);
    		if (next) {
    			// TODO: bad case: most of the time it'll hit database!
    			// try to use some internal service (PopulationManager?) instead!
    			while (xddCache.containsKey(key)) {
    				key = factory.newDocumentKey(uri, key.getRevision() + 1, dvFirst);
    			}
    			return key;
    		} 
    		
			if (hzInstance.getPartitionService().getPartition(key).getOwner().localMember()) {
	    		return null;
			} else {
				// think how to get it from concrete node?!
				keys = xddCache.keySet(Predicates.equal("uri", uri));
				if (keys.isEmpty()) {
		    		return null;
				}
			}
    	}
    	DocumentKey last = Collections.max(keys, new Comparator<DocumentKey>() {

			@Override
			public int compare(DocumentKey key1, DocumentKey key2) {
				return key1.getVersion() - key2.getVersion();
			}
    		
    	});
    	if (next) {
    		return factory.newDocumentKey(uri, last.getRevision(), last.getVersion() + 1);
    	}
    	if (acceptClosed) {
    		return last;
    	}
    	Document lastDoc = xddCache.get(last);
    	try {
    		if (lastDoc.getTxFinish() == TX_NO || !txManager.isTxVisible(lastDoc.getTxFinish())) {
    			return last;
    		}
    		// shouldn't we return previous version otherwise?
    	} catch (BagriException ex) {
    		logger.error("getDocumentKey.error", ex);
    		// ??
    	}
    	logger.info("getDocumentKey; the latest document version is finished already: {}", lastDoc);
    	return null;
    }
 	
	@Override
	public java.util.Collection<String> getDocumentUris(String pattern) {
		// implement other search types: by dates, owner, etc..
		logger.trace("getDocumentUris.enter; got pattern: {}", pattern);
		java.util.Collection<Document> docs;
		if (pattern != null) {
			Predicate<DocumentKey, Document> query = DocumentPredicateBuilder.getQuery(pattern);
	   		//Predicate<XDMDocumentKey, XDMDocument> f = Predicates.and(Predicates.regex("uri", pattern), 
	   		//		Predicates.equal("txFinish", TX_NO));
			docs = xddCache.values(query);
		} else {
			// TODO: possible OOM here!! then use PagingPredicate somehow!
			docs = xddCache.values();
		}

		// should also check if doc's start transaction is committed?
		Set<String> result = new HashSet<>(docs.size());
		for (Document doc: docs) {
			result.add(doc.getUri());
		}
		logger.trace("getDocumentUris.exit; returning: {}", result.size());
		return result;
	}

	@Override
	public java.util.Collection<String> buildDocument(Set<Long> docKeys, String template, Map<String, Object> params) throws BagriException {
        logger.trace("buildDocument.enter; docKeys: {}", docKeys.size());
		long stamp = System.currentTimeMillis();
        java.util.Collection<String> result = new ArrayList<String>(docKeys.size());
		
        int typeId = -1;
        String root = null;
		for (Iterator<Long> itr = docKeys.iterator(); itr.hasNext(); ) {
			DocumentKey docKey = factory.newDocumentKey(itr.next());
			if (hzInstance.getPartitionService().getPartition(docKey).getOwner().localMember()) {
				Document doc = xddCache.get(docKey);
				if (doc == null) {
					logger.info("buildDocument; lost document for key {}", docKey);
					continue;
				}

				StringBuilder buff = new StringBuilder(template);
				for (Map.Entry<String, Object> param: params.entrySet()) {
					String key = param.getKey();
					String path = param.getValue().toString();
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
    
	private String buildElement(String path, long[] fragments, int docType) throws BagriException {
        logger.trace("buildElement.enter; got path: {}; docType: {}", path, docType); 
    	Set<DataKey> xdKeys = getDocumentElementKeys(path, fragments, docType);
    	String dataFormat = df_xml;
    	String content = repo.getBuilder(dataFormat).buildString(xdmCache.getAll(xdKeys));
        logger.trace("buildXml.exit; returning xml length: {}", content.length()); 
       	return content;
    }
    
	@Override
	public Object getDocumentAsBean(String uri, Properties props) throws BagriException {
		String xml = getDocumentAsString(uri, props);
		if (xml == null) {
			return null;
		}
		return beanFromXML(xml);
	}

	@Override
	public Map<String, Object> getDocumentAsMap(String uri, Properties props) throws BagriException {
		DocumentKey docKey = getDocumentKey(uri, false, false);
		return getDocumentAsMap(docKey, props);
	}

	@Override
	public Map<String, Object> getDocumentAsMap(DocumentKey docKey, Properties props) throws BagriException {
		String xml = getDocumentAsString(docKey, props);
		if (xml == null) {
			return null;
		}
		return mapFromXML(xml);
	}

	public InputStream getDocumentAsStream(long docKey, Properties props) throws BagriException {
		String content = getDocumentAsString(docKey, props);
		if (content != null) {
			try {
				return new ByteArrayInputStream(content.getBytes(def_encoding));
			} catch (UnsupportedEncodingException ex) {
				throw new BagriException(ex, BagriException.ecInOut);
			}
		}
		return null;
	}
	
	@Override
	public String getDocumentAsString(String uri, Properties props) throws BagriException {
		DocumentKey docKey = getDocumentKey(uri, false, false);
		if (docKey == null) {
			//throw new XDMException("No document found for document Id: " + docId, XDMException.ecDocument);
			logger.info("getDocumentAsString; can not find active document for uri: {}", uri);
			return null;
		}
		return getDocumentAsString(docKey, props);
	}

	@Override
	public String getDocumentAsString(long docKey, Properties props) throws BagriException {
		DocumentKey xdmKey = factory.newDocumentKey(docKey);
		return getDocumentAsString(xdmKey, props);
	}
	
	@Override
	public String getDocumentAsString(DocumentKey docKey, Properties props) throws BagriException {
		
		String content = cntCache.get(docKey);
		if (content == null) {
			Document doc = getDocument(docKey);
			if (doc == null) {
				logger.info("getDocumentAsString; no document found for key: {}", docKey);
				return null;
			}
			// TODO: check Properties for document content production!
			// get Builder type from props, for instance..
			
			// if docId is not local then buildDocument returns null!
			// query docId owner node for the XML instead
			if (hzInstance.getPartitionService().getPartition(docKey).getOwner().localMember()) {
				String root = model.getDocumentRoot(doc.getTypeId());
				Map<String, Object> params = new HashMap<>();
				params.put(":doc", root);
				java.util.Collection<String> results = buildDocument(Collections.singleton(docKey.getKey()), ":doc", params);
				if (!results.isEmpty()) {
					content = results.iterator().next();
					cntCache.set(docKey, content);
				}
			} else {
				DocumentContentProvider xp = new DocumentContentProvider(repo.getClientId(), doc.getUri(), props); 
				IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
				Future<String> future = execService.submitToKeyOwner(xp, doc.getUri());
				try {
					content = future.get();
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("getDocumentAsString; error getting result", ex);
					throw new BagriException(ex, BagriException.ecDocument);
				}
			}
		}
		return content;
	}

	//@Override
	//public Source getDocumentAsSource(String uri) {
	//	return srcCache.get(factory.newXDMDocumentKey(docId.getDocumentKey()));
	//}

	@Override
	public String getDocumentContentType(long docKey) throws BagriException {
		// TODO: get it from the document itself?
		String def = repo.getSchema().getProperty(pn_schema_format_default);
		DataFormat df = repo.getDataFormat(def);
		if (df == null) {
			return mt_xml;
		}
		return df.getType();
	}
	
	private Collection getTypedCollection(Schema schema, String typePath) {
		for (Collection collect: schema.getCollections()) {
			String cPath = collect.getDocumentType();
			// TODO: very inefficient to normalize collections over and over again!
			cPath = model.normalizePath(cPath);
			if (cPath != null && typePath.equals(cPath)) {
				return collect;
			}
		}
		return null;
	}
	
	public String checkDefaultDocumentCollection(Document doc) {
		String typePath = model.getDocumentRoot(doc.getTypeId());
		Collection cln = getTypedCollection(repo.getSchema(), typePath);
		logger.trace("checkDefaultDocumentCollection; got collection: {} for typePath: {}", cln, typePath);
		if (cln != null) {
			doc.addCollection(cln.getId());
			return cln.getName();
		}
		return null;
	}
	
	private Document createDocument(DocumentKey docKey, String uri, String content, Properties props) throws BagriException {
		logger.trace("createDocument.enter; uri: {}; props: {}", uri, props);
		String dataFormat = null;
		int[] collections = null; 
		if (props != null) {
			dataFormat = props.getProperty(pn_document_data_format); 
			String prop = props.getProperty(pn_document_collections);
			if (prop != null) {
				StringTokenizer tc = new StringTokenizer(prop, ", ", false);
				collections = new int[tc.countTokens()];
				int idx = 0;
				while (tc.hasMoreTokens()) {
					String clName = tc.nextToken();
					Collection cln = repo.getSchema().getCollection(clName);
					if (cln != null) {
						collections[idx] = cln.getId();
					}
					idx++;
				}
			}
		}
		if (dataFormat == null) {
			dataFormat = uri.substring(uri.lastIndexOf(".") + 1);
		}

		Document doc = createDocument(docKey, uri, content, dataFormat, new Date(), repo.getUserName(), txManager.getCurrentTxId(), collections, false);
		
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

	@Override
	@SuppressWarnings("unchecked")
	public Document createDocument(DocumentKey docKey, String uri, String content, String dataFormat, Date createdAt, String createdBy, 
			long txStart, int[] collections, boolean addContent) throws BagriException {
		
		List<Data> data;
		ContentParser parser = repo.getParser(dataFormat);
		try {
			data = parser.parse(content);
		} catch (BagriException ex) {
			logger.info("createDocument; parse error. content: {}", content);
			throw ex;
		}

		Object[] ids = loadElements(docKey.getKey(), data);
		List<Long> fragments = (List<Long>) ids[0];
		if (fragments == null) {
			logger.warn("createDocument.exit; the document is not valid as it has no root element");
			throw new BagriException("invalid document", BagriException.ecDocument);
		} 
		int docType = fragments.get(0).intValue();
		Document doc;
		if (fragments.size() == 1) {
			doc = new Document(docKey.getKey(), uri, docType, txStart, TX_NO, createdAt, createdBy, def_encoding, content.length(), data.size());
		} else {
			doc = new FragmentedDocument(docKey.getKey(), uri, docType, txStart, TX_NO, createdAt, createdBy, def_encoding, content.length(), data.size());
			long[] fa = new long[fragments.size()];
			fa[0] = docKey.getKey();
			for (int i=1; i < fragments.size(); i++) {
				fa[i] = fragments.get(i);
			}
			((FragmentedDocument) doc).setFragments(fa);
		}

		List<String> clns = new ArrayList<>();
		if (collections != null && collections.length > 0) {
			doc.setCollections(collections);
			for (Collection cln: repo.getSchema().getCollections()) {
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
	
	private Object[] loadElements(long docKey, List<Data> data) throws BagriException {
		
		long stamp = System.currentTimeMillis();
		Data root = getDataRoot(data);
		if (root != null) {
			int docType = model.translateDocumentType(root.getPath());
			// normalize it ASAP !?
			//model.normalizeDocumentType(docType);
			Map<DataKey, Elements> elements = new HashMap<DataKey, Elements>(data.size());
			
			Set<Integer> fragments = new HashSet<>();
			for (Fragment fragment: repo.getSchema().getFragments()) {
				int fType = model.getDocumentType(fragment.getDocumentType());
				if (fType == docType) {
					Path path = model.getPath(fragment.getPath());
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
			for (Data xdm: data) {
				if (fragments.contains(xdm.getPathId())) {
					// TODO: why don't we shift it?
					//XDMDocumentKey kk = factory.newXDMDocumentKey(docGen.next(), 0);
					//fraPath = kk.getKey();
					int hash = docGen.next().intValue(); 
					fraPath = DocumentKey.toKey(hash, 0, 0);
					fragIds.add(fraPath);
					//fraPost = xdm.getPostId();
					fraPost = model.getPath(xdm.getPath()).getPostId();
				} else if (fraPost > 0 && xdm.getPathId() > fraPost) {
					fraPath = docKey;
					fraPost = 0;
				}
				pathIds.add(xdm.getPathId());
				DataKey xdk = factory.newDataKey(fraPath, xdm.getPathId());
				//logger.info("loadElements; got key: {}; fraPath: {}; fraPost: {}, partition: {}", 
				//		xdk, fraPath, fraPost, hzInstance.getPartitionService().getPartition(xdk));
				Elements xdes = elements.get(xdk);
				if (xdes == null) {
					xdes = new Elements(xdk.getPathId(), null);
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
	public Document storeDocumentFromBean(String uri, Object bean, Properties props) throws BagriException {
		String xml = beanToXML(bean);
		if (xml == null || xml.trim().length() == 0) {
			throw new BagriException("Can not convert bean [" + bean + "] to XML", BagriException.ecDocument);
		}
		logger.trace("storeDocumentFromBean; converted bean: {}", xml); 
			
		if (props != null) {
			props.setProperty(pn_document_data_format, df_xml);
		}
		return storeDocumentFromString(uri, xml, props);
	}

	@Override
	public Document storeDocumentFromMap(String uri, Map<String, Object> fields, Properties props) throws BagriException {
		String xml = mapToXML(fields);
		if (xml == null || xml.trim().length() == 0) {
			throw new BagriException("Can not convert map [" + fields + "] to XML", BagriException.ecDocument);
		}
		logger.trace("storeDocumentFromMap; converted map: {}", xml); 
		
		if (props != null) {
			props.setProperty(pn_document_data_format, df_xml);
		}
		return storeDocumentFromString(uri, xml, props);
	}
	
	//@Override
	//public XDMDocument storeDocumentFromSource(String uri, Source source, Properties props) {
	//	srcCache.put(factory.newXDMDocumentKey(docId.getDocumentKey()), source);
	//	return xddCache.get(docId);
	//}
	
	@Override
	public Document storeDocumentFromString(String uri, String content, Properties props) throws BagriException {
		logger.trace("storeDocumentFromString.enter; uri: {}; content: {}; props: {}", uri, content.length(), props);
		if (uri == null) {
			throw new BagriException("Empty URI passed", BagriException.ecDocument); 
		}

		boolean update = false;
		String storeMode = PropUtils.getProperty(props, pn_client_storeMode, pv_client_storeMode_merge);
		
		DocumentKey docKey = getDocumentKey(uri, false, true);
		if (docKey == null) {
			if (pv_client_storeMode_update.equals(storeMode)) {
				throw new BagriException("No document found for update. " +  uri, BagriException.ecDocument); 
			}
			docKey = factory.newDocumentKey(uri, 0, dvFirst);
		} else {
			if (pv_client_storeMode_insert.equals(storeMode)) {
				throw new BagriException("Document with URI '" + uri + "' already exists; docKey: " + docKey, 
						BagriException.ecDocument); 
			}
		    Document doc = getDocument(docKey);
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
				DocumentKey newKey = docKey;
				if (update) {
				    Document doc = getDocument(newKey);
				    if (doc != null) {
				    	if (doc.getTxFinish() > TX_NO && txManager.isTxVisible(doc.getTxFinish())) {
				    		throw new BagriException("Document with key: " + doc.getDocumentKey() + 
				    				", version: " + doc.getVersion() + " has been concurrently updated", 
				    				BagriException.ecDocument);
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
				Document result = createDocument(newKey, uri, content, props);
				if (update) {
					txManager.updateCounters(0, 1, 0);
				} else {
					txManager.updateCounters(1, 0, 0);
				}
			    return result;
			} catch (BagriException ex) {
				throw ex;
			} catch (Exception ex) {
				logger.error("storeDocumentFromString.error; uri: " + uri, ex);
				throw new BagriException(ex, BagriException.ecDocument);
			} finally {
				unlockDocument(docKey);
			}
		} else {
    		throw new BagriException("Was not able to aquire lock while storing Document: " + docKey + 
    				", timeout: " + timeout, BagriException.ecTransTimeout);
		}
	}

	@Override
	public void removeDocument(String uri) throws BagriException {
		logger.trace("removeDocument.enter; uri: {}", uri);
		//XDMDocumentKey docKey = getDocumentKey(docId);
	    //if (docKey == null) {
    	//	throw new XDMException("No document found for document Id: " + docId, XDMException.ecDocument);
	    //}
		if (uri == null) {
			throw new BagriException("No Document URI passed", BagriException.ecDocument); 
		}
		
		DocumentKey docKey = getDocumentKey(uri, false, false);
		if (docKey == null) {
			logger.info("removeDocument; no active document found for uri: {}", uri);
			return;
		}
		
	    boolean removed = false;
		boolean locked = lockDocument(docKey, txManager.getTransactionTimeout());
		if (locked) {
			try {
			    Document doc = getDocument(docKey);
			    if (doc != null && (doc.getTxFinish() == TX_NO || !txManager.isTxVisible(doc.getTxFinish()))) {
					triggerManager.applyTrigger(doc, Order.before, Scope.delete); 
			    	doc.finishDocument(txManager.getCurrentTxId()); 
			    	xddCache.set(docKey, doc);
			    	((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(doc.getDocumentKey());
			    	triggerManager.applyTrigger(doc, Order.after, Scope.delete); 
			    	txManager.updateCounters(0, 0, 1);
				    removed = true;
			    }
			} catch (BagriException ex) {
				throw ex;
			} catch (Exception ex) {
				logger.error("removeDocument.error; uri: " + uri, ex);
				throw new BagriException(ex, BagriException.ecDocument);
			} finally {
				unlockDocument(docKey);
			}
		} else {
    		throw new BagriException("Was not able to aquire lock while removing Document: " + docKey + 
    				", timeout: " + txManager.getTransactionTimeout(), BagriException.ecTransTimeout);
		}
		logger.trace("removeDocument.exit; removed: {}", removed);
	}
	
	public void cleanDocument(DocumentKey docKey, boolean complete) {
		logger.trace("cleanDocument.enter; docKey: {}, complete: {}", docKey, complete);
	    Document doc = getDocument(docKey);
	    boolean cleaned = false;
	    if (doc != null) {
			cntCache.delete(docKey);
			//srcCache.remove(docKey);
	    	int size = deleteDocumentElements(doc.getFragments(), doc.getTypeId());
	    	java.util.Collection<Integer> pathIds = indexManager.getTypeIndexes(doc.getTypeId(), true);
	    	for (int pathId: pathIds) {
	    		deindexElements(docKey.getKey(), pathId);
	    	}
	    	if (complete) {
	    		xddCache.delete(docKey);
	    	}
	    	cleaned = true;
	    	
			// update statistics
			for (Collection cln: repo.getSchema().getCollections()) {
				if (doc.hasCollection(cln.getId())) { 
					updateStats(cln.getName(), false, size, doc.getFragments().length);
				}
			}
			updateStats(null, false, size, doc.getFragments().length);
	    }
    	((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(docKey.getKey());
		logger.trace("cleanDocument.exit; cleaned: {}", cleaned);
	}

	public void evictDocument(DocumentKey xdmKey, Document xdmDoc) {
		logger.trace("evictDocument.enter; xdmKey: {}, xdmDoc: {}", xdmKey, xdmDoc);
		cntCache.delete(xdmKey);
		//srcCache.remove(xdmKey);
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
    	java.util.Collection<Path> allPaths = model.getTypePaths(typeId);
		logger.trace("deleteDocumentElements; got {} possible paths to remove; xdmCache size: {}", 
				allPaths.size(), xdmCache.size());
		int iCnt = 0;
		for (long docId: fragments) {
	        for (Path path: allPaths) {
	        	int pathId = path.getPathId();
	        	DataKey dKey = factory.newDataKey(docId, pathId);
	        	if (indexManager.isPathIndexed(pathId)) {
		       		Elements elts = xdmCache.remove(dKey);
		       		if (elts != null) {
		       			for (Element elt: elts.getElements()) {
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

	public void rollbackDocument(DocumentKey docKey) {
		logger.trace("rollbackDocument.enter; docKey: {}", docKey);
		boolean rolled = false;
	    Document doc = getDocument(docKey);
	    if (doc != null) {
	    	doc.finishDocument(TX_NO);
	    	xddCache.set(docKey, doc);
	    	rolled = true;
	    }
		logger.trace("rollbackDocument.exit; rolled back: {}", rolled);
	}
	
	@Override
	public java.util.Collection<String> getCollections() throws BagriException {
		List<String> clNames = new ArrayList<>(repo.getSchema().getCollections().size());
		for (Collection cln: repo.getSchema().getCollections()) {
			clNames.add(cln.getName());
		}
		return clNames;
	}

	@Override
	public java.util.Collection<String> getCollectionDocumentUris(String collection) throws BagriException {
		Set<DocumentKey> docKeys;
		if (collection == null) {
			docKeys = xddCache.localKeySet();
		} else {
			Collection cln = repo.getSchema().getCollection(collection);
			if (cln == null) {
				return null;
			}
			//int size = xddCache.size();
			Predicate<DocumentKey, Document> clp = new CollectionPredicate(cln.getId());
			docKeys = xddCache.localKeySet(clp);
			// TODO: investigate it; the localKeySet returns extra empty key for some reason!
		}
		// TODO: use props to fetch docs in batches. otherwise we can get OOM here!
		Map<DocumentKey, Document> docs = xddCache.getAll(docKeys);
		Set<String> result = new HashSet<>(docs.size());
		for (Document doc: docs.values()) {
		    if (doc.getTxFinish() == TX_NO || !txManager.isTxVisible(doc.getTxFinish())) {
				// check doc visibility via
				result.add(doc.getUri());
			}
		}
		return result;
	}

	Set<Long> getCollectionDocumentKeys(int collectId) {
		//
		Set<DocumentKey> docKeys;
		if (collectId == clnDefault) {
			// TODO: local or global keySet ?!
			docKeys = xddCache.keySet();
		} else {
			Predicate<DocumentKey, Document> clp = new CollectionPredicate(collectId);
			// TODO: local or global keySet ?!
			docKeys = xddCache.keySet(clp);
		}
		Set<Long> result = new HashSet<>(docKeys.size());
		for (DocumentKey key: docKeys) {
			result.add(key.getKey());
		}
		return result;
	}
	
	@Override
	public int removeCollectionDocuments(String collection) throws BagriException {
		logger.trace("removeCollectionDocuments.enter; collection: {}", collection);
		int cnt = 0;
		// remove local documents only?!
		java.util.Collection<String> uris = getCollectionDocumentUris(collection);
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
		Document doc = getDocument(uri);
		if (doc != null) {
			// TODO: cache size in the doc itself? yes, done
			// but must fix stats to account this size 
			int size = 0;
			for (Collection cln: repo.getSchema().getCollections()) {
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
				xddCache.set(factory.newDocumentKey(doc.getDocumentKey()), doc);
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
		Document doc = getDocument(uri);
		if (doc != null) {
			int size = 0;
			for (Collection cln: repo.getSchema().getCollections()) {
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
				xddCache.set(factory.newDocumentKey(doc.getDocumentKey()), doc);
			}
		} else {
			unkCount++;
		}
		logger.trace("removeDocumentsFromCollections.exit; removed: {}; unknown: {}", remCount, unkCount);
		return remCount;
	}
	
	private boolean lockDocument(DocumentKey docKey, long timeout) { //throws XDMException {
		
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

	private void unlockDocument(DocumentKey docKey) {

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
