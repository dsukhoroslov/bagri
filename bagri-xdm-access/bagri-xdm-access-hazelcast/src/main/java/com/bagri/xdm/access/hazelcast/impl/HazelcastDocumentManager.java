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
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PV_MODE_SERVER;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMDocumentManagerClient;
import com.bagri.xdm.access.hazelcast.process.CommandExecutor;
import com.bagri.xdm.access.hazelcast.process.DocumentBuilder;
import com.bagri.xdm.access.hazelcast.process.DocumentCreator;
import com.bagri.xdm.access.hazelcast.process.DocumentRemover;
import com.bagri.xdm.access.hazelcast.process.QueryExecutor;
import com.bagri.xdm.access.hazelcast.process.SchemaStatsAggregator;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
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
import com.hazelcast.security.UsernamePasswordCredentials;

public class HazelcastDocumentManager extends XDMDocumentManagerClient {

	private String schemaName;
	private IMap<String, XDMDocument> xddCache;
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
		logger.trace("<init>; HZ: {}", hzInstance); 
		schemaName = ((com.hazelcast.client.HazelcastClientProxy) hzInstance).getClientConfig().getGroupConfig().getName();
		initializeServices();
	}
	
	@Override
	public void close() {
		Hazelcast.shutdownAll();
	}
	
	public HazelcastInstance getHzInstance() {
		return hzInstance;
	}
	
    public int getXddSize() {
    	return xddCache.size();
    }
    
    public int getXdmSize() {
    	return xdmCache.size();
    }
    
    public long getSchemaSize() {
    	
		long stamp = System.currentTimeMillis();
		logger.trace("getSchemaSize.enter;");
		
		SchemaStatsAggregator task = new SchemaStatsAggregator();
		Map<Member, Future<Long>> results = execService.submitToAllMembers(task);
		long fullSize = 0;
		for (Map.Entry<Member, Future<Long>> entry: results.entrySet()) {
			try {
				Long size = entry.getValue().get();
				fullSize += size;
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getSchemaSize.error; ", ex);
			}
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getSchemaSize.exit; returning: {}; timeTaken: {}", fullSize, stamp);
    	return fullSize;
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
	
	IMap<String, XDMDocument> getDocumentCache() {
		return xddCache;
	}

	//@Override
	//public Long getDocumentId(String uri) {
   	//	Predicate f = Predicates.equal("uri", uri);
	//	Set<Long> docKeys = xddCache.keySet(f);
	//	if (docKeys.size() == 0) {
	//		return null;
	//	}
		// todo: check if too many docs ??
	//	return docKeys.iterator().next();
	//}
	
	//@Override
	//public XDMDocument getDocument(long docId) {
	//	return xddCache.get(docId);
	//}
	
	@Override
	public XDMDocument getDocument(String uri) {
		uri = FileUtils.uri2Path(uri);
		return xddCache.get(uri);
	}

	@Override
	public XDMDocument storeDocument(String xml) {

		long stamp = System.currentTimeMillis();
		logger.trace("storeDocument.enter; xml: {}", xml.length());
		long docId = docGen.next();
		String uri = "/library/" + docId;
		//xddCache.put(docId, null);
		// @TODO: get current user from somewhere
		xddCache.put(uri, new XDMDocument(docId, uri, 0, "system"));
		logger.trace("storeDocument; document initialized: {}", docId);
		
		DocumentCreator task = new DocumentCreator(docId, uri, xml);
		Future<XDMDocument> future = execService.submitToKeyOwner(task, docId);
		logger.trace("storeDocument; the task submit; feature: {}", future);
		XDMDocument result;
		try {
			result = future.get();
			logger.trace("storeDocument.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			return (XDMDocument) result;
		} catch (InterruptedException | ExecutionException ex) {
			// the document could be stored anyway..
			logger.error("storeDocument: exception", ex);
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
	public void removeDocument(String uri) {
		
		long stamp = System.currentTimeMillis();
		logger.trace("removeDocument.enter; uri: {}", uri);
		//XDMDocumentRemover proc = new XDMDocumentRemover();
		//Object result = xddCache.executeOnKey(docId, proc);
		
		DocumentRemover task = new DocumentRemover(uri);
		Future<XDMDocument> future = execService.submitToKeyOwner(task, uri);
		logger.trace("removeDocument; the task submit; feature: {}", future);
		XDMDocument result;
		try {
			result = future.get();
			logger.trace("removeDocument.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			//return (XDMDocument) result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("removeDocument: ", ex);
		}
	}
	
	@Override
	public Collection<String> getXML(ExpressionBuilder query, String template, Map params) {
		long stamp = System.currentTimeMillis();
		
		Collection<String> uris = getDocumentURIs(query);
		if (uris.size() > 0) {
			DocumentBuilder tt = new DocumentBuilder(query.getRoot().getDocType(), template, uris, params);
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
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("getXml; error getting result", ex);
				}
			}
			return xmls;
		}
		return Collections.emptyList();
	}
	
	@Override
	public String getDocumentAsString(String uri) {
		
		long stamp = System.currentTimeMillis();
		XDMDocument xdoc = getDocument(uri);
		if (xdoc == null) {
			return "ERROR: No document found for URI '" + uri + "'";
		}
		
		int docType = xdoc.getTypeId();
		String path = mDictionary.getDocumentRoot(docType);

		Map<String, String> params = new HashMap<String, String>();
		params.put(":doc", path);

		DocumentBuilder tt = new DocumentBuilder(docType, ":doc", Collections.singleton(uri), params);
		Map<Member, Future<Collection<String>>> result = execService.submitToAllMembers(tt);

		for (Future<Collection<String>> future: result.values()) {
			try {
				Collection<String> c = future.get();
				if (c.isEmpty()) {
					continue;
				}
				logger.trace("getDocumentAsString.exit; got template results: {}; time taken {}", c.size(), System.currentTimeMillis() - stamp);
				return c.iterator().next();
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getDocumentAsString; error getting result", ex);
			}
		}			
		return null;
	}

	@Override
	public Object executeXCommand(String command, Map bindings, Properties props) {

		long stamp = System.currentTimeMillis();
		logger.trace("executeXCommand.enter; command: {}; bindings: {}; context: {}", command, bindings, props);
		
		CommandExecutor task = new CommandExecutor(command, bindings, props);
		Future<Object> future = execService.submit(task);
		Object result = null;
		try {
			result = future.get();
			logger.trace("executeXCommand.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			//return (XDMDocument) result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("executeXCommand.error; error getting result", ex);
		}
		return result;
	}

	@Override
	public Object executeXQuery(String query, Map bindings, Properties props) {

		long stamp = System.currentTimeMillis();
		logger.trace("executeXQuery.enter; query: {}; bindings: {}; context: {}; schea: {}", 
				query, bindings, props, schemaName);
		
		QueryExecutor task = new QueryExecutor(schemaName, query, bindings, props);
		Future<Object> future = execService.submit(task);
		Object result = null;
		// @TODO: get timeout from XQJ context
		long timeout = 30;
		try {
			HazelcastXQCursor cursor;
			if (timeout > 0) {
				cursor = (HazelcastXQCursor) future.get(timeout, TimeUnit.SECONDS);
			} else {
				cursor = (HazelcastXQCursor) future.get();
			}
			cursor.deserialize(hzInstance);
			result = cursor;
			logger.trace("executeXQuery.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			//return (XDMDocument) result;
		} catch (TimeoutException ex) {
			future.cancel(true);
			logger.warn("executeXQuery.error; query timed out", ex);
		} catch (InterruptedException | ExecutionException ex) {
			// cancel feature ??
			logger.error("executeXQuery.error; error getting result", ex);
		}
		return result; 
	}

	@Override
	public Collection<String> getDocumentURIs(ExpressionBuilder query) {
		// moved all this processing to the server side!!
		return null;
	}
	
}
