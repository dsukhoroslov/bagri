package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.query.PathBuilder.*;
import static com.bagri.common.config.XDMConfigConstants.*;
import static com.bagri.common.util.XMLUtils.*;
import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;
import static com.bagri.xdm.domain.XDMDocument.dvFirst;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Source;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.common.XDMDocumentManagementServer;
import com.bagri.xdm.cache.hazelcast.predicate.CollectionPredicate;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.common.XDMDocumentKey;
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
import com.bagri.xdm.system.XDMTriggerAction.Action;
import com.bagri.xdm.system.XDMTriggerAction.Scope;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentManagementImpl extends XDMDocumentManagementServer {
	
	private RepositoryImpl repo;
    private HazelcastInstance hzInstance;
    private IndexManagementImpl indexManager;
    private TransactionManagementImpl txManager;
    private TriggerManagementImpl triggerManager;

    private IdGenerator<Long> docGen;
    private Map<XDMDocumentKey, Source> srcCache;
    private IMap<XDMDocumentKey, String> xmlCache;
	private IMap<XDMDocumentKey, XDMDocument> xddCache;
    private IMap<XDMDataKey, XDMElements> xdmCache;

    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;
    
    public void setRepository(RepositoryImpl repo) {
    	this.repo = repo;
    	//this.model = repo.getModelManagement();
    	this.txManager = (TransactionManagementImpl) repo.getTxManagement();
    	this.triggerManager = (TriggerManagementImpl) repo.getTriggerManagement();
    }
    
    IMap<XDMDocumentKey, String> getXmlCache() {
    	return xmlCache;
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

    public void setXmlCache(IMap<XDMDocumentKey, String> cache) {
    	this.xmlCache = cache;
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
    
    public Collection<XDMElements> getDocumentElements(long docKey) {
		XDMDocument doc = getDocument(docKey);
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
		if (props != null) {
			String format = props.getProperty(xdm_document_data_format);
			if (format != null) {
				return format;
			}
		}
		return XDMParser.df_xml;
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
	
	@Override
	public XDMDocument getDocument(XDMDocumentId docId) {
		XDMDocument doc = null;
		XDMDocumentKey docKey = getDocumentKey(docId);
		if (docKey != null) {
			doc = getDocument(docKey);
		}
		logger.trace("getDocument; returning: {}", doc);
		return doc;
	}

	public XDMDocument getDocument(long docKey) {
		XDMDocument doc = getDocument(factory.newXDMDocumentKey(docKey)); 
		logger.trace("getDocument; returning: {}", doc);
		return doc;
	}
	
	private XDMDocument getDocument(XDMDocumentKey docKey) {
		return xddCache.get(docKey); 
	}

    public XDMDocumentKey getDocumentKey(XDMDocumentId docId) {
		long docKey; 
		if (docId.getDocumentKey() > 0) {
			docKey = docId.getDocumentKey(); 
		} else {
			docKey = getDocumentId(docId.getDocumentUri());
	    	if (docKey == 0) {
	    		return null;
	    	}
		}
		return factory.newXDMDocumentKey(docKey);
    }
    
	public XDMDocumentKey nextDocumentKey() {
		return factory.newXDMDocumentKey(docGen.next(), dvFirst);
	}
	
	@SuppressWarnings("unchecked")
	private long getDocumentId(String uri) {
		// the txFinish can be > 0, but not committed yet!
   		Predicate<XDMDocumentKey, XDMDocument> f = Predicates.and(Predicates.equal("uri", uri), 
   				Predicates.equal("txFinish", 0L));
		Set<XDMDocumentKey> docKeys = xddCache.keySet(f);
		if (docKeys.size() == 0) {
			return 0L;
		}

		// should also check if doc's start transaction is committed..
		long docId = 0;
		for (XDMDocumentKey docKey: docKeys) {
			if (docKey.getKey() > docId) {
				docId = docKey.getKey();
			}
		}
		return docId;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<XDMDocumentId> getDocumentIds(String pattern) {
		// TODO: search by uri only so far;
		// implement other search types: by dates, owner, etc..
		logger.trace("getDocumentIds.enter; got pattern: {}", pattern);
   		Predicate<XDMDocumentKey, XDMDocument> f = Predicates.and(Predicates.regex("uri", pattern), 
   				Predicates.equal("txFinish", 0L));
		Set<XDMDocumentKey> docKeys = xddCache.keySet(f);

		// should also check if doc's start transaction is committed..
		List<XDMDocumentId> result = new ArrayList<>(docKeys.size());
		for (XDMDocumentKey docKey: docKeys) {
			result.add(new XDMDocumentId(docKey.getKey()));
		}
		logger.trace("getDocumentIds.exit; returning: {}", result.size());
		return result;
	}
	
	@Override
	public Collection<String> buildDocument(Set<Long> docKeys, String template, Map<String, String> params) {
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
						xml = xmlCache.get(docKey);
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
    
	private String buildElement(String path, long[] fragments, int docType) {
    	Set<XDMDataKey> xdKeys = getDocumentElementKeys(path, fragments, docType);
       	return buildXml(xdmCache.getAll(xdKeys));
    }
    
	@Override
	public Object getDocumentAsBean(XDMDocumentId docId) throws XDMException {
		String xml = getDocumentAsString(docId);
		try {
			return beanFromXML(xml);
		} catch (IOException ex) {
			throw new XDMException(ex.getMessage(), XDMException.ecInOut);
		}
	}

	@Override
	public Map<String, Object> getDocumentAsMap(XDMDocumentId docId) throws XDMException {
		String xml = getDocumentAsString(docId);
		return mapFromXML(xml);
	}

	@Override
	public String getDocumentAsString(XDMDocumentId docId) throws XDMException {
		XDMDocumentKey docKey = getDocumentKey(docId);
		if (docKey == null) {
			//throw new XDMException("No document found for document Id: " + docId, XDMException.ecDocument);
			logger.info("getDocumentAsString; can not construct valid DocumentKey ID: {}", docId);
			return null;
		}

		String xml = xmlCache.get(docKey);
		// very expensive operation!!
		//logger.trace("getDocumentAsString; xml cache stats: {}", xmlCache.getLocalMapStats());
		if (xml == null) {
			XDMDocument doc = getDocument(docKey);
			if (doc == null) {
				logger.info("getDocumentAsString; no document found for ID: {}", docId);
				return null;
			}
			
			// if docId is not local then buildDocument returns null!
			// query docId owner node for the XML instead
			if (hzInstance.getPartitionService().getPartition(docKey).getOwner().localMember()) {
				String root = model.getDocumentRoot(doc.getTypeId());
				Map<String, String> params = new HashMap<String, String>();
				params.put(":doc", root);
				Collection<String> results = buildDocument(Collections.singleton(docId.getDocumentKey()), ":doc", params);
				if (!results.isEmpty()) {
					xml = results.iterator().next();
					xmlCache.set(docKey, xml);
				}
			} else {
				DocumentContentProvider xp = new DocumentContentProvider(repo.getClientId(), docId); //??
				IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
				Future<String> future = execService.submitToKeyOwner(xp, docId);
				try {
					xml = future.get();
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("getDocumentAsString; error getting result", ex);
					throw new XDMException(ex, XDMException.ecDocument);
				}
			}
		}
		return xml;
	}

	// TODO: implementation of the ..source methods is wrong!?
	@Override
	public Source getDocumentAsSource(XDMDocumentId docId) {
		return srcCache.get(factory.newXDMDocumentKey(docId.getDocumentKey()));
	}
	
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
	
	private List<String> checkDocumentCollections(XDMDocument doc, Properties props) {
		List<String> result = new ArrayList<>();
		if (props != null) {
			String prop = props.getProperty(xdm_document_collections);
			if (prop != null) {
				String[] clns = prop.split(", ");
				if (clns.length > 0) {
					for (String clName: clns) {
						XDMCollection cln = repo.getSchema().getCollection(clName);
						logger.trace("checkDocumentCollections; got collection: {} for name: {}", cln, clName);
						if (cln != null) {
							doc.addCollection(cln.getId());
							result.add(clName);
						}
					}
				}
			}
		}
		return result;
	}
    
	@SuppressWarnings("unchecked")
	public XDMDocument createDocument(XDMDocumentId docId, String content, Properties props) throws XDMException {
		logger.trace("createDocument.enter; docId: {}; props: {}", docId, props);
		// TODO: move this out & refactor ?
		String dataFormat = getDataFormat(props).toUpperCase();
		XDMParser parser = factory.newXDMParser(dataFormat, model);
		List<XDMData> data = parser.parse(content);

		XDMDocumentKey docKey = factory.newXDMDocumentKey(docId.getDocumentKey());
		Object[] ids = loadElements(docKey.getKey(), data);
		List<Long> fragments = (List<Long>) ids[0];
		if (fragments == null) {
			logger.warn("createDocument.exit; the document is not valid as it has no root element");
			throw new XDMException("invalid document", XDMException.ecDocument);
		} 
		int docType = fragments.get(0).intValue();
		String user = repo.getUserName();
		XDMDocument doc;
		if (fragments.size() == 1) {
			doc = new XDMDocument(docKey.getDocumentId(), docKey.getVersion(), docId.getDocumentUri(), docType, user, txManager.getCurrentTxId());
		} else {
			doc = new XDMFragmentedDocument(docKey.getDocumentId(), docKey.getVersion(), docId.getDocumentUri(), docType, user, txManager.getCurrentTxId());
			long[] fa = new long[fragments.size()];
			fa[0] = docKey.getKey();
			for (int i=1; i < fragments.size(); i++) {
				fa[i] = fragments.get(i);
			}
			((XDMFragmentedDocument) doc).setFragments(fa);
		}

		List<String> clns = checkDocumentCollections(doc, props); 
		if (clns.size() == 0) {
			String cln = checkDefaultDocumentCollection(doc);
			if (cln != null) {
				clns.add(cln);
			}
		}
		
		Action action;
		if (docKey.getVersion() == dvFirst) {
			action = Action.insert;
			triggerManager.applyTrigger(doc, action, Scope.before);
		} else {
			action = Action.update;
			// trigger has been already invoked in storeDocument..
		}
		xddCache.set(docKey, doc);
		xmlCache.set(docKey, content);
		triggerManager.applyTrigger(doc, action, Scope.after);

		// invalidate cached query results
		Set<Integer> paths = (Set<Integer>) ids[1];
		((QueryManagementImpl) repo.getQueryManagement()).invalidateQueryResults(paths);

		// update statistics
		for (String cln: clns) {
			updateStats(cln, true, data.size(), doc.getFragments().length);
			//updateStats(cln, true, paths.size(), doc.getFragments().length);
		}
		
		logger.trace("createDocument.exit; returning: {}", doc);
		return doc;
	}
	
	public Object[] loadElements(long docKey, List<XDMData> data) throws XDMException {
		
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
					fraPath = docGen.next();
					fragIds.add(fraPath);
					fraPost = xdm.getPostId();
				} else if (fraPost > 0 && xdm.getPathId() > fraPost) {
					fraPath = docKey;
					fraPost = 0;
				}
				pathIds.add(xdm.getPathId());
				XDMDataKey xdk = factory.newXDMDataKey(fraPath, xdm.getPathId());
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
	public XDMDocument storeDocumentFromBean(XDMDocumentId docId, Object bean, Properties props) throws XDMException {
		try {
			String xml = beanToXML(bean);
			if (xml == null || xml.trim().length() == 0) {
				throw new XDMException("Can not convert bean [" + bean + "] to XML", XDMException.ecDocument);
			}
			logger.trace("storeDocumentFromBean; converted bean: {}", xml); 
			
			if (props != null) {
				props.setProperty(xdm_document_data_format, XDMParser.df_xml);
			}
			return storeDocumentFromString(docId, xml, props);
		} catch (IOException ex) {
			throw new XDMException(ex.getMessage(), XDMException.ecInOut);
		}
	}

	@Override
	public XDMDocument storeDocumentFromMap(XDMDocumentId docId, Map<String, Object> fields, Properties props) throws XDMException {
		String xml = mapToXML(fields);
		if (xml == null || xml.trim().length() == 0) {
			throw new XDMException("Can not convert map [" + fields + "] to XML", XDMException.ecDocument);
		}
		logger.trace("storeDocumentFromMap; converted map: {}", xml); 
		
		if (props != null) {
			props.setProperty(xdm_document_data_format, XDMParser.df_xml);
		}
		return storeDocumentFromString(docId, xml, props);
	}
	
	// TODO: implementation of the ..source methods is wrong!?
	@Override
	public XDMDocument storeDocumentFromSource(XDMDocumentId docId, Source source, Properties props) {
		srcCache.put(factory.newXDMDocumentKey(docId.getDocumentKey()), source);
		return xddCache.get(docId);
	}
	
	@Override
	public XDMDocument storeDocumentFromString(XDMDocumentId docId, String xml, Properties props) throws XDMException {
		logger.trace("storeDocumentFromString.enter; docId: {}; xml: {}; props: {}", docId, xml.length(), props);
		String ext = getDataFormat(props).toLowerCase();
		boolean update = false;
		if (docId == null) {
			long docKey = XDMDocumentKey.toKey(docGen.next(), dvFirst);
			docId = new XDMDocumentId(docKey, docKey + "." + ext);
		} else {
			if (docId.getDocumentKey() == 0) {
				if (docId.getDocumentUri() == null) {
					throw new XDMException("Empty Document ID passed", XDMException.ecDocument); 
				}
				long existingId = getDocumentId(docId.getDocumentUri());
				if (existingId > 0) {
					docId = new XDMDocumentId(existingId, docId.getDocumentUri());
					update = true;
				} else {
					docId = new XDMDocumentId(docGen.next(), dvFirst, docId.getDocumentUri());
				}
			} else {
				//update = true;
				if (docId.getDocumentUri() == null) {
					docId = new XDMDocumentId(docId.getDocumentKey(), docId.getDocumentKey() + "." + ext);
				} else {
					long existingId = getDocumentId(docId.getDocumentUri());
					// shouldn't we check here if document with docId exists?
					if (existingId > 0 && existingId != docId.getDocumentKey()) {
						// otherwise we'll get a situation when two different Documents
						// are stored in the same file.
						// what if they point to different versions of the same document!?
						throw new XDMException("Document with URI '" + docId.getDocumentUri() + "' already exists; docId: " + 
							existingId, XDMException.ecDocument);
					}
				}
				XDMDocumentKey docKey = factory.newXDMDocumentKey(docId.getDocumentKey());
				update = xddCache.containsKey(docKey);
			}
		}
		
		XDMDocumentKey docKey = factory.newXDMDocumentKey(docId.getDocumentKey());
		boolean locked = lockDocument(docKey);
		if (locked) {
			try {
				XDMDocumentKey newKey = docKey;
				if (update) {
				    XDMDocument doc = xddCache.get(newKey);
				    if (doc != null) {
				    	if (doc.getTxFinish() > TX_NO && txManager.isTxVisible(doc.getTxFinish())) {
				    		throw new XDMException("Document with ID: " + doc.getDocumentId() + 
				    				", version: " + doc.getVersion() + " has been concurrently updated", 
				    				XDMException.ecDocument);
				    	}
				    	logger.trace("storeDocumentFromString; going to update document: {}", doc);
				    	// we must finish old Document and create a new one!
						triggerManager.applyTrigger(doc, Action.update, Scope.before);
				    	doc.finishDocument(txManager.getCurrentTxId());
				    	// do this asynch after tx?
				    	((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(newKey.getKey());
				    	xddCache.put(docKey, doc);
				    	newKey = factory.newXDMDocumentKey(doc.getDocumentId(), doc.getVersion() + 1);
				    	// shouldn't we lock the newKey too?
				    }
				}
				docId = new XDMDocumentId(newKey.getKey(), docId.getDocumentUri());
				XDMDocument result = createDocument(docId, xml, props);
				if (update) {
					txManager.updateCounters(0, 1, 0);
				} else {
					txManager.updateCounters(1, 0, 0);
				}
			    return result;
			} catch (XDMException ex) {
				throw ex;
			} catch (Exception ex) {
				logger.error("storeDocumentFromString.error; docId: " + docId, ex);
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
	public void removeDocument(XDMDocumentId docId) throws XDMException {
		logger.trace("removeDocument.enter; docId: {}", docId);
	    XDMDocumentKey docKey = getDocumentKey(docId);
	    if (docKey == null) {
    		throw new XDMException("No document found for document Id: " + docId, XDMException.ecDocument);
	    }
	    
	    boolean removed = false;
		boolean locked = lockDocument(docKey);
		if (locked) {
			try {
			    XDMDocument doc = getDocument(docKey);
			    if (doc != null && (doc.getTxFinish() == TX_NO || !txManager.isTxVisible(doc.getTxFinish()))) {
					triggerManager.applyTrigger(doc, Action.delete, Scope.before); 
			    	doc.finishDocument(txManager.getCurrentTxId()); 
			    	xddCache.put(docKey, doc);
			    	((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(doc.getDocumentKey());
			    	triggerManager.applyTrigger(doc, Action.delete, Scope.after); 
			    	txManager.updateCounters(0, 0, 1);
				    removed = true;
			    }
			} catch (XDMException ex) {
				throw ex;
			} catch (Exception ex) {
				logger.error("removeDocument.error; docId: " + docId, ex);
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
			xmlCache.delete(docKey);
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
	    }
    	((QueryManagementImpl) repo.getQueryManagement()).removeQueryResults(docKey.getKey());
		logger.trace("cleanDocument.exit; cleaned: {}", cleaned);
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
	public Collection<XDMDocumentId> getCollectionDocumentIds(int collectId) {
		//
		Predicate<XDMDocumentKey, XDMDocument> clp = new CollectionPredicate(collectId);
		Set<XDMDocumentKey> docKeys = xddCache.keySet(clp);
		List<XDMDocumentId> result = new ArrayList<>(docKeys.size());
		for (XDMDocumentKey key: docKeys) {
			result.add(new XDMDocumentId(key.getKey()));
		}
		return result;
	}

	Collection<Long> getCollectionDocumentKeys(int collectId) {
		//
		Predicate<XDMDocumentKey, XDMDocument> clp = new CollectionPredicate(collectId);
		Set<XDMDocumentKey> docKeys = xddCache.keySet(clp);
		List<Long> result = new ArrayList<>(docKeys.size());
		for (XDMDocumentKey key: docKeys) {
			result.add(key.getKey());
		}
		return result;
	}
	
	@Override
	public void removeCollectionDocuments(int collectId) throws XDMException {
		logger.trace("removeCollectionDocuments.enter; collectId: {}", collectId);
		int cnt = 0;
		Collection<XDMDocumentId> docIds = getCollectionDocumentIds(collectId);
		for (XDMDocumentId docId: docIds) {
			removeDocument(docId);
			cnt++;
		}
		logger.trace("removeCollectionDocuments.exit; removed: {}", cnt);
	}
	
	@Override
	public int addDocumentToCollections(XDMDocumentId docId, int[] collectIds) {
		logger.trace("addDocumentsToCollections.enter; got docId: {}; collectIds: {}", docId, Arrays.toString(collectIds));
		int addCount = 0;
		int unkCount = 0;
		XDMDocument doc = getDocument(docId);
		if (doc != null) {
			// TODO: cache size in the doc itself?
			int size = 0;
			for (XDMCollection cln: repo.getSchema().getCollections()) {
				for (int collectId: collectIds) {
					if (collectId == cln.getId()) {
						if (doc.addCollection(collectId)) {
							addCount++;
							updateStats(cln.getName(), true, size, doc.getFragments().length);
						}						
						break;
					}
				}
			}
			if (addCount > 0) {
				xddCache.put(factory.newXDMDocumentKey(docId.getDocumentKey()), doc);
			}
		} else {
			unkCount++;
		}
		logger.trace("addDocumentsToCollections.exit; added: {}; unknown: {}", addCount, unkCount);
		return addCount;
	}

	@Override
	public int removeDocumentFromCollections(XDMDocumentId docId, int[] collectIds) {
		logger.trace("removeDocumentsFromCollections.enter; got docId: {}; collectIds: {}", docId, Arrays.toString(collectIds));
		int remCount = 0;
		int unkCount = 0;
		XDMDocument doc = getDocument(docId);
		if (doc != null) {
			int size = 0;
			for (XDMCollection cln: repo.getSchema().getCollections()) {
				for (int collectId: collectIds) {
					if (collectId == cln.getId()) {
						if (doc.removeCollection(collectId)) {
							remCount++;
							updateStats(cln.getName(), false, size, doc.getFragments().length);
						}
						break;
					}
				}
			}
			if (remCount > 0) {
				xddCache.put(factory.newXDMDocumentKey(docId.getDocumentKey()), doc);
			}
		} else {
			unkCount++;
		}
		logger.trace("removeDocumentsFromCollections.exit; removed: {}; unknown: {}", remCount, unkCount);
		return remCount;
	}
	
	private boolean lockDocument(XDMDocumentKey docKey) { //throws XDMException {
		
		boolean locked = false;
		long timeout = txManager.getTransactionTimeout();
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
			if (!queue.offer(new StatisticsEvent(name, add, fragments, elements))) {
				logger.warn("updateStats; queue is full!!");
			}
		}
	}

	public int updateDocumentStats(XDMDocument doc, int[] collectIds, boolean add, int size) {
		int cnt = 0;
		for (XDMCollection cln: repo.getSchema().getCollections()) {
			for (int collectId: collectIds) {
				if (collectId == cln.getId()) {
					updateStats(cln.getName(), add, size, doc.getFragments().length);
					cnt++;
					break;
				}
			}
		}
		return cnt;
	}

}
