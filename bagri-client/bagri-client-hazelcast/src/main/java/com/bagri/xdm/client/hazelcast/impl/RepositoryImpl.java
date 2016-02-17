package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.common.util.PropUtils.setProperty;
import static com.bagri.xdm.common.XDMConstants.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMHealthCheckState;
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
	private ClientManagementImpl clientMgr;
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
		//clientId = proxy.getLocalEndpoint().getUuid();

		clientMgr = new ClientManagementImpl();
		clientId = UUID.randomUUID().toString();
		clientMgr.connect(clientId, proxy);
		
		logger.debug("<init>; connected to HZ server as: {}; {}", clientId, proxy);
		initializeServices(null);
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
		setProperty(props, pn_client_connectAttempts, "3"); 
		setProperty(props, pn_client_poolSize, "5");
		setProperty(props, pn_client_healthCheck, null);
		setProperty(props, pn_client_queryCache, null);
		setProperty(props, pn_client_txTimeout, null);
		setProperty(props, pn_client_customAuth, null);
		return props;
	}
	
	private static Properties getConvertedProps(Properties original) {
		Properties props = new Properties();
		setProperty(original, props, pn_schema_name, "schema");
		setProperty(original, props, pn_schema_address, "address");
		setProperty(original, props, pn_schema_user, "user");
		setProperty(original, props, pn_schema_password, "password");
		setProperty(original, props, pn_client_loginTimeout, "loginTimeout");
		setProperty(original, props, pn_client_bufferSize, null); 
		setProperty(original, props, pn_client_connectAttempts, null);
		Object factory = original.get(pn_client_dataFactory);
		if (factory != null) {
			props.put(pn_client_dataFactory, factory);
		}
		setProperty(props, pn_client_smart, null); 
		setProperty(props, pn_client_poolSize, null);
		setProperty(props, pn_client_healthCheck, null);
		setProperty(props, pn_client_queryCache, null);
		setProperty(props, pn_client_txTimeout, null);
		setProperty(props, pn_client_customAuth, null);
		return props;
	}
	
	private void initializeFromProperties(Properties props) {
		clientMgr = new ClientManagementImpl();
		clientId = UUID.randomUUID().toString();
		hzClient = clientMgr.connect(clientId, props);
		com.hazelcast.client.impl.HazelcastClientProxy proxy = (com.hazelcast.client.impl.HazelcastClientProxy) hzClient; 
		schemaName = proxy.getClientConfig().getGroupConfig().getName();
		initializeServices(props);
	}
	
	private void initializeServices(Properties props) {
		//execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		setAccessManagement(new AccessManagementImpl());
		setBindingManagement(new BindingManagementImpl());
		setDocumentManagement(new DocumentManagementImpl());
		setHealthManagement(new HealthManagementImpl(hzClient));
		setModelManagement(new ModelManagementImpl(hzClient));
		setQueryManagement(new QueryManagementImpl());
		setTxManagement(new TransactionManagementImpl());
		hzClient.getUserContext().put(bean_id, this);

		if (props != null) {
			String value = props.getProperty(pn_client_healthCheck);
			if (value != null) {
				getHealthManagement().setCheckSate(XDMHealthCheckState.valueOf(value));
			}
			value = props.getProperty(pn_client_queryCache);
			if (value != null) {
				((QueryManagementImpl) getQueryManagement()).setQueryCache(Boolean.parseBoolean(value));
			}
		}
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
		clientMgr.disconnect(clientId);
		logger.trace("close.exit;");
	}
	
	@Override
	public String getClientId() {
		return clientId;
	}
	
	@Override
	public String getUserName() {
		return clientMgr.getUserName(clientId);
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
