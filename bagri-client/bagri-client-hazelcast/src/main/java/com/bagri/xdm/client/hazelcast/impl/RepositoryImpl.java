package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.common.util.PropUtils.getSystemProperty;
import static com.bagri.common.util.PropUtils.setProperty;
import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.client.common.impl.XDMRepositoryBase;
import com.bagri.xdm.client.hazelcast.serialize.XQItemSerializer;
import com.bagri.xdm.client.hazelcast.serialize.XQItemTypeSerializer;
import com.bagri.xdm.client.hazelcast.serialize.XQSequenceSerializer;
import com.bagri.xdm.client.hazelcast.task.query.XQCommandExecutor;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public class RepositoryImpl extends XDMRepositoryBase implements XDMRepository {
	
    private final static Logger logger = LoggerFactory.getLogger(RepositoryImpl.class);
	
	public static final String PN_POOL_SIZE = "hz.pool.size";
	public static final String PN_SCHEMA_NAME = "hz.schema.name";
	public static final String PN_SCHEMA_PASS = "hz.schema.password";
	public static final String PN_SERVER_ADDRESS = "hz.server.address";
	public static final String PN_CLIENT_SMART = "hz.client.smart";
    
	private String clientId;
	private String schemaName;
	
	private ResultsIterator cursor;
	private HazelcastInstance hzClient;
	private IExecutorService execService;
	

	public RepositoryImpl() {
		initializeFromProperties(getSystemProps());
	}

	public RepositoryImpl(Properties props) {
		initializeFromProperties(getConvertedProps(props));
	}
	
	public RepositoryImpl(HazelcastInstance hzInstance) {
		this.hzClient = hzInstance;
		com.hazelcast.client.HazelcastClientProxy proxy = (com.hazelcast.client.HazelcastClientProxy) hzClient; 
		schemaName = proxy.getClientConfig().getGroupConfig().getName();
		clientId = proxy.getLocalEndpoint().getUuid();
		logger.trace("<init>; connected to HZ server as: {}; {}", clientId, proxy);
		initializeServices();
	}
	
	private static Properties getSystemProps() {
		Properties props = new Properties();
		props.setProperty(PN_SCHEMA_NAME, getSystemProperty(PN_SCHEMA_NAME, "schema"));
		props.setProperty(PN_SCHEMA_PASS, getSystemProperty(PN_SCHEMA_PASS, "password"));
		props.setProperty(PN_SERVER_ADDRESS, getSystemProperty(PN_SERVER_ADDRESS, "address"));
		props.setProperty(PN_CLIENT_SMART, getSystemProperty(PN_CLIENT_SMART, "false"));
		return props;
	}
	
	private static Properties getConvertedProps(Properties original) {
		Properties props = new Properties();
		setProperty(original, props, PN_SCHEMA_NAME, "schema");
		setProperty(original, props, PN_SCHEMA_PASS, "password");
		setProperty(original, props, PN_SERVER_ADDRESS, "address");
		setProperty(original, props, PN_CLIENT_SMART, "false");
		props.put("xqDataFactory", original.get("xqDataFactory"));
		return props;
	}
	
	private void initializeFromProperties(Properties props) {
		initializeHazelcast(props);
		initializeServices();
	}
	
	private void initializeHazelcast(Properties props) {
		String schema = props.getProperty(PN_SCHEMA_NAME);
		String password = props.getProperty(PN_SCHEMA_PASS);
		String address = props.getProperty(PN_SERVER_ADDRESS);
		String smart = props.getProperty(PN_CLIENT_SMART);
		InputStream in = DocumentManagementImpl.class.getResourceAsStream("/hazelcast/hazelcast-client.xml");
		ClientConfig config = new XmlClientConfigBuilder(in).build();
		config.getGroupConfig().setName(schema);
		config.getGroupConfig().setPassword(password);
		config.getNetworkConfig().addAddress(address);
		config.getNetworkConfig().setSmartRouting(smart.equalsIgnoreCase("true"));
		//config.setProperty("hazelcast.logging.type", "slf4j");
		//UsernamePasswordCredentials creds = new UsernamePasswordCredentials(schema, password);
		//SecureCredentials creds = new SecureCredentials(password);
		//config.getSecurityConfig().setCredentials(creds);
		//config.setCredentials(creds);
			
		XQDataFactory xqFactory = (XQDataFactory) props.get("xqDataFactory");
		if (xqFactory != null) {
			XQItemTypeSerializer xqits = new XQItemTypeSerializer();
			xqits.setXQDataFactory(xqFactory);
			config.getSerializationConfig().getSerializerConfigs().add(
					new SerializerConfig().setTypeClass(XQItemType.class).setImplementation(xqits));

			XQItemSerializer xqis = new XQItemSerializer();
			xqis.setXQDataFactory(xqFactory);
			config.getSerializationConfig().getSerializerConfigs().add(
					new SerializerConfig().setTypeClass(XQItem.class).setImplementation(xqis));
				
			XQSequenceSerializer xqss = new XQSequenceSerializer();
			xqss.setXQDataFactory(xqFactory);
			config.getSerializationConfig().getSerializerConfigs().add(
					new SerializerConfig().setTypeClass(XQSequence.class).setImplementation(xqss));
		}
		logger.debug("initializeHazelcast; config: {}", config);
		hzClient = HazelcastClient.newHazelcastClient(config);
		//logger.debug("initializeHazelcast; got HZ: {}", hzInstance);
		com.hazelcast.client.HazelcastClientProxy proxy = (com.hazelcast.client.HazelcastClientProxy) hzClient; 
		schemaName = proxy.getClientConfig().getGroupConfig().getName();
		clientId = proxy.getLocalEndpoint().getUuid();
		logger.trace("initializeHazelcast; connected to HZ server as: {}", clientId);
	}

	private void initializeServices() {
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		DocumentManagementImpl docMgr = new DocumentManagementImpl();
		setDocumentManagement(docMgr);
		QueryManagementImpl queryMgr = new QueryManagementImpl();
		setQueryManagement(queryMgr);
		ModelManagementImpl modelMgr = new ModelManagementImpl();
		setModelManagement(modelMgr);
	}

	@Override
	public void setDocumentManagement(XDMDocumentManagement docMgr) {
		super.setDocumentManagement(docMgr);
		((DocumentManagementImpl) docMgr).initialize(this);
	}

	@Override
	public void setQueryManagement(XDMQueryManagement queryMgr) {
		super.setQueryManagement(queryMgr);
		((QueryManagementImpl) queryMgr).initialize(this);
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
		
			// destroy result queue!?
			//if (cursor != null) {
			//	cursor.close(true);
			//}
			
			//List<Runnable> lostTasks = execService.shutdownNow();
			//if (lostTasks != null && lostTasks.size() > 0) {
			//	logger.info("close; {} tasks were lost in executor service queue", lostTasks.size());
			//}
			
			hzClient.getLifecycleService().shutdown();
			// probably, should do something like this:
			//execService.awaitTermination(100, TimeUnit.SECONDS);
		} else {
			logger.info("close; an attempt to close not-running client!");
		}
		logger.trace("close.exit;");
	}
	
	HazelcastInstance getHazelcastClient() {
		return hzClient;
	}
	
	Iterator execXQuery(boolean isQuery, String query, Map bindings, Properties props) { //throws Exception {
		
		//if (logger.isTraceEnabled()) {
		//	for (Object o: bindings.entrySet()) {
		//		Map.Entry e = (Map.Entry) o;
		//		logger.trace("execXQuery.binding; {}:{}; {}:{}", e.getKey().getClass().getName(),
		//				e.getKey(), e.getValue().getClass().getName(), e.getValue());
		//	}
		//}
		
		props.put("clientId", clientId);
		//props.put("batchSize", "5");
		
		String runOn = System.getProperty("xdm.client.submitTo", "any");
		
		XQCommandExecutor task = new XQCommandExecutor(isQuery, schemaName, query, bindings, props);
		Future<Object> future;
		if (isQuery) {
			if ("owner".equals(runOn)) {
				int key = getQueryManagement().getQueryKey(query);
				future = execService.submitToKeyOwner(task, key);
			} else if ("member".equals(runOn)) {
				int key = getQueryManagement().getQueryKey(query);
				Member member = hzClient.getPartitionService().getPartition(key).getOwner();
				future = execService.submitToMember(task, member);
			} else {
				future = execService.submit(task);
			}
		} else {
			future = execService.submit(task);
		}

		Iterator result = null;
		long timeout = Long.parseLong(props.getProperty("timeout", "0"));
		int fetchSize = Integer.parseInt(props.getProperty("batchSize", "0"));
		try {
			//if (cursor != null) {
			//	cursor.close(false);
			//}
			
			if (timeout > 0) {
				cursor = (ResultsIterator) future.get(timeout, TimeUnit.SECONDS);
			} else {
				cursor = (ResultsIterator) future.get();
			}
			
			logger.trace("execXQuery; got cursor: {}", cursor);
			if (cursor != null) {
				cursor.deserialize(hzClient);

				if (cursor.isFailure()) {
					//Exception ex = (Exception) cursor.next();
					//throw ex;
					while (cursor.hasNext()) {
						Object err = cursor.next();
						if (err instanceof String) {
							throw new RuntimeException((String) err);
						}
					}
				}
			}
			
			if (fetchSize == 0) {
				result = extractFromCursor(cursor);
			} else {
				result = cursor;
			}
		} catch (TimeoutException ex) {
			future.cancel(true);
			logger.warn("execXQuery.error; query timed out", ex);
		} catch (InterruptedException | ExecutionException ex) {
			// cancel future ??
			logger.error("execXQuery.error; error getting result", ex);
		}
		return result; 
	}
	
	private Iterator extractFromCursor(ResultsIterator cursor) {
		List result = new ArrayList();
		while (cursor.hasNext()) {
			result.add(cursor.next());
		}
		return result.iterator();
	}


}
