package com.bagri.xdm.access.hazelcast.impl;

import static com.bagri.xdm.access.api.XDMCacheConstants.*;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.*;
import static com.bagri.common.util.PropUtils.getSystemProperty;
import static com.bagri.common.util.PropUtils.setProperty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.xdm.access.api.XDMDocumentManagementClient;
import com.bagri.xdm.access.hazelcast.pof.XQItemSerializer;
import com.bagri.xdm.access.hazelcast.pof.XQItemTypeSerializer;
import com.bagri.xdm.access.hazelcast.process.DocumentBuilder;
import com.bagri.xdm.access.hazelcast.process.DocumentCreator;
import com.bagri.xdm.access.hazelcast.process.DocumentRemover;
import com.bagri.xdm.access.hazelcast.process.DocumentIdsProvider;
import com.bagri.xdm.access.hazelcast.process.DocumentUrisProvider;
import com.bagri.xdm.access.hazelcast.process.XMLBuilder;
import com.bagri.xdm.access.hazelcast.process.XMLProvider;
import com.bagri.xdm.access.hazelcast.process.XQCommandExecutor;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.security.UsernamePasswordCredentials;

public class DocumentManagementClient extends XDMDocumentManagementClient {

	private String clientId;
	private String schemaName;
	private IMap<Long, XDMDocument> xddCache;
	private IMap<XDMDataKey, XDMElements> xdmCache;
	private IdGenerator<Long> docGen;
	private IExecutorService execService;
	
	private HazelcastInstance hzClient;
	private HazelcastXQCursor cursor;
	
	public DocumentManagementClient() {
		super();
		initializeFromProperties(getSystemProps());
	}

	public DocumentManagementClient(Properties props) {
		super();
		initializeFromProperties(getConvertedProps(props));
	}
	
	public DocumentManagementClient(HazelcastInstance hzInstance) {
		super();
		this.hzClient = hzInstance;
		com.hazelcast.client.HazelcastClientProxy proxy = (com.hazelcast.client.HazelcastClientProxy) hzClient; 
		schemaName = proxy.getClientConfig().getGroupConfig().getName();
		clientId = proxy.getLocalEndpoint().getUuid();
		logger.trace("<init>; connected to HZ server as: {}; {}", clientId, proxy);
		initializeServices();
	}
	
	@Override
	public void close() {
		logger.trace("close.enter;");
		if (hzClient.getLifecycleService().isRunning()) {
			//try {
			//	Thread.sleep(1000);
			//} catch (InterruptedException ex) {
			//	logger.info("close; interrupted: {}", ex);
			//}
			// destroy result queue!
			if (cursor != null) {
				cursor.close(true);
			}
			
			//List<Runnable> lostTasks = execService.shutdownNow();
			//if (lostTasks != null && lostTasks.size() > 0) {
			//	logger.info("close; {} tasks were lost in executor service queue", lostTasks.size());
			//}
			
			hzClient.getLifecycleService().shutdown();
			// probably, should do somethiong like this:
			//hzInstance.getExecutorService(PN_SCHEMA_NAME).awaitTermination(100, TimeUnit.SECONDS);
		} else {
			logger.info("close; an attempt to close not-running client!");
		}
		logger.trace("close.exit;");
	}
	
	public HazelcastInstance getHzInstance() {
		return hzClient;
	}
	
    public int getXddSize() {
    	return xddCache.size();
    }
    
    public int getXdmSize() {
    	return xdmCache.size();
    }
    
	private void loadCache(IMap cache) {
		long stamp = System.currentTimeMillis();
		Set keys = cache.keySet();
		for (Object key: keys) {
			cache.get(key);
		}
		logger.debug("loadCache; cache: {}, time taken: {}", cache, System.currentTimeMillis() - stamp);
	}
	
