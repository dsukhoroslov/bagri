package com.bagri.xdm.access.hazelcast.impl;

import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_DOCTYPE_DICT;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_ELEMENT;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_NAMESPACE_DICT;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_PATH_DICT;
import static com.bagri.xdm.access.api.XDMCacheConstants.SQN_DOCUMENT;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_CACHE_MODE;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SCHEMA_NAME;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SCHEMA_PASS;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SERVER_ADDRESS;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PV_MODE_CLIENT;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PV_MODE_SERVER;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.PathExpression;
import com.bagri.xdm.access.api.XDMDocumentManagerClient;
import com.bagri.xdm.access.hazelcast.process.DocumentBuilder;
import com.bagri.xdm.access.hazelcast.process.DocumentCreator;
import com.bagri.xdm.access.hazelcast.process.DocumentRemover;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.security.UsernamePasswordCredentials;

public class HazelcastDocumentManager extends XDMDocumentManagerClient {

	private IMap<Long, XDMDocument> xddCache;
	private IMap<XDMDataKey, XDMElement> xdmCache;
	private IdGenerator<Long> docGen;
	protected IExecutorService execService;
	
	//@Autowired
	private HazelcastInstance hzInstance;
	
	public HazelcastDocumentManager() {
		super();
		Properties defaults = getSystemProps();
		initializeHazelcast(defaults);
		mFactory = new HazelcastXDMFactory();
		mDictionary = new HazelcastSchemaDictionary(hzInstance);
		initializeServices();
	}

	public HazelcastDocumentManager(Properties props) {
		super();
		Properties converted = getConvertedProps(props);
		initializeHazelcast(converted);
		mFactory = new HazelcastXDMFactory();
		mDictionary = new HazelcastSchemaDictionary(hzInstance);
		initializeServices();
	}
	
	public HazelcastDocumentManager(HazelcastInstance hzInstance) {
		super();
		this.hzInstance = hzInstance;
		initializeServices();
	}
	
	@Override
	public void close() {
		Hazelcast.shutdownAll();
	}
	
	public HazelcastInstance getHzInstance() {
		return hzInstance;
	}
	
	private void loadCache(IMap cache) {
		long stamp = System.currentTimeMillis();
		Set keys = cache.keySet();
		for (Object key: keys) {
			cache.get(key);
		}
		logger.debug("loadCache; cache: {}, time taken: {}", cache, System.currentTimeMillis() - stamp);
	}
	
	private String getProperty(String name, String fallback) {
		String prop = System.getProperty(name);
		if (prop == null) {
			prop = System.getProperty(fallback);
		}
		return prop;
	}
	
	private void setProperty(Properties source, Properties target, String name, String fallback) {
		String prop = source.getProperty(name);
		if (prop == null && fallback != null) {
			prop = source.getProperty(fallback);
		}
		
		if (prop != null) {
			target.setProperty(name, prop);
		}
	}
	
	private Properties getSystemProps() {
		Properties props = new Properties();
		props.setProperty(PN_CACHE_MODE, System.getProperty(PN_CACHE_MODE, "client"));
		props.setProperty(PN_SCHEMA_NAME, getProperty(PN_SCHEMA_NAME, "schema"));
		props.setProperty(PN_SCHEMA_PASS, getProperty(PN_SCHEMA_PASS, "password"));
		props.setProperty(PN_SERVER_ADDRESS, getProperty(PN_SERVER_ADDRESS, "address"));
		return props;
	}
	
	private Properties getConvertedProps(Properties original) {
		Properties props = new Properties();
		setProperty(original, props, PN_CACHE_MODE, null);
		setProperty(original, props, PN_SCHEMA_NAME, "schema");
		setProperty(original, props, PN_SCHEMA_PASS, "password");
		setProperty(original, props, PN_SERVER_ADDRESS, "address");
		return props;
	}
	
	private void initializeHazelcast(Properties props) {
		String mode = props.getProperty(PN_CACHE_MODE);
		if (PV_MODE_SERVER.equalsIgnoreCase(mode)) {
			Config config = new ClasspathXmlConfig("hazelcast/hazelcast-server.xml");
			hzInstance = Hazelcast.newHazelcastInstance(config);
		} else {
			String schema = props.getProperty(PN_SCHEMA_NAME);
			String password = props.getProperty(PN_SCHEMA_PASS);
			String address = props.getProperty(PN_SERVER_ADDRESS);
			InputStream in = HazelcastDocumentManager.class.getResourceAsStream("/hazelcast/hazelcast-client.xml");
			ClientConfig config = new XmlClientConfigBuilder(in).build();
			config.getGroupConfig().setName(schema);
			config.getGroupConfig().setPassword(password);
			config.getNetworkConfig().addAddress(address);
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(schema, password);
			config.getSecurityConfig().setCredentials(creds);
			hzInstance = HazelcastClient.newHazelcastClient(config);
		}
	}

