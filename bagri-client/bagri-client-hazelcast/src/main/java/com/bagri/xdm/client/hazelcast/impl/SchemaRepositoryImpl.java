package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.common.util.PropUtils.setProperty;
import static com.bagri.xdm.common.Constants.*;

import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.HealthCheckState;
import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.TransactionManagement;
import com.bagri.xdm.api.impl.SchemaRepositoryBase;
import com.hazelcast.core.HazelcastInstance;

public class SchemaRepositoryImpl extends SchemaRepositoryBase implements SchemaRepository {
	
    private final static Logger logger = LoggerFactory.getLogger(SchemaRepositoryImpl.class);
	
	private String clientId;
	private String schemaName;
	private ClientManagementImpl clientMgr;
	private HazelcastInstance hzClient;
	
	public SchemaRepositoryImpl() {
		initializeFromProperties(getSystemProps());
	}

	public SchemaRepositoryImpl(Properties props) {
		initializeFromProperties(getConvertedProps(props));
	}
	
	public SchemaRepositoryImpl(HazelcastInstance hzInstance) {
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
		setQueryManagement(new QueryManagementImpl());
		setTxManagement(new TransactionManagementImpl());
		hzClient.getUserContext().put("xdmRepo", this);

		if (props != null) {
			String value = props.getProperty(pn_client_healthCheck);
			if (value != null) {
				getHealthManagement().setCheckSate(HealthCheckState.valueOf(value));
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
	public void setDocumentManagement(DocumentManagement docMgr) {
		super.setDocumentManagement(docMgr);
		((DocumentManagementImpl) docMgr).initialize(this);
	}

	@Override
	public void setQueryManagement(QueryManagement queryMgr) {
		super.setQueryManagement(queryMgr);
		((QueryManagementImpl) queryMgr).initialize(this);
	}
	
	@Override
	public void setTxManagement(TransactionManagement txMgr) {
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
		return "SchemaRepositoryImpl[" + clientId + "]";
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