	private static Properties getSystemProps() {
		Properties props = new Properties();
		props.setProperty(PN_CACHE_MODE, System.getProperty(PN_CACHE_MODE, "client"));
		props.setProperty(PN_SCHEMA_NAME, getSystemProperty(PN_SCHEMA_NAME, "schema"));
		props.setProperty(PN_SCHEMA_PASS, getSystemProperty(PN_SCHEMA_PASS, "password"));
		props.setProperty(PN_SERVER_ADDRESS, getSystemProperty(PN_SERVER_ADDRESS, "address"));
		return props;
	}
	
	private static Properties getConvertedProps(Properties original) {
		Properties props = new Properties();
		setProperty(original, props, PN_CACHE_MODE, null);
		setProperty(original, props, PN_SCHEMA_NAME, "schema");
		setProperty(original, props, PN_SCHEMA_PASS, "password");
		setProperty(original, props, PN_SERVER_ADDRESS, "address");
		props.put("xqDataFactory", original.get("xqDataFactory"));
		return props;
	}
	
	private void initializeFromProperties(Properties props) {
		initializeHazelcast(props);
		mFactory = new HazelcastXDMFactory();
		mDictionary = new HazelcastSchemaDictionary(hzClient);
		initializeServices();
	}
	
	private void initializeHazelcast(Properties props) {
		String mode = props.getProperty(PN_CACHE_MODE);
		if (PV_MODE_SERVER.equalsIgnoreCase(mode)) {
			Config config = new ClasspathXmlConfig("hazelcast/hazelcast-server.xml");
			hzClient = Hazelcast.newHazelcastInstance(config);
		} else {
			String schema = props.getProperty(PN_SCHEMA_NAME);
			String password = props.getProperty(PN_SCHEMA_PASS);
			String address = props.getProperty(PN_SERVER_ADDRESS);
			InputStream in = DocumentManagementClient.class.getResourceAsStream("/hazelcast/hazelcast-client.xml");
			ClientConfig config = new XmlClientConfigBuilder(in).build();
			config.getGroupConfig().setName(schema);
			config.getGroupConfig().setPassword(password);
			config.getNetworkConfig().addAddress(address);
			//UsernamePasswordCredentials creds = new UsernamePasswordCredentials(schema, password);
			//SecureCredentials creds = new SecureCredentials(password);
			//config.getSecurityConfig().setCredentials(creds);
			//config.setCredentials(creds);
			
			XQDataFactory xqFactory = (XQDataFactory) props.get("xqDataFactory");
			if (xqFactory != null) {
				XQItemSerializer xqis = new XQItemSerializer();
				xqis.setXQDataFactory(xqFactory);
				SerializerConfig xqisc = new SerializerConfig();
				xqisc.setTypeClass(XQItem.class);
				xqisc.setImplementation(xqis);
				config.getSerializationConfig().getSerializerConfigs().add(xqisc);
				
				XQItemTypeSerializer xqits = new XQItemTypeSerializer();
				xqits.setXQDataFactory(xqFactory);
				SerializerConfig xqitsc = new SerializerConfig();
				xqitsc.setTypeClass(XQItemType.class);
				xqitsc.setImplementation(xqits);
				config.getSerializationConfig().getSerializerConfigs().add(xqitsc);
			}
			logger.debug("initializeHazelcast; config: {}", config);
			hzClient = HazelcastClient.newHazelcastClient(config);
			//logger.debug("initializeHazelcast; got HZ: {}", hzInstance);
			com.hazelcast.client.HazelcastClientProxy proxy = (com.hazelcast.client.HazelcastClientProxy) hzClient; 
			schemaName = proxy.getClientConfig().getGroupConfig().getName();
			clientId = proxy.getLocalEndpoint().getUuid();
			logger.trace("initializeHazelcast; connected to HZ server as: {}", clientId);
		}
	}