	private void initializeServices() {
		xddCache = hzInstance.getMap(CN_XDM_DOCUMENT);
		xdmCache = hzInstance.getMap(CN_XDM_ELEMENT);
		execService = hzInstance.getExecutorService("xdm-exec-pool");
		docGen = new HazelcastIdGenerator(hzInstance.getIdGenerator(SQN_DOCUMENT));
		
		String mode = System.getProperty(PN_CACHE_MODE);
		if (!(PV_MODE_SERVER.equalsIgnoreCase(mode))) {
			loadCache(hzInstance.getMap(CN_XDM_PATH_DICT)); 
			loadCache(hzInstance.getMap(CN_XDM_NAMESPACE_DICT)); 
			loadCache(hzInstance.getMap(CN_XDM_DOCTYPE_DICT));
		}
	}
	
	IMap<XDMDataKey, XDMElement> getDataCache() {
		return xdmCache;
	}
	
	IMap<Long, XDMDocument> getDocumentCache() {
		return xddCache;
	}

	@Override
	public Long getDocumentId(String uri) {
   		Predicate f = Predicates.equal("uri", uri);
		Set<Long> docKeys = xddCache.keySet(f);
		if (docKeys.size() == 0) {
			return null;
		}
		// todo: check if too many docs ??
		return docKeys.iterator().next();
	}
	
	@Override
	public XDMDocument getDocument(long docId) {
		return xddCache.get(docId);
	}
	
