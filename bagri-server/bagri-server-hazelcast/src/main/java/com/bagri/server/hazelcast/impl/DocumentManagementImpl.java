package com.bagri.server.hazelcast.impl;

import com.bagri.client.hazelcast.impl.BoundedCursorImpl;
import com.bagri.client.hazelcast.impl.FixedCursorImpl;
import com.bagri.client.hazelcast.impl.QueuedCursorImpl;
import com.bagri.client.hazelcast.task.doc.DocumentProvider;
import com.bagri.core.DataKey;
import com.bagri.core.DocumentKey;
import com.bagri.core.KeyFactory;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.impl.ResultCursorBase;
import com.bagri.core.model.Data;
import com.bagri.core.model.Document;
import com.bagri.core.model.Element;
import com.bagri.core.model.Elements;
import com.bagri.core.model.FragmentedDocument;
import com.bagri.core.model.ParseResults;
import com.bagri.core.model.Path;
import com.bagri.core.model.Transaction;
import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentConverter;
import com.bagri.core.server.api.ContentMerger;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.DocumentManagement;
import com.bagri.core.server.api.impl.DocumentManagementBase;
import com.bagri.core.system.Collection;
import com.bagri.core.system.DataFormat;
import com.bagri.core.system.Fragment;
import com.bagri.core.system.Schema;
import com.bagri.core.system.TriggerAction.Order;
import com.bagri.core.system.TriggerAction.Scope;
import com.bagri.server.hazelcast.impl.CompressingCursorImpl;
import com.bagri.server.hazelcast.predicate.CollectionPredicate;
import com.bagri.server.hazelcast.predicate.DocumentPredicateBuilder;
import com.bagri.server.hazelcast.task.doc.DocumentProcessor;
import com.bagri.server.hazelcast.task.doc.DocumentRemoveProcessor;
import com.bagri.support.idgen.IdGenerator;
import com.bagri.support.stats.StatisticsEvent;
import com.bagri.support.util.FileUtils;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.impl.MapEntrySimple;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bagri.core.Constants.*;
import static com.bagri.core.api.BagriException.*;
import static com.bagri.core.api.TransactionManagement.*;
import static com.bagri.core.model.Document.*;
import static com.bagri.core.query.PathBuilder.*;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.core.system.DataFormat.*;
import static com.bagri.support.util.FileUtils.*;

public class DocumentManagementImpl extends DocumentManagementBase implements DocumentManagement, EntryEvictedListener<DocumentKey, Document> {

	private static final transient Logger logger = LoggerFactory.getLogger(DocumentManagementImpl.class);
	
	//private static final String fnUri = "uri";
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
	private IMap<DocumentKey, Document> docCache;
    private IMap<DataKey, Elements> eltCache;
    //private IMap<UrlHashKey, List<DocumentKey>> keyCache;

	private boolean binaryDocs;
	private boolean binaryElts;
	private boolean binaryContent;
	
	private boolean cacheContent;
	private boolean cacheElements; 

    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;

	private ExecutorService execPool;

    public void setRepository(SchemaRepositoryImpl repo) {
    	this.repo = repo;
    	this.factory = repo.getFactory();
    	//this.model = repo.getModelManagement();
    	this.txManager = (TransactionManagementImpl) repo.getTxManagement();
    	this.triggerManager = (TriggerManagementImpl) repo.getTriggerManagement();
    	binaryDocs = InMemoryFormat.BINARY == repo.getHzInstance().getConfig().getMapConfig(CN_XDM_DOCUMENT).getInMemoryFormat();
    	binaryElts = InMemoryFormat.BINARY == repo.getHzInstance().getConfig().getMapConfig(CN_XDM_ELEMENT).getInMemoryFormat();
    	binaryContent = InMemoryFormat.BINARY == repo.getHzInstance().getConfig().getMapConfig(CN_XDM_CONTENT).getInMemoryFormat();
    	//keyCache = repo.getHzInstance().getMap(CN_XDM_KEY);
    }

    IMap<DocumentKey, Document> getDocumentCache() {
    	return docCache;
    }

    IMap<DataKey, Elements> getElementCache() {
    	return eltCache;
    }
    
    boolean isCacheContent() {
    	return cacheContent;
    }
    
    boolean isCacheElements() {
    	return cacheElements;
    }

    public void setCacheContent(boolean cacheContent) {
    	this.cacheContent = cacheContent;
    }

    public void setCacheElements(boolean cacheElements) {
    	this.cacheElements = cacheElements;
    }

    public void setDocumentIdGenerator(IdGenerator<Long> docGen) {
    	this.docGen = docGen;
    }

    public void setContentCache(IMap<DocumentKey, Object> cache) {
    	this.cntCache = cache;
    }

    public void setDocumentCache(IMap<DocumentKey, Document> cache) {
    	this.docCache = cache;
		docCache.addLocalEntryListener(this);
    }