	private void initializeServices() {
		xddCache = hzClient.getMap(CN_XDM_DOCUMENT);
		xdmCache = hzClient.getMap(CN_XDM_ELEMENT);
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		docGen = new HazelcastIdGenerator(hzClient.getAtomicLong(SQN_DOCUMENT));
		
		//String mode = System.getProperty(PN_CACHE_MODE);
		//if (!(PV_MODE_SERVER.equalsIgnoreCase(mode))) {
		//	loadCache(hzClient.getMap(CN_XDM_PATH_DICT)); 
		//	loadCache(hzClient.getMap(CN_XDM_NAMESPACE_DICT)); 
		//	loadCache(hzClient.getMap(CN_XDM_DOCTYPE_DICT));
		//}
	}
	
	IMap<XDMDataKey, XDMElements> getDataCache() {
		return xdmCache;
	}
	
	IMap<Long, XDMDocument> getDocumentCache() {
		return xddCache;
	}

	@Override
	public XDMDocument getDocument(long docId) {
		return xddCache.get(docId);
	}
	
	@Override
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
	public XDMDocument storeDocument(String xml) {

		long docId = docGen.next();
		String uri = "" + docId + ".xml";
		return storeDocument(docId, uri, xml);
	}

	public XDMDocument storeDocument(String uri, String xml) {

		long docId = docGen.next();
		return storeDocument(docId, uri, xml);
	}

	@Override
	public XDMDocument storeDocument(long docId, String uri, String xml) {
		
		// todo: override existing document -> create a new version ?

		logger.trace("storeDocument.enter; docId: {}, uri: {}; xml: {}", docId, uri, xml.length());
		// @TODO: generate uri properly.. depends on Schema settings 
		//String uri = "" + docId + ".xml";
		//xddCache.put(docId, null);
		// @TODO: get current user from somewhere. but we're on the client side here..
		//xddCache.put(docId, new XDMDocument(docId, uri, 0, "system"));
		//logger.trace("storeDocument; document initialized: {}", docId);
		
		DocumentCreator task = new DocumentCreator(docId, uri, xml);
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
		
		DocumentRemover task = new DocumentRemover(docId);
		Future<XDMDocument> future = execService.submitToKeyOwner(task, docId);
		try {
			XDMDocument result = future.get();
			logger.trace("removeDocument.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			//return (XDMDocument) result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("removeDocument.error: ", ex);
		}
	}
	
	@Override
	public String getDocumentAsString(long docId) {
		// actually, I can try just get it from XML cache!
		
		long stamp = System.currentTimeMillis();
		logger.trace("getDocumentAsString.enter; got docId: {}", docId);
		
		String result = null;
		XMLProvider xp = new XMLProvider(docId);
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

	@Override
	public Collection<String> getDocumentURIs(ExpressionContainer query) {

		long stamp = System.currentTimeMillis();
		logger.trace("getDocumentURIs.enter; query: {}", query);
		
		DocumentUrisProvider task = new DocumentUrisProvider(query);
		Future<Collection<String>> future = execService.submit(task);
		Collection<String> result = null;
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentURIs.error; error getting result", ex);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getDocumentURIs.exit; time taken: {}; returning: {}", stamp, result);
		return result;
	}
	
	@Override
	public Collection<Long> getDocumentIDs(ExpressionContainer query) {

		long stamp = System.currentTimeMillis();
		logger.trace("getDocumentIDs.enter; query: {}", query);
		
		DocumentIdsProvider task = new DocumentIdsProvider(query);
		Future<Collection<Long>> future = execService.submit(task);
		Collection<Long> result = null;
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentIDs.error; error getting result", ex);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getDocumentIDs.exit; time taken: {}; returning: {}", stamp, result);
		return result;
	}
	