	@Override
	public XDMDocument storeDocument(String xml) {

		long stamp = System.currentTimeMillis();
		logger.trace("storeDocument.enter; xml: {}", xml.length());
		long docId = docGen.next();
		String uri = "/library/" + docId;
		//xddCache.put(docId, null);
		xddCache.put(docId, new XDMDocument(docId, uri, 0));
		logger.trace("storeDocument; document initialized: {}", docId);
		
		DocumentCreator task = new DocumentCreator(docId, uri, xml);
		Future<XDMDocument> future = execService.submitToKeyOwner(task, docId);
		logger.trace("storeDocument; the task submit; feature: {}", future);
		XDMDocument result;
		try {
			result = future.get();
			logger.trace("storeDocument.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			return (XDMDocument) result;
		//} catch (InterruptedException | ExecutionException ex) {
			// the document could be stored anyway..
		//	logger.error("storeDocument: exception", ex);
		} catch (Throwable ex) {
			// the document was not stored..
			xddCache.remove(docId);
			logger.error("storeDocument: removed; throwable", ex);
		}
		return null;
	}

	@Override
	public XDMDocument storeDocument(String uri, String xml) {
		
		// todo: override existing document -> create a new version ?
		return null;
	}
	
	@Override
	public void removeDocument(long docId) {
		
		long stamp = System.currentTimeMillis();
		logger.trace("removeDocument.enter; docId: {}", docId);
		//XDMDocumentRemover proc = new XDMDocumentRemover();
		//Object result = xddCache.executeOnKey(docId, proc);
		
		DocumentRemover task = new DocumentRemover(docId);
		Future<XDMDocument> future = execService.submitToKeyOwner(task, docId);
		logger.trace("removeDocument; the task submit; feature: {}", future);
		XDMDocument result;
		try {
			result = future.get();
			logger.trace("removeDocument.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			//return (XDMDocument) result;
		} catch (Throwable ex) {
			logger.error("removeDocument: ", ex);
		}
		
		/*
	    boolean removed = false;
	    if (xddCache.remove(docId) != null) {
	    
	   		Predicate f = Predicates.equal("documentId", docId);
			Set<XDMDataKey> xdmKeys = xdmCache.keySet(f);
			logger.trace("process; got {} document elements to remove", xdmKeys.size());
			int cnt = 0;
	        for (XDMDataKey key: xdmKeys) {
	        	//xdmCache.delete(key);
	        	DataDocumentKey ddk; //) key).
	        	if (xdmCache.remove(key) != null) {
	        		cnt++;
	        	} else {
	    			logger.trace("process; data not found for key {}", key);
	    			logger.trace("process; get returns: {}", xdmCache.get(key));
	        	}
	        }
			logger.trace("process; {} document elements were removed", cnt);
	        removed = true;
	    }
        //xddCache.delete(docEntry.getKey());
		*/
	}
	
	@Override
	public Collection<String> getXML(ExpressionBuilder query, String template, Map params) {
		long stamp = System.currentTimeMillis();
		
		Collection<Long> docIds = getDocumentIDs(query);
		if (docIds.size() > 0) {
			DocumentBuilder tt = new DocumentBuilder(query.getRoot().getDocType(), template, docIds, params);
			Map<Member, Future<Collection<String>>> result = execService.submitToAllMembers(tt);

			Collection<String> xmls = new ArrayList<String>();
			for (Future<Collection<String>> future: result.values()) {
				try {
					Collection<String> c = future.get();
					if (c.isEmpty()) {
						continue;
					}
					logger.trace("getXml.exit; got template results: {}; time taken {}", c, System.currentTimeMillis() - stamp);
					xmls.addAll(c);
				} catch (Exception ex) { //InterruptedException | ExecutionException ex) {
					logger.error("getXml; error getting result", ex);
				}
			}
			return xmls;
		}
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public String getDocumentAsString(long docId) {
		
		long stamp = System.currentTimeMillis();
		XDMDocument xdoc = (XDMDocument) xddCache.get(docId);
		int docType = xdoc.getTypeId();
		String path = mDictionary.getDocumentRoot(docType);

		Map<String, String> params = new HashMap<String, String>();
		params.put(":doc", path);

		DocumentBuilder tt = new DocumentBuilder(docType, ":doc", Collections.singleton(docId), params);
		Map<Member, Future<Collection<String>>> result = execService.submitToAllMembers(tt);

		for (Future<Collection<String>> future: result.values()) {
			try {
				Collection<String> c = future.get();
				if (c.isEmpty()) {
					continue;
				}
				logger.trace("getDocumentAsString.exit; got template results: {}; time taken {}", c, System.currentTimeMillis() - stamp);
				return c.iterator().next();
			} catch (Exception ex) { //InterruptedException | ExecutionException ex) {
				logger.error("getDocumentAsString; error getting result", ex);
			}
		}			
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	private Predicate getValueFilter(PathExpression pex) {
		String field = "value";
		Object value = pex.getValue();
		if (value instanceof Integer) {
			field = "asInt"; 
		} else if (value instanceof Long) {
			field = "asLong";
		} else if (value instanceof Boolean) {
			field = "asBoolean";
		} else if (value instanceof Byte) {
			field = "asByte";
		} else if (value instanceof Short) {
			field = "asShort";
		} else if (value instanceof Float) {
			field = "asFloat";
		} else if (value instanceof Double) {
			field = "asDouble";
		} else {
			value = value.toString();
		}
	
		switch (pex.getCompType()) {
			case EQ: return Predicates.equal(field, (Comparable) value);
			case LE: return Predicates.lessEqual(field, (Comparable) value);
			case LT: return Predicates.lessThan(field, (Comparable) value);
			case GE: return Predicates.greaterEqual(field, (Comparable) value);
			case GT: return Predicates.greaterThan(field, (Comparable) value);
			default: return null;
		}
		
	}
	
	@Override
	protected Set<Long> queryPathKeys(Set<Long> found, PathExpression pex) {

		int pathId = -1;
		if (pex.isRegex()) {
			Set<Integer> pathIds = mDictionary.translatePathFromRegex(pex.getDocType(), pex.getRegex());
			logger.trace("queryPathKeys; regex: {}; pathIds: {}", pex.getRegex(), pathIds);
			if (pathIds.size() > 0) {
				pathId = pathIds.iterator().next();
			}
		} else {
			String path = pex.getFullPath();
			logger.trace("queryPathKeys; path: {}; comparison: {}", path, pex.getCompType());
			pathId = mDictionary.translatePath(pex.getDocType(), path, XDMNodeKind.fromPath(path));
		}
		String value = pex.getValue().toString();
		Predicate valueFilter = getValueFilter(pex);
		if (valueFilter == null) {
			throw new IllegalArgumentException("Can't construct filter for expression: " + pex);
		}

		Predicate f = Predicates.and(Predicates.equal("pathId", pathId), valueFilter);
		Set<XDMDataKey> keys = xdmCache.keySet(f);
		logger.trace("queryPathKeys; path: {}, value: {}; got keys: {}; cache size: {}", 
				new Object[] {pathId, value, keys.size(), xdmCache.size()}); 
		
		if (keys.size() > 0) {
			Set<Long> docIds = new HashSet<Long>();
			for (XDMDataKey key: keys) {
				docIds.add(key.getDocumentId());
			}
			found.retainAll(docIds);
		} else {
			found.clear();
		}
		return found;
	}
	
	@Override
	public Collection<Long> getDocumentIDs(ExpressionBuilder query) {
		if (query.getRoot() != null) {
			Predicate f = Predicates.equal("typeId", query.getRoot().getDocType());
			//Set<Long> keys = new HashSet<Long>(xddCache.keySet(f));
			Set<Long> keys = xddCache.keySet(f);
			return queryKeys(keys, query.getRoot());
		} else {
			return xddCache.keySet();
		}
	}
	
	
}
