package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.common.util.PropUtils.getSystemProperty;
import static com.bagri.common.util.PropUtils.setProperty;
import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.xqj.BagriXQConstants.*;

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
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.client.common.impl.XDMRepositoryBase;
import com.bagri.xdm.client.hazelcast.serialize.SecureCredentials;
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
import com.hazelcast.security.Credentials;

public class RepositoryImpl extends XDMRepositoryBase implements XDMRepository {
	
    private final static Logger logger = LoggerFactory.getLogger(RepositoryImpl.class);
	
	private String clientId;
	private String schemaName;
	
	private ResultCursor cursor;
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
		com.hazelcast.client.impl.HazelcastClientProxy proxy = (com.hazelcast.client.impl.HazelcastClientProxy) hzClient;
		schemaName = proxy.getClientConfig().getGroupConfig().getName();
		clientId = proxy.getLocalEndpoint().getUuid();
		logger.debug("<init>; connected to HZ server as: {}; {}", clientId, proxy);
		Credentials creds = proxy.getClientConfig().getSecurityConfig().getCredentials();
		logger.debug("<init>; with credentials: {}", creds);
		initializeServices();
	}
	
	private static Properties getSystemProps() {
		Properties props = new Properties();
		props.setProperty(pn_schema_name, getSystemProperty(pn_schema_name, "schema"));
		props.setProperty(pn_server_address, getSystemProperty(pn_server_address, "address"));
		props.setProperty(pn_schema_user, getSystemProperty(pn_schema_user, "user"));
		props.setProperty(pn_schema_password, getSystemProperty(pn_schema_password, "password"));
		props.setProperty(pn_client_smart, getSystemProperty(pn_client_smart, "smart"));
		props.setProperty(pn_login_timeout, getSystemProperty(pn_login_timeout, "loginTimeout"));
		return props;
	}
	
	private static Properties getConvertedProps(Properties original) {
		Properties props = new Properties();
		setProperty(original, props, pn_server_address, "address");
		setProperty(original, props, pn_schema_name, "schema");
		setProperty(original, props, pn_schema_user, "user");
		setProperty(original, props, pn_schema_password, "password");
		setProperty(original, props, pn_client_smart, "smart");
		//setProperty(original, props, pn_client_submitTo, "any");
		//setProperty(original, props, pn_fetch_size, "0");
		setProperty(original, props, pn_login_timeout, "loginTimeout");
		props.put(pn_data_factory, original.get(pn_data_factory));
		return props;
	}
	
	private void initializeFromProperties(Properties props) {
		initializeHazelcast(props);
		initializeServices();
	}
	
	private void initializeHazelcast(Properties props) {
		String schema = props.getProperty(pn_schema_name);
		String address = props.getProperty(pn_server_address);
		String user = props.getProperty(pn_schema_user);
		String password = props.getProperty(pn_schema_password);
		String smart = props.getProperty(pn_client_smart);
		String timeout = props.getProperty(pn_login_timeout);
		InputStream in = DocumentManagementImpl.class.getResourceAsStream("/hazelcast/hazelcast-client.xml");
		ClientConfig config = new XmlClientConfigBuilder(in).build();
		config.getGroupConfig().setName(schema);
		config.getGroupConfig().setPassword(password);
		config.getNetworkConfig().addAddress(address);
		if (smart != null) {
			config.getNetworkConfig().setSmartRouting(smart.equalsIgnoreCase("true"));
		}
		if (timeout != null) {
			int tm = Integer.parseInt(timeout); // login timeout in seconds
			if (tm > 0) {
				config.getNetworkConfig().setConnectionTimeout(tm*1000);
			}
		}
		
		//config.setProperty("hazelcast.logging.type", "slf4j");
		//UsernamePasswordCredentials creds = new UsernamePasswordCredentials(schema, password);
		//SecureCredentials creds = new SecureCredentials(schema, password);
		SecureCredentials creds = new SecureCredentials(user, password);
		config.getSecurityConfig().setCredentials(creds);
		config.setCredentials(creds);
			
		XQDataFactory xqFactory = (XQDataFactory) props.get(pn_data_factory);
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
		com.hazelcast.client.impl.HazelcastClientProxy proxy = (com.hazelcast.client.impl.HazelcastClientProxy) hzClient; 
		schemaName = proxy.getClientConfig().getGroupConfig().getName();
		clientId = proxy.getLocalEndpoint().getUuid();
		logger.trace("initializeHazelcast; connected to HZ server as: {}", clientId);
	}

	private void initializeServices() {
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		setBindingManagement(new BindingManagementImpl());
		setDocumentManagement(new DocumentManagementImpl());
		setQueryManagement(new QueryManagementImpl());
		setModelManagement(new ModelManagementImpl(hzClient));
		setTxManagement(new TransactionManagementImpl());
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
	public void setTxManagement(XDMTransactionManagement txMgr) {
		super.setTxManagement(txMgr);
		((TransactionManagementImpl) txMgr).initialize(this);
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
	
	String getClientId() {
		return clientId;
	}

	long getTransactionId() {
		return ((TransactionManagementImpl) this.getTxManagement()).getTxId();
	}
	
	Iterator execXQuery(boolean isQuery, String query, Map bindings, Properties props) { //throws Exception {
		
		//props.put(PN_BATCH_SIZE, "5");
		props.setProperty(pn_client_id, clientId);
		props.setProperty(pn_tx_id, String.valueOf(getTransactionId()));
		//props.setProperty(pn_fetch_size, fetchSize);
		//props.setProperty(pn_client_submitTo, submitTo);
		
		String runOn = props.getProperty(pn_client_submitTo, "any");
		
		XQCommandExecutor task = new XQCommandExecutor(isQuery, schemaName, query, bindings, props);
		Future<ResultCursor> future;
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
		long timeout = Long.parseLong(props.getProperty(pn_queryTimeout, "0"));
		int fetchSize = Integer.parseInt(props.getProperty(pn_fetch_size, "0"));
		try {
			//if (cursor != null) {
			//	cursor.close(false);
			//}
			
			if (timeout > 0) {
				cursor = future.get(timeout, TimeUnit.SECONDS);
			} else {
				cursor = future.get();
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
	
	private Iterator extractFromCursor(ResultCursor cursor) {
		List result = new ArrayList();
		while (cursor.hasNext()) {
			result.add(cursor.next());
		}
		return result.iterator();
	}


}
