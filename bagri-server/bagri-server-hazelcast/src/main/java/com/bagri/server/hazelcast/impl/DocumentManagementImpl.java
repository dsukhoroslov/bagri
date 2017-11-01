package com.bagri.server.hazelcast.impl;

import com.bagri.client.hazelcast.UrlHashKey;
import com.bagri.client.hazelcast.impl.FixedCollectionImpl;
import com.bagri.client.hazelcast.impl.QueuedCollectionImpl;
import com.bagri.client.hazelcast.impl.ZippedCollectionImpl;
import com.bagri.client.hazelcast.task.doc.DocumentProvider;
import com.bagri.core.DataKey;
import com.bagri.core.DocumentKey;
import com.bagri.core.KeyFactory;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCollection;
import com.bagri.core.model.Data;
import com.bagri.core.model.Document;
import com.bagri.core.model.Element;
import com.bagri.core.model.Elements;
import com.bagri.core.model.FragmentedDocument;
import com.bagri.core.model.Path;
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
import com.bagri.server.hazelcast.predicate.DocumentPredicateBuilder;
import com.bagri.server.hazelcast.predicate.LimitPredicate;
import com.bagri.server.hazelcast.task.doc.DocumentProcessor;
import com.bagri.server.hazelcast.task.doc.DocumentRemoveProcessor;
import com.bagri.support.idgen.IdGenerator;
import com.bagri.support.stats.StatisticsEvent;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.bagri.core.Constants.*;
import static com.bagri.core.api.BagriException.*;
import static com.bagri.core.api.TransactionManagement.*;
import static com.bagri.core.model.Document.*;
import static com.bagri.core.query.PathBuilder.*;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.core.system.DataFormat.*;
import static com.bagri.support.util.FileUtils.*;

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
    private IMap<UrlHashKey, List<DocumentKey>> keyCache;

	private boolean binaryDocs;
	private boolean binaryElts;
	private boolean binaryContent;

    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;

	//private IExecutorService execSvc;
	private ExecutorService execSvc;
	private Map<String, byte[]> sharedMap;

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
    	keyCache = repo.getHzInstance().getMap(CN_XDM_KEY);
    	//execSvc = repo.getHzInstance().getExecutorService(PN_XDM_TRANS_POOL);
		sharedMap = new HashMap<>(10);
		for (int j=0; j < 10; j++) {
			sharedMap.put("field" + j, org.apache.commons.lang3.RandomStringUtils.random(100).getBytes());
		}
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

	@SuppressWarnings("unchecked")
	private Set<DocumentKey> getDocumentsOfType(String root) {
   		Predicate<DocumentKey, Document> f = Predicates.and(Predicates.equal(fnRoot, root),
   				Predicates.equal(fnTxFinish, TX_NO));
		return xddCache.keySet(f);
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
   			if (doc.getTxFinish() != TX_NO) { // || !txManager.isTxVisible(lastDoc.getTxFinish())) {
   				logger.debug("getDocument; the latest document version is finished already: {}", doc);
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
	public Iterable<String> getDocumentUris(final String pattern, final Properties props) {
		logger.trace("getDocumentUris.enter; got pattern: {}; props: {}", pattern, props);
		final int fetchSize;
		boolean asynch = false;
		if (props != null) {
			fetchSize = Integer.valueOf(props.getProperty(pn_client_fetchSize, "0"));
			asynch = Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"));
		} else {
			fetchSize = 0;
		}

		final ResultCollection<String> cln;
		if (asynch) {
			String clientId = props.getProperty(pn_client_id);
			cln = new QueuedCollectionImpl<>(hzInstance, "client:" + clientId);
			execSvc.execute(new Runnable() {
				@Override
				public void run() {
					fetchUris(pattern, fetchSize, cln);
					// TODO: check terminator..
					cln.add(null); //Null._null);
				}
			});
		} else {
			// what if fetchSize = 0!?
			cln = new FixedCollectionImpl<>(fetchSize);
			fetchUris(pattern, fetchSize, cln);
		}

		logger.trace("getDocumentUris.exit; returning: {}", cln);
		return cln;
	}

	private void fetchUris(String pattern, int fetchSize, ResultCollection<String> cln) {
		Predicate<DocumentKey, Document> query;
		if (pattern == null) {
			query = Predicates.equal(fnTxFinish, TX_NO);
		} else {
			query = DocumentPredicateBuilder.getQuery(repo, pattern);
		}
		if (fetchSize > 0) {
			query = new LimitPredicate<>(fetchSize, query);
		}

		int cnt = 0;
		if (query != null) {
			java.util.Collection<Document> docs = ddSvc.getLastDocumentsForQuery(query, fetchSize);
			for (Document doc: docs) {
				cln.add(doc.getUri());
			}
		}
		logger.trace("fetchUris.exit; fetched {} uris", cnt);
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

	@SuppressWarnings("unchecked")
	private DocumentAccessor getDocumentInternal(DocumentKey docKey, Document doc, Properties props) throws BagriException {
		if (props == null) {
			props = new Properties();
		}
		String headers = props.getProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_URI_WITH_CONTENT)); //CLIENT_DOCUMENT
		long headMask = Long.parseLong(headers);
		if ((headMask & DocumentAccessor.HDR_CONTENT) != 0) {
			ContentConverter<Object, ?> cc = getConverter(props, doc.getContentType(), null);
			Object content = getDocumentContent(docKey);
			logger.trace("getDocument; got content: {}", content);
			if (content == null) {
				String dataFormat = props.getProperty(pn_document_data_format);
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
					DocumentProvider xp = new DocumentProvider(repo.getClientId(), txManager.getCurrentTxId(), props, doc.getUri());
					content = xddCache.executeOnKey(docKey, xp);
				}
			}
			if (cc != null) {
				content = cc.convertTo(content);
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
					cType = Map.class;
				} 
				// else repo will return common Bean converter
			} else {
				cType = contentType;
			}
			cc = repo.getConverter(srcFormat, cType); 
			if (cc == null) {
				throw new BagriException("No converter found from " + srcFormat + " to " + dataFormat, BagriException.ecDocument);
			}
		}
		return cc;
	}
	
	private ResultCollection<DocumentAccessor> getResultIterator(Properties props) {
		ResultCollection<DocumentAccessor> iter;
		if (Boolean.parseBoolean(props.getProperty(pn_client_fetchAsynch, "false"))) {
			String clientId = props.getProperty(pn_client_id);
			iter = new QueuedCollectionImpl<>(hzInstance, "client:" + clientId);
		} else {
			int fetchSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, "0"));
			if (Boolean.parseBoolean(props.getProperty(pn_document_compress, "false"))) {
				iter = new ZippedCollectionImpl<>(fetchSize, ddSvc.getSerializationService());
			} else {
				iter = new FixedCollectionImpl<>(fetchSize);
			}
		}
		return iter;
	}

	@Override
	public Iterable<DocumentAccessor> getDocuments(final String pattern, final Properties props) throws BagriException {
		logger.trace("getDocuments.enter; got pattern: {}; props: {}", pattern, props);

		final ResultCollection<DocumentAccessor> cln = getResultIterator(props);
		if (cln.isAsynch()) {
			execSvc.execute(new Runnable() {
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

	private void fetchDocuments(String pattern, Properties props, ResultCollection<DocumentAccessor> cln) throws BagriException {
		Predicate<DocumentKey, Document> query;
		if (pattern == null) {
			query = Predicates.equal(fnTxFinish, TX_NO);
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
		if (fetchSize > 0) {
			query = new LimitPredicate<>(fetchSize, query);
		}

		int cnt = 0;
		if (query != null) {
			DocumentAccessorImpl dai;
			java.util.Collection<Document> docs = ddSvc.getLastDocumentsForQuery(query, fetchSize);
			if ((headers & DocumentAccessor.HDR_CONTENT) > 0) {
				// doc & content
				for (Document doc: docs) {
				//for (int i=0; i < fetchSize; i++) {
					DocumentKey key = factory.newDocumentKey(doc.getDocumentKey());
					Object content = ddSvc.getCachedObject(CN_XDM_CONTENT, key, binaryContent);
					ContentConverter<Object, ?> cc = getConverter(props, doc.getContentType(), null);
					if (cc != null) {
						content = cc.convertTo(content);
					}
					dai = new DocumentAccessorImpl(repo, doc, headers, content);
					//dai = new DocumentAccessorImpl(repo, null, sharedMap, "BMAP", 0, null, null, 0, 0, 0, 0, null, 0, 0, null, 0); 
					cln.add(dai);
					cnt++;
				}
			} else {
				// doc only
				for (Document doc: docs) {
				//for (int i=0; i < fetchSize; i++) {
					dai = new DocumentAccessorImpl(repo, doc, headers);
					//dai = new DocumentAccessorImpl(repo, null, sharedMap); 
					cln.add(dai);
					cnt++;
				}
			}
		}
		logger.trace("fetchDocuments.exit; fetched {} docs", cnt);
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
			long txStart, int[] collections, boolean addContent) throws BagriException {

		ParseResults pRes;
		//dataFormat = repo.getHandler(dataFormat).getDataFormat();
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
	    		if (!mergeElts) {
			    	Set<Integer> pIds = new HashSet<>(data.size());
			    	for (Data dt: data) {
			    		pIds.add(dt.getPathId());
			    	}
		    		// delete old elements
			    	java.util.Collection<Path> paths = model.getTypePaths(updated.getTypeRoot());
			    	Set<Integer> pathIds = new HashSet<>(paths.size());
					for (Path path: paths) {
						DataKey dKey = factory.newDataKey(updated.getDocumentKey(), path.getPathId());
		    			if (!pIds.contains(dKey.getPathId())) {
		        			ddSvc.deleteCachedObject(CN_XDM_ELEMENT, dKey);
		    			}
		    		}
	    		}
	    	}
		}

		int length = pRes.getContentLength();
		String root = data.get(0).getRoot();
		Set<Integer> ids = processElements(docId, data);
		String dataFormat = props.getProperty(pn_document_data_format, df_xml);
		Document newDoc = new Document(docId, uri, root, txId, TX_NO, new Date(), repo.getUserName(), dataFormat + "/" + def_encoding, length, data.size());

		String collections = props.getProperty(pn_document_collections);
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

		//UrlHashKey hash = new UrlHashKey(uri);
		//List<DocumentKey> keys = ddSvc.getCachedObject(CN_XDM_KEY, hash, true);
		//if (keys == null) {
		//	keys = new ArrayList<>();
		//}
		//keys.add(docKey);
		//keyCache.set(hash, keys);

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
		//dataFormat = repo.getHandler(dataFormat).getDataFormat();
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
			logger.error("storeDocumentInternal.error; uri: {}", uri, result);
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
    	//Map<DataKey, Elements> eMap = ddSvc.getElements(newDoc.getDocumentKey());
		for (Path path: paths) {
			DataKey dKey = factory.newDataKey(newDoc.getDocumentKey(), path.getPathId());
			//Elements elts = eMap.get(dKey);
			Elements elts = ddSvc.getCachedObject(CN_XDM_ELEMENT, dKey, binaryElts);
			if (elts != null) {
				if (indexManager.isPathIndexed(path.getPathId())) {
					for (Element elt: elts.getElements()) {
						indexManager.addIndex(newDoc.getDocumentKey(), path.getPathId(), path.getPath(), elt.getValue());
					}
				}
				pathIds.add(path.getPathId());
			}
		}

		// invalidate cached query results.
		((QueryManagementImpl) repo.getQueryManagement()).invalidateQueryResults(pathIds);

		logger.trace("storeDocumentInternal.exit; returning: {}", newDoc);
		return newDoc;
	}

	@Override
	public <T> Iterable<DocumentAccessor> storeDocuments(final Map<String, T> documents, final Properties props) throws BagriException {
		logger.trace("storeDocuments.enter; got documents: {}; props: {}", documents, props);
		final ResultCollection<DocumentAccessor> cln = getResultIterator(props);
		if (cln.isAsynch()) {
			try {
				execSvc.execute(new Runnable() {
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

	private void iterateDocuments(Map<String, Object> documents, Properties props, ResultCollection<DocumentAccessor> cln) throws BagriException {
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
		triggerManager.applyTrigger(doc, Order.before, Scope.delete);
		Object result = xddCache.executeOnKey(docKey, new DocumentRemoveProcessor(txManager.getCurrentTransaction(), ddSvc.getLastKeyForUri(doc.getUri()), props));
		if (result instanceof Exception) {
			logger.error("removeDocumentInternal.error; uri: {}", doc.getUri(), result);
			if (result instanceof BagriException) {
				throw (BagriException) result;
			}
			throw new BagriException((Exception) result, ecDocument);
		}

		DocumentAccessorImpl docAccessor = (DocumentAccessorImpl) result;

		txManager.updateCounters(0, 0, 1);
		Document newDoc = xddCache.get(docKey);
        if (newDoc != null) {
			triggerManager.applyTrigger(newDoc, Order.after, Scope.delete);
        }
		((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(docKey.getKey());

		return docAccessor;
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
	public Iterable<DocumentAccessor> removeDocuments(final String pattern, final Properties props) throws BagriException {
		logger.trace("removeDocuments.enter; pattern: {}", pattern);
		final ResultCollection<DocumentAccessor> cln = getResultIterator(props);
		if (cln.isAsynch()) {
			try {
				execSvc.execute(new Runnable() {
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

	private void deleteDocuments(String pattern, Properties props, ResultCollection<DocumentAccessor> cln) throws BagriException {
		Predicate<DocumentKey, Document> query = DocumentPredicateBuilder.getQuery(repo, pattern);

		// remove local documents only?! yes!
		java.util.Collection<Document> docs = ddSvc.getLastDocumentsForQuery(query, 0);

		for (Document doc: docs) {
			DocumentKey docKey = factory.newDocumentKey(doc.getDocumentKey());
			cln.add(removeDocumentInternal(docKey, doc, props));
		}
		//logger.trace("deleteDocuments.exit; removed: {}", cln.size());
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

	public Object processDocumentRemoval(Map.Entry<DocumentKey, Document> entry, Properties properties, long txStart, Document doc) {
		if (txStart == TX_NO) {
			entry.setValue(null);
			cntCache.delete(entry.getKey());
			return new DocumentAccessorImpl();
		} else {
			doc.finishDocument(txStart);
		}
		entry.setValue(doc);
		String headers = properties.getProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CLIENT_DOCUMENT));
		long headMask = Long.parseLong(headers);
		if ((headMask & DocumentAccessor.HDR_CONTENT) != 0) {
			return new DocumentAccessorImpl(repo, doc, headMask, getDocumentContent(entry.getKey()));
		}
		return new DocumentAccessorImpl(repo, doc, headMask);
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

}