	@Override
	public Collection<String> getXML(ExpressionContainer query, String template, Map params) {
		long stamp = System.currentTimeMillis();
		logger.trace("getXML.enter; got query: {}; template: {}; params: {}", query, template, params);
		
		XMLBuilder xb = new XMLBuilder(query, template, params);
		Map<Member, Future<Collection<String>>> result = execService.submitToAllMembers(xb);

		Collection<String> xmls = new ArrayList<String>();
		for (Future<Collection<String>> future: result.values()) {
			try {
				Collection<String> c = future.get();
				if (c.isEmpty()) {
					continue;
				}
				xmls.addAll(c);
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getXML; error getting result", ex);
			}
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getXML.exit; got query results: {}; time taken {}", xmls.size(), stamp);
		return xmls;
	}
	
	@Override
	public Object executeXCommand(String command, Map bindings, Properties props) {

		long stamp = System.currentTimeMillis();
		logger.trace("executeXCommand.enter; command: {}; bindings: {}; context: {}", command, bindings, props);
		try {
			Object result = execXQuery(false, command, bindings, props);
			logger.trace("executeXCommand.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			return result;
		} catch (Exception ex) {
			logger.warn("executeXCommand.error; time taken: {}; exception: {}", System.currentTimeMillis() - stamp, ex);
		}
		return null; 
	}

	@Override
	public Object executeXQuery(String query, Map bindings, Properties props) {

		long stamp = System.currentTimeMillis();
		logger.trace("executeXQuery.enter; query: {}; bindings: {}; context: {}", query, bindings, props);
		try {
			Object result = execXQuery(true, query, bindings, props);
			logger.trace("executeXQuery.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			return result; 
		} catch (Exception ex) {
			logger.warn("executeXQuery.error; time taken: {}; exception: {}", System.currentTimeMillis() - stamp, ex);
			ex.printStackTrace();
		}
		return null; 
	}
	
	private Object execXQuery(boolean isQuery, String query, Map bindings, Properties props) throws Exception {
		
		//if (logger.isTraceEnabled()) {
		//	for (Object o: bindings.entrySet()) {
		//		Map.Entry e = (Map.Entry) o;
		//		logger.trace("execXQuery.binding; {}:{}; {}:{}", e.getKey().getClass().getName(),
		//				e.getKey(), e.getValue().getClass().getName(), e.getValue());
		//	}
		//}
		
		props.put("clientId", clientId);
		//props.put("batchSize", "5");
		
		String runOn = System.getProperty("xdm.client.submitTo", "owner");
		
		XQCommandExecutor task = new XQCommandExecutor(isQuery, schemaName, query, bindings, props);
		Future<Object> future;
		if (isQuery) {
			if ("owner".equals(runOn)) {
				int key = getQueryKey(query);
				future = execService.submitToKeyOwner(task, key);
			} else if ("member".equals(runOn)) {
				int key = getQueryKey(query);
				Member member = hzClient.getPartitionService().getPartition(key).getOwner();
				future = execService.submitToMember(task, member);
			} else {
				future = execService.submit(task);
			}
		} else {
			future = execService.submit(task);
		}

		Object result = null;
		long timeout = Long.parseLong(props.getProperty("timeout", "0"));
		try {
			if (cursor != null) {
				cursor.close(false);
			}
			if (timeout > 0) {
				cursor = (HazelcastXQCursor) future.get(timeout, TimeUnit.SECONDS);
			} else {
				cursor = (HazelcastXQCursor) future.get();
			}
			
			if (cursor != null) {
				cursor.deserialize(hzClient);

				if (cursor.isFailure()) {
					Exception ex = (Exception) cursor.next();
					throw ex;
				}
			}
			result = cursor;
		} catch (TimeoutException ex) {
			future.cancel(true);
			logger.warn("execXQuery.error; query timed out", ex);
		} catch (InterruptedException | ExecutionException ex) {
			// cancel future ??
			logger.error("execXQuery.error; error getting result", ex);
		}
		return result; 
	}
	
	private int getQueryKey(String query) {
		// get it form some common service, 
		// QueryManagement most probably
		return query.hashCode();
	}

}
