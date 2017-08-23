package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.api.BagriException.ecDocument;
import static com.bagri.core.api.TransactionManagement.TX_NO;
import static com.bagri.core.model.Document.clnDefault;
import static com.bagri.core.model.Document.dvFirst;
import static com.bagri.core.query.PathBuilder.*;
import static com.bagri.core.system.DataFormat.df_xml;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.support.util.FileUtils.def_encoding;
import static com.bagri.support.util.XMLUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.bagri.client.hazelcast.impl.FixedCollectionImpl;
import com.bagri.client.hazelcast.impl.QueuedCollectionImpl;
import com.bagri.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.core.DataKey;
import com.bagri.core.DocumentKey;
import com.bagri.core.KeyFactory;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.ResultCollection;
import com.bagri.core.model.Data;
import com.bagri.core.model.Document;
import com.bagri.core.model.Element;
import com.bagri.core.model.Elements;
import com.bagri.core.model.FragmentedDocument;
import com.bagri.core.model.Null;
import com.bagri.core.model.Path;
import com.bagri.core.model.QueryResult;
import com.bagri.core.model.Transaction;
import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentConverter;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.DocumentManagement;
import com.bagri.core.server.api.ParseResults;
import com.bagri.core.server.api.impl.DocumentManagementBase;
import com.bagri.core.system.Collection;
import com.bagri.core.system.DataFormat;
import com.bagri.core.system.Fragment;
import com.bagri.core.system.Schema;
import com.bagri.core.system.TriggerAction.Order;
import com.bagri.core.system.TriggerAction.Scope;
import com.bagri.server.hazelcast.predicate.CollectionPredicate;
import com.bagri.server.hazelcast.predicate.DocVisiblePredicate;
import com.bagri.server.hazelcast.predicate.DocumentPredicateBuilder;
import com.bagri.server.hazelcast.task.doc.DocumentProcessor;
import com.bagri.support.idgen.IdGenerator;
import com.bagri.support.stats.StatisticsEvent;
import com.bagri.support.util.CollectionUtils;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.PartitionPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentManagementImpl extends DocumentManagementBase implements DocumentManagement {
	
	private static final String fnUri = "uri";
	//private static final String fnTxStart = "txStart";
	private static final String fnTxFinish = "txFinish";
	private static final String fnRoot = "root";
	
	private KeyFactory factory;
	private SchemaRepositoryImpl repo;
    private HazelcastInstance hzInstance;
    private IndexManagementImpl indexManager;
    private TransactionManagementImpl txManager;
    private TriggerManagementImpl triggerManager;
	private DataDistributionService ddSvc;

    private IdGenerator<Long> docGen;
    private IMap<DocumentKey, Object> cntCache;
	private IMap<DocumentKey, Document> xddCache;
    private IMap<DataKey, Elements> xdmCache;

	private boolean binaryDocs;
	private boolean binaryElts;
	private boolean binaryContent;
    
    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;
	
	//private IExecutorService execSvc;
	private ExecutorService execSvc;
	//private Map<String, String> sharedMap;
	
    public void setRepository(SchemaRepositoryImpl repo) {
    	this.repo = repo;
    	this.factory = repo.getFactory();
    	//this.model = repo.getModelManagement();
    	this.txManager = (TransactionManagementImpl) repo.getTxManagement();
    	this.triggerManager = (TriggerManagementImpl) repo.getTriggerManagement();
    	binaryDocs = InMemoryFormat.BINARY == repo.getHzInstance().getConfig().getMapConfig(CN_XDM_DOCUMENT).getInMemoryFormat();
    	binaryElts = InMemoryFormat.BINARY == repo.getHzInstance().getConfig().getMapConfig(CN_XDM_ELEMENT).getInMemoryFormat();
    	binaryContent = InMemoryFormat.BINARY == repo.getHzInstance().getConfig().getMapConfig(CN_XDM_CONTENT).getInMemoryFormat();
    	
    	execSvc = Executors.newFixedThreadPool(32);
    	//execSvc = repo.getHzInstance().getExecutorService(PN_XDM_TRANS_POOL);
		//sharedMap = new HashMap<>(10);
		//for (int j=0; j < 10; j++) {
		//	sharedMap.put("field" + j, org.apache.commons.lang3.RandomStringUtils.random(100));
		//}
    }
    
    IMap<DocumentKey, Object> getContentCache() {
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
    
    public void setContentCache(IMap<DocumentKey, Object> cache) {
    	this.cntCache = cache;
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
    
    public void setDistrService(DataDistributionService ddSvc) {
    	this.ddSvc = ddSvc;
    }

    private Set<DataKey> getDocumentElementKeys(String path, long[] fragments) {
    	Set<Integer> parts = model.getPathElements(path);
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
    	// could be faster to do this via EP..
		Document doc = getDocument(uri);
		if (doc == null) {
			return null;
		}

		Set<DataKey> keys = getDocumentElementKeys(doc.getTypeRoot(), doc.getFragments());
		Map<DataKey, Elements> elements = xdmCache.getAll(keys);
		return elements.values();
    }
    
	public String checkDocumentCommited(long docKey, int clnId) throws BagriException {
		
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

	int indexElements(int pathId) throws BagriException {
		Path path = model.getPath(pathId);
		Set<DocumentKey> docKeys = getDocumentsOfType(path.getRoot());
		int cnt = 0;
		for (DocumentKey docKey: docKeys) {
			DataKey xdk = factory.newDataKey(docKey.getKey(), pathId);
			Elements elts = xdmCache.get(xdk);
			if (elts != null) {
				for (Element elt: elts.getElements()) {
					indexManager.addIndex(docKey.getKey(), pathId, path.getPath(), elt.getValue());
					cnt++;
				}
			}
		}
		return cnt;
	}

	int deindexElements(int pathId) {
		Path path = model.getPath(pathId);
		Set<DocumentKey> docKeys = getDocumentsOfType(path.getRoot());
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
	private Set<DocumentKey> getDocumentsOfType(String root) {
   		Predicate<DocumentKey, Document> f = Predicates.and(Predicates.equal(fnRoot, root), 
   				Predicates.equal(fnTxFinish, TX_NO));
		return xddCache.keySet(f);
	}
	
	public Document getDocument(long docKey) {
		Document doc = getDocument(factory.newDocumentKey(docKey)); 
		//logger.trace("getDocument; returning: {}", doc);
		return doc;
	}
	
	private Document getDocument(DocumentKey docKey) {
		//return xddCache.get(docKey);
		return (Document) ddSvc.getCachedObject(CN_XDM_DOCUMENT, docKey, binaryDocs);
	}

	@Override
	public Document getDocument(String uri) {
		Document doc = ddSvc.getLastDocumentForUri(uri);
   		if (doc != null) {
   			if (doc.getTxFinish() != TX_NO) { // || !txManager.isTxVisible(lastDoc.getTxFinish())) {
   				logger.debug("getDocument; the latest document version is finished already: {}", doc);
   				doc = null;
   			}
    	}
		return doc;
	}
	
	private Object getDocumentContent(DocumentKey docKey) {
		//Object content = cntCache.get(docKey);
		Object content = ddSvc.getCachedObject(CN_XDM_CONTENT, docKey, binaryContent);
		if (content == null) {
			// build it with builder!
		}
		return content; 
	}

	@Override
	public String getDocumentContentType(long docKey) throws BagriException {
		Document doc = getDocument(docKey);
		if (doc != null) {
			return doc.getContentType();
		}
		
		String def = repo.getSchema().getProperty(pn_schema_format_default);
		DataFormat df = repo.getDataFormat(def);
		if (df == null) {
			return mt_xml;
		}
		return df.getType();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public java.util.Collection<String> getDocumentUris(String pattern, Properties props) {
		logger.trace("getDocumentUris.enter; got pattern: {}; props: {}", pattern, props);
		Predicate<DocumentKey, Document> query;
		if (pattern != null) {
			query = DocumentPredicateBuilder.getQuery(pattern);
		} else {
			query = Predicates.equal(fnTxFinish, TX_NO);
		}
		
		if (props != null) {
			int pageSize = Integer.valueOf(props.getProperty(pn_client_fetchSize, "0"));
			if (pageSize > 0) {
				query = new PagingPredicate<>(query, pageSize);
				//query = Predicates.and(new PagingPredicate(pageSize), query);
			}
		} //else {
		//  Projection<Entry<DocumentKey, Document>, String> pro = Projections.singleAttribute(fnUri);
		//	uris = xddCache.project(pro, query);
		//}
		
		java.util.Collection<Document> docs = xddCache.values(query);
		java.util.Collection<String> uris = new ArrayList<>(docs.size());
		if (pattern.indexOf(fnTxFinish) < 0) {
			for (Document doc: docs) {
				if (doc.getTxFinish() == TX_NO) {
					uris.add(doc.getUri());
				}
			}
		} else {
			for (Document doc: docs) {
				uris.add(doc.getUri());
			}
		}
		
		// should also check if doc's start transaction is committed?
		logger.trace("getDocumentUris.exit; returning: {}", uris);
		return uris;
	}
	
	private void fetchDocuments(String pattern, int fetchSize, ResultCollection cln) {
		Predicate<DocumentKey, Document> query = DocumentPredicateBuilder.getQuery(pattern);
		//if (pattern.indexOf(fnTxFinish) < 0) {
		//	query = Predicates.and(query, Predicates.equal(fnTxFinish, TX_NO));
		//}
		
		//if (fetchSize > 0) {
		//	query = new PagingPredicate<>(query, fetchSize);
		//}
		
		java.util.Collection<DocumentKey> keys = ddSvc.getLastKeysForQuery(query, fetchSize);
		//java.util.Collection<DocumentKey> keys = xddCache.localKeySet(query);
		int cnt = 0;
		for (DocumentKey key: keys) {
			Object content = ddSvc.getCachedObject(CN_XDM_CONTENT, key, binaryContent);
			if (content != null) {
				cln.add(content);
				cnt++;
			}
		}
		logger.trace("fetchDocuments.exit; fetched {} docs", cnt);
	}

	@Override
	public Iterable<?> getDocuments(final String pattern, final Properties props) {
		logger.trace("getDocuments.enter; got pattern: {}; props: {}", pattern, props);
		final int fetchSize;
		boolean asynch = false;
		if (props != null) {
			fetchSize = Integer.valueOf(props.getProperty(pn_client_fetchSize, "0"));
			asynch = Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"));
		} else {
			fetchSize = 0;
		}

		final ResultCollection cln; 
		if (asynch) {
			String clientId = props.getProperty(pn_client_id);
			cln = new QueuedCollectionImpl(hzInstance, "client:" + clientId);
			execSvc.execute(new Runnable() {
				@Override
				public void run() {
					fetchDocuments(pattern, fetchSize, cln);
					cln.add(Null._null);
				}
			});
		} else {
			cln = new FixedCollectionImpl(fetchSize);
			fetchDocuments(pattern, fetchSize, cln);
		}
		 
		logger.trace("getDocuments.exit; returning: {}", cln);
		return cln;
	}

	java.util.Collection<String> buildContent(Set<Long> docKeys, String template, Map<String, Object> params, String dataFormat) throws BagriException {
		
        logger.trace("buildContent.enter; docKeys: {}", docKeys.size());
        ContentBuilder<?> builder = repo.getBuilder(dataFormat);
        if (builder == null) {
			logger.info("buildContent.exit; no Handler found for dataFormat {}", dataFormat);
        	return null;
        }
        
		long stamp = System.currentTimeMillis();
        java.util.Collection<String> result = new ArrayList<>(docKeys.size());
		
        
        String root = null;
		for (Iterator<Long> itr = docKeys.iterator(); itr.hasNext(); ) {
			DocumentKey docKey = factory.newDocumentKey(itr.next());
			if (ddSvc.isLocalKey(docKey)) {
				Document doc = xddCache.get(docKey);
				if (doc == null) {
					logger.info("buildContent; lost document for key {}", docKey);
					continue;
				}

				StringBuilder buff = new StringBuilder(template);
				for (Map.Entry<String, Object> param: params.entrySet()) {
					String key = param.getKey();
					String path = param.getValue().toString();
					Object content = null;
					if (path.equals(root)) {
						// TODO: get and convert to string?
						content = cntCache.get(docKey);
					}
					if (content == null) {
				        logger.trace("buildContent; no content found for doc key: {}", docKey);
						content = buildElement(path, doc.getFragments(), builder);
					}
					if (content != null) {
						String str = content.toString();
						int pos = 0;
						while (true) {
							int idx = buff.indexOf(key, pos);
							if (idx < 0) break;
							buff.replace(idx, idx + key.length(), str);
							pos = idx + str.length();
						}
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
    
	private Object buildElement(String path, long[] fragments, ContentBuilder<?> builder) throws BagriException {
    	Set<DataKey> xdKeys = getDocumentElementKeys(path, fragments);
    	return builder.buildContent(xdmCache.getAll(xdKeys));
    }
    
	@Override
	public <T> T getDocumentAs(String uri, Properties props) throws BagriException {
		Document doc = getDocument(uri);
		if (doc == null) {
			logger.info("getDocumentAs; no document found for uri: {}", uri);
			return null;
		}

		DocumentKey docKey = factory.newDocumentKey(doc.getDocumentKey());
		return getDocumentAs(docKey, doc, props);
	}

	@Override
	public <T> T getDocumentAs(long docKey, Properties props) throws BagriException {
		return getDocumentAs(factory.newDocumentKey(docKey), props);  
	}

	@Override
	public <T> T getDocumentAs(DocumentKey docKey, Properties props) throws BagriException {
		Document doc = getDocument(docKey);
		if (doc == null) {
			logger.info("getDocumentAs; no document found for key: {}", docKey);
			return null;
		}

		return getDocumentAs(docKey, doc, props);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getDocumentAs(DocumentKey docKey, Document doc, Properties props) throws BagriException {
		String dataFormat = null;
		if (props != null) {
			dataFormat = props.getProperty(pn_document_data_format);
		} else {
			props = new Properties();
		}
		if (dataFormat == null) {
			dataFormat = repo.getSchema().getProperty(pn_schema_format_default);
		}
		if (!props.containsKey(pn_document_data_format)) {
			props.setProperty(pn_document_data_format, dataFormat);
		}
		
		ContentConverter<Object, T> cc = null;
		String srcFormat = doc.getContentType();
		if (!srcFormat.equals(dataFormat)) {
			Class<?> to = null;
			if (pv_document_data_source_map.equals(dataFormat)) {
				to = Map.class;
			} // else it'll return common Bean converter
			cc = repo.getConverter(srcFormat, to);
			if (cc == null) {
				throw new BagriException("No converter found between " + srcFormat + " and " + dataFormat, BagriException.ecDocument);
			}
		}
		
		Object content = getDocumentContent(docKey);
		logger.trace("getDocumentAs; got content: {}", content); 
		if (content == null) {
			// build it and store in cache
			// if docId is not local then buildDocument returns null!
			// query docId owner node for the XML instead
			if (ddSvc.isLocalKey(docKey)) {
				Map<String, Object> params = new HashMap<>();
				params.put(":doc", doc.getTypeRoot());
				java.util.Collection<String> results = buildContent(Collections.singleton(docKey.getKey()), ":doc", params, dataFormat);
				if (results.isEmpty()) {
					content = results.iterator().next();
					cntCache.set(docKey, content);
				}
			} else {
				// can cause distributed deadlock! call to EP from the same EP!
				DocumentContentProvider xp = new DocumentContentProvider(repo.getClientId(), txManager.getCurrentTxId(), doc.getUri(), props); 
				content = xddCache.executeOnKey(docKey, xp);
			}
		}
		if (cc != null) {
			return (T) cc.convertTo(content);
		}
		return (T) content;
	}

	//public InputStream getDocumentAsStream(long docKey, Properties props) throws BagriException {
	//	String content = getDocumentAsString(docKey, props);
	//	if (content != null) {
	//		try {
	//			return new ByteArrayInputStream(content.getBytes(def_encoding));
	//		} catch (UnsupportedEncodingException ex) {
	//			throw new BagriException(ex, BagriException.ecInOut);
	//		}
	//	}
	//	return null;
	//}
	
	private Collection getTypedCollection(Schema schema, String typePath) {
		for (Collection collect: schema.getCollections()) {
			String cPath = collect.getDocumentType();
			if (cPath != null && typePath.equals(cPath)) {
				return collect;
			}
		}
		return null;
	}
	
	public String checkDefaultDocumentCollection(Document doc) {
		Collection cln = getTypedCollection(repo.getSchema(), doc.getTypeRoot());
		logger.trace("checkDefaultDocumentCollection; got collection: {} for typePath: {}", cln, doc.getTypeRoot());
		if (cln != null) {
			doc.addCollection(cln.getId());
			return cln.getName();
		}
		return null;
	}
	
	private Document createDocument(DocumentKey docKey, String uri, Object content, Properties props) throws BagriException {
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

	//@Override
	@SuppressWarnings("unchecked")
	public Document createDocument(DocumentKey docKey, String uri, Object content, String dataFormat, Date createdAt, String createdBy, 
			long txStart, int[] collections, boolean addContent) throws BagriException {
		
		ParseResults pRes;
		dataFormat = repo.getHandler(dataFormat).getDataFormat();
		ContentParser<Object> parser = repo.getParser(dataFormat);
		try {
			pRes = parser.parse(content);
			// TODO: get length from parser
		} catch (BagriException ex) {
			logger.info("createDocument; parse error. content: {}", content);
			throw ex;
		}

		List<Data> data = pRes.getResults();
		int length = pRes.getContentLength();
		Object[] ids = loadElements(docKey.getKey(), data);
		List<Long> fragments = (List<Long>) ids[0];
		if (fragments == null) {
			logger.warn("createDocument.exit; the document is not valid as it has no root element");
			throw new BagriException("invalid document", BagriException.ecDocument);
		} 

		String root = data.get(0).getRoot();
		Document doc;
		if (fragments.size() == 0) {
			doc = new Document(docKey.getKey(), uri, root, txStart, TX_NO, createdAt, createdBy, dataFormat + "/" + def_encoding, length, data.size());
		} else {
			doc = new FragmentedDocument(docKey.getKey(), uri, root, txStart, TX_NO, createdAt, createdBy, dataFormat + "/" + def_encoding, length, data.size());
			long[] fa = new long[fragments.size()];
			fa[0] = docKey.getKey();
			for (int i=0; i < fragments.size(); i++) {
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
		Data dRoot = getDataRoot(data);
		if (dRoot != null) {
			String root = dRoot.getRoot();
			Map<DataKey, Elements> elements = new HashMap<DataKey, Elements>(data.size());
			
			Set<Integer> fragments = new HashSet<>();
			for (Fragment fragment: repo.getSchema().getFragments()) {
				if (fragment.getDocumentType().equals(root)) {
					Path path = model.getPath(root, fragment.getPath());
					if (path != null) {
						fragments.add(path.getPathId());
					} else if (isRegexPath(fragment.getPath())) {
						String nPath = fragment.getPath();
						fragments.addAll(model.translatePathFromRegex(root, regexFromPath(nPath)));
					} else {	
						logger.info("loadElements; path not found for fragment: {}; docType: {} ({})", 
								fragment, dRoot.getPath(), root);
					}
				}
			}
			logger.debug("loadElements; fragments found: {}; for docType: {} ({}); docKey: {}", 
					fragments, dRoot.getPath(), root, docKey);
			
			long fraPath = docKey;
			long fraPost = 0;
			int size = 1;
			if (fragments.size() > 0) {
				size = data.size() / fragments.size();
			}
			Set<Integer> pathIds = new HashSet<>(size);
			List<Long> fragIds = new ArrayList<>(size);
			for (Data xdm: data) {
				if (fragments.contains(xdm.getPathId())) {
					int hash = docGen.next().intValue(); 
					fraPath = DocumentKey.toKey(hash, 0, 0);
					fragIds.add(fraPath);
					//fraPost = xdm.getPostId();
					fraPost = model.getPath(root, xdm.getPath()).getPostId();
				} else if (fraPost > 0 && xdm.getPathId() > fraPost) {
					fraPath = docKey;
					fraPost = 0;
				}
				pathIds.add(xdm.getPathId());
				if (xdm.getValue() != null) {
					DataKey xdk = factory.newDataKey(fraPath, xdm.getPathId());
					Elements xdes = elements.get(xdk);
					if (xdes == null) {
						xdes = new Elements(xdk.getPathId(), null);
						elements.put(xdk, xdes);
					}
					xdes.addElement(xdm.getElement());
					indexManager.addIndex(docKey, xdm.getPathId(), xdm.getPath(), xdm.getValue());
				}
			}
			xdmCache.putAll(elements);
			
			stamp = System.currentTimeMillis() - stamp;
			logger.debug("loadElements; cached {} elements for docKey: {}; fragments: {}; time taken: {}", 
					elements.size(), docKey, fragIds.size(), stamp);
			Object[] result = new Object[2];
			result[0] = fragIds;
			result[1] = pathIds;
			return result;
		}
		return null;
	}
	
	public Document processDocument(Map.Entry<DocumentKey, Document> old, long txId, String uri, Object content, ParseResults pRes, Properties props) throws BagriException {
		
		logger.trace("processDocument.enter; uri: {}; results: {}; props: {}", uri, pRes, props);
		
		//boolean update = (old.getValue() != null); // && (doc.getTxFinish() == TX_NO || !txManager.isTxVisible(doc.getTxFinish())));
		DocumentKey docKey = old.getKey();
		long docId = docKey.getKey();
		List<Data> data = pRes.getResults();
		if (old.getValue() != null) {
	    	logger.trace("processDocument; going to update document: {}", old);
	    	Document updated = old.getValue();
	    	if (txId > TX_NO) {
		    	// we must finish old Document and create a new one!
				//triggerManager.applyTrigger(doc, Order.before, Scope.update);
		    	updated.finishDocument(txId);
			    old.setValue(updated);
			    docKey = factory.newDocumentKey(docKey.getKey(), docKey.getVersion() + 1); // docKey.getKey() + 1);
				docId = docKey.getKey();
	    	} else {
		    	// we do changes inplace, no new version created
	    		boolean mergeElts = Boolean.parseBoolean(props.getProperty(pn_document_map_merge, "false"));
	    		java.util.Collection<DataKey> dKeys = ddSvc.getElementKeys(docId);
		    	Set<Integer> pIds = new HashSet<>(data.size());
		    	for (Data dt: data) {
		    		pIds.add(dt.getPathId());
		    	}
		    	logger.trace("processDocument; found {} element keys for docId {}; paths: {}", dKeys.size(), docId, pIds.size());
	    		// delete old elements
		    	for (DataKey dKey: dKeys) {
	    			if (!mergeElts && !pIds.contains(dKey.getPathId())) {
	        			ddSvc.deleteCachedObject(CN_XDM_ELEMENT, dKey);
	    			}
	    		}
	    	}
		}

		int length = pRes.getContentLength(); 
		String root = data.get(0).getRoot();
		Set<Integer> ids = processElements(docId, data);
		String dataFormat = props.getProperty(pn_document_data_format, df_xml);
		Document newDoc = new Document(docId, uri, root, txId, TX_NO, new Date(), repo.getUserName(), dataFormat + "/" + def_encoding, length, data.size());

		String collections = props == null ? null : props.getProperty(pn_document_collections);
		if (collections != null) {
			StringTokenizer tc = new StringTokenizer(collections, ", ", false);
			while (tc.hasMoreTokens()) {
				String clName = tc.nextToken();
				Collection cln = repo.getSchema().getCollection(clName);
				if (cln != null) {
					newDoc.addCollection(cln.getId());
					updateStats(clName, true, data.size(), 0);
					//updateStats(clName, true, paths.size(), doc.getFragments().length);
				}
			}
		} else {
			String clName = checkDefaultDocumentCollection(newDoc);
			if (clName != null) {
				updateStats(clName, true, data.size(), 0);
			}
		}
		updateStats(null, true, data.size(), 0);
		
		if (old.getValue() == null || txId == TX_NO) {
	    	old.setValue(newDoc);
		} else {
			xddCache.set(docKey, newDoc);
			//ddSvc.storeData(docKey, newDoc, CN_XDM_DOCUMENT);
		}
		//ddSvc.storeData(docKey, content, CN_XDM_CONTENT);
		cntCache.set(docKey, content);
		
		logger.trace("processDocument.exit; returning: {}", newDoc);
		return newDoc;
	}
	
	private Set<Integer> processElements(long docKey, List<Data> data) throws BagriException {
		
		Data dRoot = getDataRoot(data);
		if (dRoot != null) {
			//String root = dRoot.getDataPath().getRoot();
			Map<DataKey, Elements> elements = new HashMap<DataKey, Elements>(data.size());
			Set<Integer> pathIds = new HashSet<>(data.size());
			for (Data xdm: data) {
				if (xdm.getValue() != null) {
					pathIds.add(xdm.getPathId());
					DataKey xdk = factory.newDataKey(docKey, xdm.getPathId());
					Elements xdes = elements.get(xdk);
					if (xdes == null) {
						xdes = new Elements(xdk.getPathId(), null);
						elements.put(xdk, xdes);
					}
					xdes.addElement(xdm.getElement());
				}
			}
			// TODO: do it directly via RecordStore
			//xdmCache.putAll(elements);
			for (Map.Entry<DataKey, Elements> e: elements.entrySet()) {
				xdmCache.set(e.getKey(), e.getValue());
				//ddSvc.storeData(e.getKey(), e.getValue(), CN_XDM_ELEMENT);
			}
			return pathIds;
		}
		throw new BagriException("invalid document: has no root element", ecDocument);
	}

	@Override
	public <T> Document storeDocumentFrom(String uri, T content, Properties props) throws BagriException {
		logger.trace("storeDocumentFrom; got uri: {}; content: {}; props: {}", uri, content, props); 
		String dataFormat = null;
		if (props != null) {
			dataFormat = props.getProperty(pn_document_data_format);
		} else {
			props = new Properties();
		}
		if (dataFormat == null) {
			dataFormat = repo.getSchema().getProperty(pn_schema_format_default);
		}
		if (!props.containsKey(pn_document_data_format)) {
			props.setProperty(pn_document_data_format, dataFormat);
		}
		String srcFormat = props.getProperty(pn_document_data_source, dataFormat);
		if (!srcFormat.equals(dataFormat)) {
			ContentConverter<Object, T> cc = repo.getConverter(dataFormat, content.getClass());
			if (cc == null) {
				throw new BagriException("No converter found between " + srcFormat + " and " + dataFormat, BagriException.ecDocument);
			}
			Object converted = cc.convertFrom(content);
			logger.trace("storeDocumentFrom; converted content: {}", converted); 
			return storeDocument(uri, converted, props);
		}
		return storeDocument(uri, content, props);
	}

	private Document storeDocument(String uri, Object content, Properties props) throws BagriException {
		logger.trace("storeDocument.enter; uri: {}; content: {}; props: {}", uri, content.getClass().getName(), props);
		if (uri == null) {
			throw new BagriException("Empty URI passed", ecDocument); 
		}
		
		DocumentKey docKey = ddSvc.getLastKeyForUri(uri);
		String storeMode = props.getProperty(pn_client_storeMode, pv_client_storeMode_merge); 
		if (docKey == null) {
			if (pv_client_storeMode_update.equals(storeMode)) {
				throw new BagriException("No document with URI '" +  uri + "' found for update", ecDocument); 
			}
			docKey = factory.newDocumentKey(uri, 0, dvFirst);
		} else {
			if (pv_client_storeMode_insert.equals(storeMode)) {
				throw new BagriException("Document with URI '" + uri + "' already exists; docKey: " + docKey, ecDocument); 
			}
		    //Document doc = getDocument(docKey);
			//update = (doc != null && (doc.getTxFinish() == TX_NO || !txManager.isTxVisible(doc.getTxFinish())));
			//triggerManager.applyTrigger(doc, Order.before, Scope.update);
	    	// do this asynch after tx?
	    	//((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(docKey.getKey());

			//if (indexManager.isPathIndexed(pathId)) {
        	//	Elements elts;
        	//	if (mergeElts || oldPathId) {
        			//elts = xdmCache.get(dKey);
        	//		elts = ddSvc.getCachedObject(CN_XDM_ELEMENT, dKey, binaryElts);
        	//	} else {
	       	//		elts = ddSvc.removeCachedObject(CN_XDM_ELEMENT, dKey, binaryElts);
        	//	}
        		// can't do this from partition thread!
	       		//if (elts != null) {
	       		//	for (Element elt: elts.getElements()) {
	       		//		indexManager.removeIndex(docId, pathId, elt.getValue());
	       		//	}
	       		//}
			//}
		}

		String dataFormat = props.getProperty(pn_document_data_format);
		dataFormat = repo.getHandler(dataFormat).getDataFormat();
		ContentParser<Object> parser = repo.getParser(dataFormat);
		ParseResults pRes = parser.parse(content);
		// ??
		props.setProperty(pn_document_data_format, dataFormat);
		
		// if fragmented document - process it in the old style!
		
		Transaction tx = null;
    	String txLevel = props.getProperty(pn_client_txLevel);
    	if (!pv_client_txLevel_skip.equals(txLevel)) {
    		tx = txManager.getCurrentTransaction(); 
    	}
		
		Object result = xddCache.executeOnKey(docKey, new DocumentProcessor(tx, uri, content, pRes, props));
		if (result instanceof Exception) {
			logger.error("storeDocument.error; uri: {}", uri, result);
			if (result instanceof BagriException) {
				throw (BagriException) result;
			}
			throw new BagriException((Exception) result, ecDocument);
		}

		Scope scope;
		Document newDoc = (Document) result;
		if (newDoc.getVersion() > dvFirst) {
			scope = Scope.update;
			txManager.updateCounters(0, 1, 0);
		} else {
			scope = Scope.insert;
			txManager.updateCounters(1, 0, 0);
		}
		triggerManager.applyTrigger(newDoc, Order.after, scope);

    	java.util.Collection<Path> paths = model.getTypePaths(newDoc.getTypeRoot());
    	Set<Integer> pathIds = new HashSet<>(paths.size());
    	Map<DataKey, Elements> eMap = ddSvc.getElements(newDoc.getDocumentKey());
		for (Path path: paths) {
			DataKey dKey = factory.newDataKey(newDoc.getDocumentKey(), path.getPathId());
			Elements elts = eMap.get(dKey);
			if (elts != null) {
				for (Element elt: elts.getElements()) {
					indexManager.addIndex(newDoc.getDocumentKey(), path.getPathId(), path.getPath(), elt.getValue());
				}
				pathIds.add(path.getPathId());
			}
		}

		// invalidate cached query results.
		((QueryManagementImpl) repo.getQueryManagement()).invalidateQueryResults(pathIds);
		
		logger.trace("storeDocument.exit; returning: {}", newDoc);
		return newDoc;
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
		
		Document doc = getDocument(uri);
		if (doc == null) {
			logger.info("removeDocument; no active document found for uri: {}", uri);
			return;
		}
		
		DocumentKey docKey = factory.newDocumentKey(doc.getDocumentKey());
		boolean locked = lockDocument(docKey, txManager.getTransactionTimeout());
		if (locked) {
			try {
				triggerManager.applyTrigger(doc, Order.before, Scope.delete); 
		    	doc.finishDocument(txManager.getCurrentTxId()); 
		    	xddCache.set(docKey, doc);
		    	((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(doc.getDocumentKey());
		    	triggerManager.applyTrigger(doc, Order.after, Scope.delete); 
		    	txManager.updateCounters(0, 0, 1);
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
		logger.trace("removeDocument.exit; removed: {}", doc);
	}
	
	public void cleanDocument(DocumentKey docKey, boolean complete) {
		logger.trace("cleanDocument.enter; docKey: {}, complete: {}", docKey, complete);
	    Document doc = getDocument(docKey);
	    boolean cleaned = false;
	    if (doc != null) {
	    	// TODO: clean via EntryProcessor..
			cntCache.delete(docKey);
	    	int size = deleteDocumentElements(doc.getFragments(), doc.getTypeRoot());
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
    	int size = deleteDocumentElements(xdmDoc.getFragments(), xdmDoc.getTypeRoot());

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
	
	private int deleteDocumentElements(long[] fragments, String root) {

    	int cnt = 0;
    	java.util.Collection<Path> allPaths = model.getTypePaths(root);
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
	public java.util.Collection<String> getCollectionDocumentUris(String collection, Properties props) throws BagriException {
		int pageSize = 100;
		if (props != null) {
			pageSize = Integer.valueOf(props.getProperty(pn_client_fetchSize, "100"));
		} 
		PagingPredicate<DocumentKey, Document> pager = null;
		Predicate<DocumentKey, Document> query = new DocVisiblePredicate();
		((DocVisiblePredicate) query).setRepository(repo);
		if (collection == null) {
			if (pageSize > 0) {
				pager = new PagingPredicate<>(query, pageSize);
				query = pager;
			}
		} else {
			Collection cln = repo.getSchema().getCollection(collection);
			if (cln == null) {
				return null;
			}
			query = Predicates.and(query, new CollectionPredicate(cln.getId()));
			if (pageSize > 0) {
				pager = new PagingPredicate<>(query, pageSize);
				query = pager;
			}
		}
		
		List<String> result = new ArrayList<>(); 
		if (pager != null) {
			int size;
			do {
				size = result.size(); 
				fillUris(query, result);
				pager.nextPage();
			} while (result.size() > size);
		} else {
			fillUris(query, result);
		}
		
		// does not work because of a bug in HZ
		//Projection<Entry<DocumentKey, Document>, String> pro = Projections.singleAttribute(fnUri);
		//if (pager != null) {
		//	int size;
		//	do {
		//		size = result.size();  
		//		result.addAll(xddCache.project(pro, query));
		//		pager.nextPage();
		//	} while (result.size() > size);
		//} else {
		//	result.addAll(xddCache.project(pro, query));
		//}
		return result;
	}
	
	private void fillUris(Predicate<DocumentKey, Document> query, java.util.Collection<String> uris) throws BagriException {
		java.util.Collection<Document> docs = xddCache.values(query);
		for (Document doc: docs) {
	    	uris.add(doc.getUri());
		}
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
		// remove local documents only?! yes!
		java.util.Collection<String> uris = getCollectionDocumentUris(collection, null);
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
