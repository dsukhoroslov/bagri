package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.common.util.PropUtils.setProperty;
import static com.bagri.xdm.common.XDMConstants.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

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
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.security.Credentials;

public class RepositoryImpl extends XDMRepositoryBase implements XDMRepository {
	
    private final static Logger logger = LoggerFactory.getLogger(RepositoryImpl.class);
	
	private String clientId;
	private String schemaName;
	private HazelcastInstance hzClient;
	
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
		setProperty(props, pn_schema_name, null); // "schema"
		setProperty(props, pn_schema_address, null); //"address"
		setProperty(props, pn_schema_user, null); //"user"
		setProperty(props, pn_schema_password, null); //"password"
		setProperty(props, pn_client_smart, null); //"smart"
		setProperty(props, pn_client_loginTimeout, null); //"loginTimeout"
		setProperty(props, pn_client_bufferSize, null); 
		setProperty(props, pn_client_connectAttempts, null); 
		return props;
	}
	
	private static Properties getConvertedProps(Properties original) {
		Properties props = new Properties();
		setProperty(original, props, pn_schema_name, "schema");
		setProperty(original, props, pn_schema_address, "address");
		setProperty(original, props, pn_schema_user, "user");
		setProperty(original, props, pn_schema_password, "password");
		//setProperty(original, props, pn_client_smart, "smart");
		setProperty(original, props, pn_client_loginTimeout, "loginTimeout");
		setProperty(original, props, pn_client_bufferSize, null); 
		setProperty(original, props, pn_client_connectAttempts, null);
		Object factory = original.get(pn_data_factory);
		if (factory != null) {
			props.put(pn_data_factory, factory);
		}
		setProperty(props, pn_client_smart, null); //"smart"
		return props;
	}
	
	private void initializeFromProperties(Properties props) {
		initializeHazelcast(props);
		initializeServices();
	}
	
	private void initializeHazelcast(Properties props) {
		String schema = props.getProperty(pn_schema_name);
		String address = props.getProperty(pn_schema_address);
		String user = props.getProperty(pn_schema_user);
		String password = props.getProperty(pn_schema_password);
		String smart = props.getProperty(pn_client_smart);
		String timeout = props.getProperty(pn_client_loginTimeout);
		String buffer = props.getProperty(pn_client_bufferSize); 
		String attempts = props.getProperty(pn_client_connectAttempts); 

		InputStream in = getClass().getResourceAsStream("/hazelcast/hazelcast-client.xml");
		ClientConfig config = new XmlClientConfigBuilder(in).build();
		config.getGroupConfig().setName(schema);
		config.getGroupConfig().setPassword(password);
		String[] members = address.split(",");
		config.getNetworkConfig().setAddresses(Arrays.asList(members));
		if (smart != null) {
			config.getNetworkConfig().setSmartRouting(smart.equalsIgnoreCase("true"));
		}
		if (attempts != null) {
			int count = Integer.parseInt(attempts);
			if (count > 0) {
				config.getNetworkConfig().setConnectionAttemptLimit(count);
			}
		}
		if (timeout != null) {
			int tm = Integer.parseInt(timeout); // login timeout in seconds
			if (tm > 0) {
				config.getNetworkConfig().setConnectionTimeout(tm*1000);
			}
		}
		if (buffer != null) {
			int size = Integer.parseInt(buffer);
			if (size > 0) {
				config.getNetworkConfig().getSocketOptions().setBufferSize(size);
			}
		}
		
		config.setProperty("hazelcast.logging.type", "slf4j");
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
		try {
			hzClient = HazelcastClient.newHazelcastClient(config);
			//logger.debug("initializeHazelcast; got HZ: {}", hzInstance);
		} catch (Throwable ex) {
			logger.error("initializeHazelcast.error", ex);
			throw ex;
		}
		com.hazelcast.client.impl.HazelcastClientProxy proxy = (com.hazelcast.client.impl.HazelcastClientProxy) hzClient; 
		schemaName = proxy.getClientConfig().getGroupConfig().getName();
		clientId = proxy.getLocalEndpoint().getUuid();
		logger.trace("initializeHazelcast; connected to HZ server as: {}", clientId);
	}

	private void initializeServices() {
		//execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		setBindingManagement(new BindingManagementImpl());
		setDocumentManagement(new DocumentManagementImpl());
		setQueryManagement(new QueryManagementImpl());
		setModelManagement(new ModelManagementImpl(hzClient));
		setTxManagement(new TransactionManagementImpl());
	}
	
	@Override
	public int hashCode() {
		return clientId.hashCode();
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
	
	@Override
	public String getClientId() {
		return clientId;
	}
	
	@Override
	public String toString() {
		return "RepositoryImpl[" + clientId + "]";
	}
	
	HazelcastInstance getHazelcastClient() {
		return hzClient;
	}
	
	String getSchemaName() {
		return schemaName;
	}

	long getTransactionId() {
		return ((TransactionManagementImpl) this.getTxManagement()).getTxId();
	}
	
}