    public void setElementCache(IMap<DataKey, Elements> cache) {
    	this.eltCache = cache;
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

    public void setExecPool(ExecutorService execSvc) {
    	this.execPool = execSvc;
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
		Map<DataKey, Elements> elements = eltCache.getAll(keys);
		return elements.values();
    }

	public String checkDocumentCommited(long docKey, int clnId) throws BagriException {

		Document doc = getDocument(docKey);
		if (doc != null) {
			if (clnId > 0 && !doc.hasCollection(clnId)) {
				return null;
			}
			if (!doc.isActive() && txManager.isTxVisible(doc.getTxFinish())) {
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
			Elements elts = eltCache.get(xdk);
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
			Elements elts = eltCache.get(xdk);
			if (elts != null) {
				for (Element elt: elts.getElements()) {
					indexManager.removeIndex(docKey.getKey(), pathId, elt.getValue());
					cnt++;
				}
			}
		}
		return cnt;
	}

	@SuppressWarnings("unchecked")
	private Set<DocumentKey> getDocumentsOfType(String root) {
   		Predicate<DocumentKey, Document> f = Predicates.and(Predicates.equal(fnRoot, root),
   				Predicates.equal(fnTxFinish, TX_NO));
		return docCache.keySet(f);
	}

	public Document getDocument(long docKey) {
		return getDocument(factory.newDocumentKey(docKey));
	}

	public Document getDocument(DocumentKey docKey) {
		return (Document) ddSvc.getCachedObject(CN_XDM_DOCUMENT, docKey, binaryDocs);
	}

	Document getDocument(String uri) {
		Document doc = ddSvc.getLastDocumentForUri(uri);
   		if (doc != null) {
   			if (!doc.isActive()) { // && txManager.isTxVisible(doc.getTxFinish())) {
   				logger.info("getDocument; the latest document version is finished already: {}", doc);
   				doc = null;
   			}
    	}
		return doc;
	}

	private Object getDocumentContent(DocumentKey docKey) {
		return ddSvc.getCachedObject(CN_XDM_CONTENT, docKey, binaryContent);
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
	public DocumentAccessor getDocument(String uri, Properties props) throws BagriException {
		Document doc = getDocument(uri);
		if (doc == null) {
			logger.info("getDocument; no document found for uri: {}", uri);
			return null;
		}

		DocumentKey docKey = factory.newDocumentKey(doc.getDocumentKey());
		return getDocumentInternal(docKey, doc, props);
	}

	@Override
	public DocumentAccessor getDocument(long docKey, Properties props) throws BagriException {
		return getDocument(factory.newDocumentKey(docKey), props);
	}

	@Override
	public DocumentAccessor getDocument(DocumentKey docKey, Properties props) throws BagriException {
		Document doc = getDocument(docKey);
		if (doc == null) {
			logger.info("getDocument; no document found for key: {}", docKey);
			return null;
		}

		return getDocumentInternal(docKey, doc, props);
	}

	private DocumentAccessor getDocumentInternal(DocumentKey docKey, Document doc, Properties props) throws BagriException {
		if (props == null) {
			props = new Properties();
		}
		String headers = props.getProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_URI_WITH_CONTENT)); //CLIENT_DOCUMENT
		long headMask = Long.parseLong(headers);
		logger.trace("getDocumentInternal; returning document: {} for props: {}", doc, props);
		if ((headMask & DocumentAccessor.HDR_CONTENT) != 0) {
			if (!ddSvc.isLocalKey(docKey)) {
				// shouldn't cause a deadlock as getDoc from client should be routed properly
				logger.trace("getDocumentInternal; docKey is not local, requesting owner node..");
				DocumentProvider xp = new DocumentProvider(repo.getClientId(), txManager.getCurrentTxId(), props, doc.getUri());
				return (DocumentAccessor) docCache.executeOnKey(docKey, xp);
			}
		
			// getConverter set dataFormat in props
			ContentConverter<Object, ?> cc = getConverter(props, doc.getContentType(), null);
			Object content = getDocumentContent(docKey);
			if (content == null) {
				logger.debug("getDocumentInternal; got no content for doc: {}", doc);
				if (cacheElements) { 
					// build content from elements
					String dataFormat = props.getProperty(pn_document_data_format);
				    ContentBuilder<?> builder = repo.getBuilder(dataFormat);
				    if (builder == null) {
						logger.info("buildContent.exit; no Handler found for dataFormat {}", dataFormat);
						// get builder for default format!
				        return null;
				    }
				    Set<DataKey> xdKeys = getDocumentElementKeys(doc.getTypeRoot(), doc.getFragments());
				    content = builder.buildContent(eltCache.getAll(xdKeys));
				} else {
					// get content from source
					// this is a rough huck, just to make it work
					String dataPath = repo.getSchema().getProperty(pn_schema_store_data_path);
					String fullUri = dataPath + "/" + doc.getUri();
					try {
						content = FileUtils.readTextFile(fullUri);
					} catch (IOException ex) {
						logger.info("getDocumentInternal; error reading content", ex);
					}
				}
				
				if (content != null && cacheContent) {
					// do this asynchronously!?
					cntCache.set(docKey, content);
				}
				logger.trace("getDocumentInternal; new content is: {}", content);
			}
			if (cc != null) {
				content = cc.convertTo(content);
				String dataFormat = props.getProperty(pn_document_data_format);
				return new DocumentAccessorImpl(repo, doc, headMask, dataFormat, content);
			}
			return new DocumentAccessorImpl(repo, doc, headMask, content);
		}
		return new DocumentAccessorImpl(repo, doc, headMask);
	}
	
	@SuppressWarnings("unchecked")
	private <T> ContentConverter<Object, T> getConverter(Properties props, String srcFormat, Class<T> contentType) throws BagriException {
		String dataFormat = props.getProperty(pn_document_data_format);
		if (dataFormat == null) {
			dataFormat = repo.getSchema().getProperty(pn_schema_format_default);
			props.setProperty(pn_document_data_format, dataFormat);
		}
		if (srcFormat == null) {
			srcFormat = dataFormat;
		}
		ContentConverter<Object, T> cc = null;
		if (!srcFormat.equals(dataFormat)) {
			Class<?> cType = null;
			if (contentType == null) {
				if (pv_document_data_source_map.equals(dataFormat)) {
					cType = Map.class; //ContentConverter.MapConverter.class;
				} else if (pv_document_data_source_json.equals(dataFormat)) {
					cType = ContentConverter.JsonConverter.class;
				} else if (pv_document_data_source_xml.equals(dataFormat)) {
					cType = ContentConverter.XmlConverter.class;
				} 
				// else repo will return common Bean converter
			} else {
				cType = contentType;
			}
			cc = repo.getConverter(srcFormat, cType); 
			if (cc == null) {
				logger.warn("getConverter; no converter found from {} to {}, content type: {}", srcFormat, dataFormat, contentType);
				throw new BagriException("No converter found from " + srcFormat + " to " + dataFormat, BagriException.ecDocument);
			}
		}
		logger.trace("getConverter; returning {} for data format: {}, source format: {}, content type: {}", cc, dataFormat, srcFormat, contentType);
		return cc;
	}
	
	private ResultCursorBase<DocumentAccessor> getResultCursor(Properties props) {
		ResultCursorBase<DocumentAccessor> cursor;
		int fetchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
		if (Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"))) {
			String clientId = props.getProperty(pn_client_id);
			String queueName = "client:" + clientId;
			if (hzInstance.getCluster().getMembers().size() > 1) {
				cursor = new BoundedCursorImpl<>(hzInstance, queueName, fetchSize);
			} else {
				cursor = new QueuedCursorImpl<>(hzInstance, queueName);
			}
		} else {
			if (Boolean.parseBoolean(props.getProperty(pn_document_compress, "false"))) {
				cursor = new CompressingCursorImpl<>(repo, fetchSize);
			} else {
				cursor = new FixedCursorImpl<>(fetchSize);
			}
		}
		return cursor;
	}

	@Override
	public ResultCursor<DocumentAccessor> getDocuments(final String pattern, final Properties props) throws BagriException {
		logger.trace("getDocuments.enter; got pattern: {}; props: {}", pattern, props);

		final ResultCursorBase<DocumentAccessor> cln = getResultCursor(props);
		if (cln.isAsynch()) {
			execPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						fetchDocuments(pattern, props, cln);
					} catch (BagriException ex) {
						throw new RuntimeException(ex);
					}
					cln.finish();
				}
			});
		} else {
			fetchDocuments(pattern, props, cln);
		}

		logger.trace("getDocuments.exit; returning: {}", cln);
		return cln;
	}

	@SuppressWarnings("unchecked")
	private void fetchDocuments(String pattern, Properties props, ResultCursorBase<DocumentAccessor> cln) throws BagriException {
		Predicate<DocumentKey, Document> query;
		if (pattern == null) {
			query = Predicates.equal(fnTxFinish, TX_NO);
			//query = new TruePredicate<>();
		} else {
			query = DocumentPredicateBuilder.getQuery(repo, pattern);
		}

		final int fetchSize;
		final long headers;
		if (props != null) {
			fetchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
			headers = Long.parseLong(props.getProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_URI_WITH_CONTENT)));
		} else {
			fetchSize = 0;
			headers = DocumentAccessor.HDR_URI_WITH_CONTENT;
		}
		//if (fetchSize > 0) {
		//	query = new LimitPredicate<>(fetchSize, query);
		//}

		int cnt = 0;
		if (query != null) {
			DocumentAccessorImpl dai;
			java.util.Collection<Document> docs;
			if (fetchSize == 0) {
				docs = ddSvc.getLastDocumentsForQuery(query);
			} else {
				docs = ddSvc.getLastDocumentsForQuery(query, fetchSize);
			}
			if ((headers & DocumentAccessor.HDR_CONTENT) > 0) {
				// doc & content
				for (Document doc: docs) {
					DocumentKey key = factory.newDocumentKey(doc.getDocumentKey());
					Object content = ddSvc.getCachedObject(CN_XDM_CONTENT, key, binaryContent);
					ContentConverter<Object, ?> cc = getConverter(props, doc.getContentType(), null);
					if (cc != null) {
						content = cc.convertTo(content);
					}
					dai = new DocumentAccessorImpl(repo, doc, headers, content);
					if (!cln.add(dai)) {
						break;
					}
					cnt++;
				}
			} else {
				// doc only
				for (Document doc: docs) {
					dai = new DocumentAccessorImpl(repo, doc, headers);
					if (!cln.add(dai)) {
						break;
					}
					cnt++;
				}
			}
		}
		logger.trace("fetchDocuments.exit; fetched {} docs", cnt);
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

	//@Override
	@SuppressWarnings("unchecked")
	public Document createDocument(DocumentKey docKey, String uri, Object content, String dataFormat, Date createdAt, String createdBy,
			long txStart, int[] collections) throws BagriException {

		ParseResults pRes = parseContent(docKey, content, dataFormat, null);

		List<Long> fragments = null;
		List<Data> data = pRes.getResults();
		int length = pRes.getContentLength();
		String root = "/"; //TODO: make constant for this
		if (data != null) { 
			Object[] ids = loadElements(docKey.getKey(), data);
			if (ids == null) {
				logger.warn("createDocument.exit; the document is not valid as it has no root element");
				throw new BagriException("invalid document", BagriException.ecDocument);
			}
			fragments = (List<Long>) ids[0];
			root = pRes.getContentRoot();
		}

		Document doc;
		String format = dataFormat + "/" + def_encoding;
		if (fragments == null || fragments.size() == 0) {
			doc = new Document(docKey.getKey(), uri, root, txStart, TX_NO, createdAt, createdBy, format, length, pRes.getResultSize());
		} else {
			doc = new FragmentedDocument(docKey.getKey(), uri, root, txStart, TX_NO, createdAt, createdBy, format, length, pRes.getResultSize());
			long[] fa = new long[fragments.size()];
			fa[0] = docKey.getKey();
			for (int i=0; i < fragments.size(); i++) {
				fa[i] = fragments.get(i);
			}
			((FragmentedDocument) doc).setFragments(fa);
		}
		
		indexManager.indexDocument(docKey.getKey(), data);

		if (collections != null && collections.length > 0) {
			doc.setCollections(collections);
			for (int clnId: collections) {
				for (Collection cln: repo.getSchema().getCollections()) {
					if (clnId == cln.getId()) {
						updateStats(cln.getName(), true, doc.getElements(), doc.getFragments().length);
						break;
					}
				}
			}
		} else {
			String clName = checkDefaultDocumentCollection(doc);
			if (clName != null) {
				updateStats(clName, true, doc.getElements(), doc.getFragments().length);
			}
		}
		updateStats(null, true, doc.getElements(), doc.getFragments().length);
		
		if (cacheContent) {
			//ddSvc.storeData(docKey, content, CN_XDM_CONTENT);
			cntCache.set(docKey, content);
		}

		// invalidate cached query results on load. what for..?
		//Set<Integer> paths = (Set<Integer>) ids[1];
		//((QueryManagementImpl) repo.getQueryManagement()).invalidateQueryResults(paths);

		return doc;
	}

	private Object[] loadElements(long docKey, List<Data> data) throws BagriException {

		Data dRoot = getDataRoot(data);
		if (dRoot == null) { 
			return null;
		}
		
		List<Long> fragIds = new ArrayList<>(1);
		Set<Integer> pathIds = new HashSet<>(1);
		if (cacheElements) {
			String root = dRoot.getRoot();
			Set<Integer> fragments = new HashSet<>();
			Map<DataKey, Elements> elements = new HashMap<>(data.size());
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
			for (Data xdm: data) {
				pathIds.add(xdm.getPathId());
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
			eltCache.putAll(elements);
			logger.debug("loadElements; cached {} elements for docKey: {}; fragments: {}", elements.size(), docKey, fragIds.size());
		} else {
			//for (Data xdm: data) {
			//	pathIds.add(xdm.getPathId());
			//	indexManager.addIndex(docKey, xdm.getPathId(), xdm.getPath(), xdm.getValue());
			//}
		}

		Object[] result = new Object[2];
		result[0] = fragIds;
		result[1] = pathIds;
		return result;
	}

	public Document processDocument(Map.Entry<DocumentKey, Document> old, long txId, String uri, String user, Object content, ParseResults pRes, Properties props) throws BagriException {

		logger.trace("processDocument.enter; uri: {}; results: {}; props: {}", uri, pRes, props);

		//boolean update = (old.getValue() != null); // && (doc.getTxFinish() == TX_NO || !txManager.isTxVisible(doc.getTxFinish())));
		String dataFormat = props.getProperty(pn_document_data_format, df_xml);
		DocumentKey docKey = old.getKey();
		long docId = docKey.getKey();
		int rSize = pRes.getResultSize(); 
		List<Data> data = pRes.getResults();
		if (old.getValue() != null) {
	    	logger.trace("processDocument; going to update document: {}", old);
	    	Document updated = old.getValue();
			triggerManager.applyTrigger(updated, Order.before, Scope.update);
	    	if (txId > TX_NO) {
		    	// we must finish old Document and create a new one!
		    	updated.finishDocument(txId);
			    old.setValue(updated);
			    docKey = factory.newDocumentKey(docKey.getKey(), docKey.getVersion() + 1); 
				docId = docKey.getKey();
	    	} else {
		    	// we do changes inplace, no new version created
	    		boolean mergeContent = Boolean.parseBoolean(props.getProperty(pn_document_map_merge, "false"));
	    		if (!mergeContent && rSize > 0) {
			    	Set<Integer> pIds = new HashSet<>(rSize);
			    	for (Data dt: data) {
			    		pIds.add(dt.getPathId());
			    	}
		    		// delete old elements
			    	java.util.Collection<Path> paths = model.getTypePaths(updated.getTypeRoot());
			    	//Set<Integer> pathIds = new HashSet<>(paths.size());
					for (Path path: paths) {
						DataKey dKey = factory.newDataKey(updated.getDocumentKey(), path.getPathId());
		    			if (!pIds.contains(dKey.getPathId())) {
		        			ddSvc.deleteCachedObject(CN_XDM_ELEMENT, dKey);
		    			}
		    		}
					// we handle indices later..
	    		} else {
	    			content = mergeContent(docKey, content, dataFormat);
	    		}
	    	}
	    	
			// remove stats for old version
	    	for (int clnId: updated.getCollections()) {
	    		for (Collection cln: repo.getSchema().getCollections()) {
	    			if (clnId == cln.getId()) {
	    				updateStats(cln.getName(), false, updated.getElements(), updated.getFragments().length);
	    				break;
	    			}
	    		}
	    	}
			updateStats(null, false, updated.getElements(), updated.getFragments().length);
		}
		// why before insert trigger not invoked?
		
		int length = pRes.getContentLength();
		String root = pRes.getContentRoot();
		if (rSize > 0 && cacheElements) {
			processElements(docId, data);
		}
		Document newDoc = new Document(docId, uri, root, txId, TX_NO, new Date(), user, dataFormat + "/" + def_encoding, length, rSize);

		String collections = props.getProperty(pn_document_collections);
		if (collections != null) {
			StringTokenizer tc = new StringTokenizer(collections, ", ", false);
			while (tc.hasMoreTokens()) {
				String clName = tc.nextToken();
				Collection cln = repo.getSchema().getCollection(clName);
				if (cln != null) {
					newDoc.addCollection(cln.getId());
					updateStats(clName, true, newDoc.getElements(), newDoc.getFragments().length);
				}
			}
		} else {
			String clName = checkDefaultDocumentCollection(newDoc);
			if (clName != null) {
				updateStats(clName, true, newDoc.getElements(), newDoc.getFragments().length);
			}
		}
		updateStats(null, true, newDoc.getElements(), newDoc.getFragments().length);

		boolean revisioned = docKey.getRevision() > 0;
		Scope scope;
		if (old.getValue() == null) {
			scope = Scope.insert;
			if (revisioned) {
				docCache.set(docKey, newDoc);
			} else {
				old.setValue(newDoc);
			}
		} else {
			scope = Scope.update;
			if (txId == TX_NO && !revisioned) {
				old.setValue(newDoc);
			} else {
				//ddSvc.storeData(docKey, newDoc, CN_XDM_DOCUMENT);
				docCache.set(docKey, newDoc);
			}
		}

		if (cacheContent) {
			//ddSvc.storeData(docKey, content, CN_XDM_CONTENT);
			cntCache.set(docKey, content);
		}
		
		triggerManager.applyTrigger(newDoc, Order.after, scope);

		logger.trace("processDocument.exit; returning: {}", newDoc);
		return newDoc;
	}

	private Set<Integer> processElements(long docKey, List<Data> data) throws BagriException {

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
		//eltCache.putAll(elements);
		for (Map.Entry<DataKey, Elements> e: elements.entrySet()) {
			eltCache.set(e.getKey(), e.getValue());
			//ddSvc.storeData(e.getKey(), e.getValue(), CN_XDM_ELEMENT);
		}
		return pathIds;
	}

	@Override
	public <T> DocumentAccessor storeDocument(String uri, T content, Properties props) throws BagriException {
		logger.trace("storeDocument; got uri: {}; content: {}; props: {}", uri, content, props);

		if (props == null) {
			props = new Properties();
		}
		String srcFormat = props.getProperty(pn_document_data_source);
		ContentConverter<Object, T> cc = getConverter(props, srcFormat, (Class<T>) content.getClass());
		if (cc != null) {
			Object converted = cc.convertFrom(content);
			logger.trace("storeDocument; converted content: {}", converted);
			content = (T) converted;
		}

		Document newDoc = storeDocumentInternal(uri, content, props);
		String headers = props.getProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CLIENT_DOCUMENT));
		long headMask = Long.parseLong(headers);
		DocumentAccessorImpl result;
		if ((headMask & DocumentAccessor.HDR_CONTENT) != 0) {
			result = new DocumentAccessorImpl(repo, newDoc, headMask, content);
		} else {
			result = new DocumentAccessorImpl(repo, newDoc, headMask);
		}
		return result;
	}
	
	private Document storeDocumentInternal(String uri, Object content, Properties props) throws BagriException {
		logger.trace("storeDocumentInternal.enter; uri: {}; content: {}; props: {}", uri, content.getClass().getName(), props);
		if (uri == null) {
			throw new BagriException("Empty URI passed", ecDocument);
		}
		
		boolean update = false;
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
			update = true;
		}

		String dataFormat = props.getProperty(pn_document_data_format);
		ParseResults pRes = parseContent(docKey, content, dataFormat, update ? props : null);
		// if fragmented document - process it in the old style!
		
		Transaction tx = null;
    	String txLevel = props.getProperty(pn_client_txLevel);
    	if (!pv_client_txLevel_skip.equals(txLevel)) {
    		tx = txManager.getCurrentTransaction();
    	}

    	String user = repo.getUserName();
		Object result = docCache.executeOnKey(docKey, new DocumentProcessor(tx, uri, user, content, pRes, props));
		if (result instanceof Exception) {
			logger.error("storeDocumentInternal.error; uri: {}", uri, result);
			if (result instanceof BagriException) {
				throw (BagriException) result;
			}
			throw new BagriException((Exception) result, ecDocument);
		}

		Document newDoc = (Document) result;
		if (tx != null) {
			if (newDoc.getVersion() > dvFirst) {
				txManager.updateCounters(0, 1, 0);
			} else {
				txManager.updateCounters(1, 0, 0);
			}
		}
		// otherwise counters did not change

    	boolean inVals = false;
		String inScope = props.getProperty(pn_query_invalidate, pv_query_invalidate_values);
		boolean inAll = pv_query_invalidate_all.equals(inScope);
		if (pRes.getResultSize() > 0) {
	    	java.util.Collection<Path> paths = model.getTypePaths(newDoc.getTypeRoot());
	    	Set<Integer> pathIds = new HashSet<>(paths.size());
	    	
	    	List<String> inPath = null;
	    	if (!inAll) {
				if (inScope.startsWith("/")) {
					// invalidate by path-value pairs
					inPath = Arrays.asList(inScope.split(";"));
				} else {
					inVals = pv_query_invalidate_values.equals(inScope);
				}
	    	}
	    	
			for (Path path: paths) {
				DataKey dKey = factory.newDataKey(newDoc.getDocumentKey(), path.getPathId());
				Elements elts = ddSvc.getCachedObject(CN_XDM_ELEMENT, dKey, binaryElts);
				if (elts != null) {
					if (indexManager.isPathIndexed(path.getPathId())) {
						for (Element elt: elts.getElements()) {
							indexManager.addIndex(newDoc.getDocumentKey(), path.getPathId(), path.getPath(), elt.getValue());
						}
					}
					pathIds.add(path.getPathId());
					
					if (inVals || (inPath != null && inPath.contains(path.getPath()))) {
						((QueryManagementImpl) repo.getQueryManagement()).invalidateQueryResults(path.getPathId(), elts);
					}
				}
			}

			if (inAll || pv_query_invalidate_paths.equals(inScope)) {
				// invalidate cached query results.
				logger.debug("storeDocumentInternal; going to invalidate {} paths for document {}", pathIds.size(), uri); 
				((QueryManagementImpl) repo.getQueryManagement()).invalidateQueryResults(pathIds);
			} 
		}
		if (inAll || pv_query_invalidate_docs.equals(inScope) || inVals) {
			// don't remember why for no tx only?!
			if (update && tx == null) {
				logger.debug("storeDocumentInternal; going to invalidate query results for document {} with key {}", uri, docKey); 
				((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(docKey.getKey());
			}
		}

		logger.trace("storeDocumentInternal.exit; returning: {}", newDoc);
		return newDoc;
	}
	
	@SuppressWarnings("unchecked")
	private Object mergeContent(DocumentKey docKey, Object newContent, String dataFormat) throws BagriException {
		ContentMerger cm = repo.getMerger(dataFormat);
		if (cm != null) {
   			Object oldContent = ddSvc.getCachedObject(CN_XDM_CONTENT, docKey, binaryContent);
   			return cm.mergeContent(oldContent, newContent);
    	}
    	return newContent;
	}
	
	private ParseResults parseContent(DocumentKey docKey, Object content, String dataFormat, Properties props) throws BagriException {
		ParseResults result;
		boolean cacheElts = cacheElements;
		if (!cacheElts) {
			cacheElts = indexManager.hasIndices();
		}
		if (cacheElts) {
	    	if (props != null) {
				if (Boolean.parseBoolean(props.getProperty(pn_document_map_merge, "false"))) {
	    			content = mergeContent(docKey, content, dataFormat);
				}
	    	}			
			ContentParser<Object> parser = repo.getParser(dataFormat);
			result = parser.parse(content);
			// TODO: check for parse results?
		} else {
			result = new ParseResults(0, null);
		}
		return result;
	}

	@Override
	public <T> ResultCursor<DocumentAccessor> storeDocuments(final Map<String, T> documents, final Properties props) throws BagriException {
		logger.trace("storeDocuments.enter; got documents: {}; props: {}", documents, props);
		final ResultCursorBase<DocumentAccessor> cln = getResultCursor(props);
		if (cln.isAsynch()) {
			try {
				execPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							iterateDocuments((Map<String, Object>) documents, props, cln);
						} catch (BagriException ex) {
							throw new Error(ex);
						}
						cln.finish();
					}
				});
			} catch (Error er) {
				throw (BagriException) er.getCause();
			}
		} else {
			iterateDocuments((Map<String, Object>) documents, props, cln);
		}

		logger.trace("storeDocuments.exit; returning: {}", cln);
		return cln;
	}

	private void iterateDocuments(Map<String, Object> documents, Properties props, ResultCursorBase<DocumentAccessor> cln) throws BagriException {
		for (Map.Entry<String, Object> document: documents.entrySet()) {
			if (ddSvc.isLocalKey(document.getKey().hashCode())) {
				cln.add(storeDocument(document.getKey(), document.getValue(), props));
			}
		}
	}

	@Override
	public DocumentAccessor removeDocument(String uri, Properties props) throws BagriException {
		logger.trace("removeDocument.enter; uri: {}", uri);
		if (uri == null) {
			throw new BagriException("No Document URI passed", BagriException.ecDocument);
		}

		Document doc = getDocument(uri);
		if (doc == null) {
			logger.info("removeDocument; no active document found for uri: {}", uri);
			return null;
		}

		DocumentKey docKey = factory.newDocumentKey(doc.getDocumentKey());
		return removeDocumentInternal(docKey, doc, props);
	}

	private DocumentAccessor removeDocumentInternal(DocumentKey docKey, Document doc, Properties props) throws BagriException {
		if (props == null) {
			props = new Properties();
		}
		Transaction tx = txManager.getCurrentTransaction();
		Object result = docCache.executeOnKey(docKey, new DocumentRemoveProcessor(tx, props));
		if (result instanceof Exception) {
			logger.error("removeDocumentInternal.error; uri: {}", doc.getUri(), result);
			if (result instanceof BagriException) {
				throw (BagriException) result;
			}
			throw new BagriException((Exception) result, ecDocument);
		}

		DocumentAccessorImpl docAccessor = (DocumentAccessorImpl) result;

		// even if tx is null??
		txManager.updateCounters(0, 0, 1);
		
		((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(docKey.getKey());

		return docAccessor;
	}

	public Object processDocumentRemoval(Map.Entry<DocumentKey, Document> entry, Properties properties, long txStart, Document doc) throws BagriException {
		triggerManager.applyTrigger(doc, Order.before, Scope.delete);
		if (txStart == TX_NO) {
			entry.setValue(null);
			cntCache.delete(entry.getKey());
		} else {
			doc.finishDocument(txStart);
			entry.setValue(doc);
		}
		triggerManager.applyTrigger(doc, Order.after, Scope.delete);
		
		String headers = properties.getProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CLIENT_DOCUMENT));
		long headMask = Long.parseLong(headers);
		if ((headMask & DocumentAccessor.HDR_CONTENT) != 0) {
			return new DocumentAccessorImpl(repo, doc, headMask, getDocumentContent(entry.getKey()));
		}
		return new DocumentAccessorImpl(repo, doc, headMask);
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
	    		docCache.delete(docKey);
	    	}
	    	cleaned = true;

	    	// TODO: if xdm-key cache is used it must be cleaned too!

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

	public void evictDocument(DocumentKey docKey, Document doc) {
		logger.trace("evictDocument.enter; docKey: {}, doc: {}", docKey, doc);
		cntCache.delete(docKey);
		// deletes elements together with indices. what if we store indices only!?
    	int size = deleteDocumentElements(doc.getFragments(), doc.getTypeRoot());

    	//what about older document versions? // use document container for versions..
    	
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
		logger.trace("deleteDocumentElements; got {} possible paths to remove; eltCache size: {}",
				allPaths.size(), eltCache.size());
		int iCnt = 0;
		for (long docId: fragments) {
	        for (Path path: allPaths) {
	        	int pathId = path.getPathId();
	        	DataKey dKey = factory.newDataKey(docId, pathId);
	       		Elements elts = eltCache.remove(dKey);
	       		if (elts != null) {
		   			cnt++;
	       			if (indexManager.isPathIndexed(pathId)) {
		       			for (Element elt: elts.getElements()) {
		       				indexManager.removeIndex(docId, pathId, elt.getValue());
		       				iCnt++;
		       			}
		       		}
	        	//} else {
	        	//	eltCache.delete(dKey);
	        	}
	        }
		}
		logger.trace("deleteDocumentElements; deleted keys: {}; indexes: {}; eltCache size after delete: {}",
				cnt, iCnt, eltCache.size());
		return cnt;
	}

	public void rollbackDocument(DocumentKey docKey) {
		logger.trace("rollbackDocument.enter; docKey: {}", docKey);
		boolean rolled = false;
	    Document doc = getDocument(docKey);
	    if (doc != null) {
	    	doc.finishDocument(TX_NO);
	    	docCache.set(docKey, doc);
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

	Set<Long> getCollectionDocumentKeys(int collectId) {
		//
		Set<DocumentKey> docKeys;
		if (collectId == clnDefault) {
			// TODO: local or global keySet ?!
			docKeys = docCache.keySet();
		} else {
			Predicate<DocumentKey, Document> clp = new CollectionPredicate(collectId);
			// TODO: local or global keySet ?!
			docKeys = docCache.keySet(clp);
		}
		Set<Long> result = new HashSet<>(docKeys.size());
		for (DocumentKey key: docKeys) {
			result.add(key.getKey());
		}
		return result;
	}

	@Override
	public ResultCursor<DocumentAccessor> removeDocuments(final String pattern, final Properties props) throws BagriException {
		logger.trace("removeDocuments.enter; pattern: {}", pattern);
		final ResultCursorBase<DocumentAccessor> cln = getResultCursor(props);
		if (cln.isAsynch()) {
			try {
				execPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							deleteDocuments(pattern, props, cln);
						} catch (BagriException ex) {
							throw new Error(ex);
						}
						cln.finish();
					}
				});
			} catch (Error er) {
				throw (BagriException) er.getCause();
			}
		} else {
			deleteDocuments(pattern, props, cln);
		}

		logger.trace("removeDocuments.exit; returning: {}", cln);
		return cln;
	}

	private void deleteDocuments(String pattern, Properties props, ResultCursorBase cln) throws BagriException {
		Predicate<DocumentKey, Document> query = DocumentPredicateBuilder.getQuery(repo, pattern);

		// remove local documents only?! yes!
		java.util.Collection<Document> docs = ddSvc.getLastDocumentsForQuery(query);

		for (Document doc: docs) {
			DocumentKey docKey = factory.newDocumentKey(doc.getDocumentKey());
			cln.add(removeDocumentInternal(docKey, doc, props));
		}
		//logger.trace("deleteDocuments.exit; removed: {}", cln.size());
	}

	@Override
	public int addDocumentToCollections(String uri, String[] collections) {
		logger.trace("addDocumentsToCollections.enter; got uri: {}; collectIds: {}", uri, Arrays.toString(collections));
		int cnt = 0;
		Document doc = getDocument(uri);
		if (doc != null) {
			DocumentKey key = factory.newDocumentKey(doc.getDocumentKey());
			cnt = updateDocumentCollections(true, new MapEntrySimple<>(key, doc), collections);
			if (cnt > 0) {
				docCache.set(key, doc);
			}
		}
		logger.trace("addDocumentsToCollections.exit; added: {}", cnt);
		return cnt;
	}

	@Override
	public int removeDocumentFromCollections(String uri, String[] collections) {
		logger.trace("removeDocumentsFromCollections.enter; got uri: {}; collectIds: {}", uri, Arrays.toString(collections));
		int cnt = 0;
		Document doc = getDocument(uri);
		if (doc != null) {
			DocumentKey key = factory.newDocumentKey(doc.getDocumentKey());
			cnt = updateDocumentCollections(false, new MapEntrySimple<>(key, doc), collections);
			if (cnt > 0) {
				docCache.set(key, doc);
			}
		}
		logger.trace("removeDocumentsFromCollections.exit; removed: {}", cnt);
		return cnt;
	}
	
	public int updateDocumentCollections(boolean add, Map.Entry<DocumentKey, Document> entry, String[] collections) {
		int updCount = 0;
		Document doc = entry.getValue();
		if (doc != null) {
			int size = doc.getElements();
			for (Collection cln: repo.getSchema().getCollections()) {
				for (String collection: collections) {
					if (collection.equals(cln.getName())) {
						if (add) {
							if (doc.addCollection(cln.getId())) {
								updCount++;
								updateStats(cln.getName(), true, size, doc.getFragments().length);
							}
						} else {
							if (doc.removeCollection(cln.getId())) {
								updCount++;
								updateStats(cln.getName(), false, size, doc.getFragments().length);
							}
						}
						break;
					}
				}
			}
			if (updCount > 0) {
				entry.setValue(doc);
			}
		}
		return updCount;
	}

	private void updateStats(String name, boolean add, int elements, int fragments) {
		if (enableStats) {
			if (!queue.offer(new StatisticsEvent(name, add, new Object[] {fragments, elements}))) {
				logger.warn("updateStats; queue is full!!");
			}
		}
	}

    private boolean lockDocument(DocumentKey docKey, long timeout) { //throws XDMException {

        boolean locked = false;
        if (timeout > 0) {
            try {
                locked = docCache.tryLock(docKey, timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                logger.error("lockDocument.error", ex);
                //throw new XDMException(ex);
            }
        } else {
            locked = docCache.tryLock(docKey);
        }
        return locked;
    }

    private void unlockDocument(DocumentKey docKey) {

        docCache.unlock(docKey);
    }


    @Override
    public void entryEvicted(EntryEvent<DocumentKey, Document> event) {
		evictDocument(event.getKey(), event.getOldValue());
    }
    
}

