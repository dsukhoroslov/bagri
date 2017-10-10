package com.bagri.client.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.support.util.PropUtils.setProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.serialize.ByteMapContentSerializer;
import com.bagri.client.hazelcast.serialize.ObjectMapContentSerializer;
import com.bagri.client.hazelcast.serialize.StringContentSerializer;
import com.bagri.client.hazelcast.serialize.StringMapContentSerializer;
import com.bagri.core.api.ContentSerializer;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.HealthCheckState;
import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.impl.SchemaRepositoryBase;
import com.hazelcast.core.HazelcastInstance;

public class SchemaRepositoryImpl extends SchemaRepositoryBase implements SchemaRepository {
	
    private static final Logger logger = LoggerFactory.getLogger(SchemaRepositoryImpl.class);
    private static final ThreadLocal<SchemaRepository> repo = new ThreadLocal<>();
    
	private String clientId;
	private String schemaName;
	private ClientManagementImpl clientMgr;
	private HazelcastInstance hzClient;
	private Map<String, ContentSerializer<?>> css = new HashMap<>(); //ConcurrentHashMap<>();
	
	public static SchemaRepository getRepository() {
		return repo.get();
	}
	
	public SchemaRepositoryImpl() {
		initializeFromProperties(getSystemProps());
		repo.set(this);
	}

	public SchemaRepositoryImpl(Properties props) {
		initializeFromProperties(getConvertedProps(props));
		repo.set(this);
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
		// TODO: serializers!?
		repo.set(this);
	}
	
	private static Properties getSystemProps() {
		Properties props = new Properties();
		setProperty(props, pn_schema_name, null);
		setProperty(props, pn_schema_address, null);
		setProperty(props, pn_schema_user, null);
		setProperty(props, pn_schema_password, null);
		setProperty(props, pn_client_smart, null);
		setProperty(props, pn_client_loginTimeout, null);
		setProperty(props, pn_client_sharedConnection, null);
		setProperty(props, pn_client_bufferSize, null); 
		setProperty(props, pn_client_connectAttempts, "3"); 
		setProperty(props, pn_client_poolSize, "5");
		setProperty(props, pn_client_healthCheck, null);
		setProperty(props, pn_client_queryCache, null);
		setProperty(props, pn_client_txLevel, null);
		setProperty(props, pn_client_txTimeout, null);
		setProperty(props, pn_client_customAuth, null);
		setProperty(props, pn_client_contentSerializers, pv_client_defaultSerializers);
		setProperty(props, pn_client_contentSerializer + "." + "MAP", ObjectMapContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "BMAP", ByteMapContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "SMAP", StringMapContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "JSON", StringContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "XML", StringContentSerializer.class.getName());
		return props;
	}
	
	private static Properties getConvertedProps(Properties original) {
		Properties props = new Properties();
		setProperty(original, props, pn_schema_name, "schema");
		setProperty(original, props, pn_schema_address, "address");
		setProperty(original, props, pn_schema_user, "user");
		setProperty(original, props, pn_schema_password, "password");
		setProperty(original, props, pn_client_loginTimeout, "loginTimeout");
		setProperty(original, props, pn_client_sharedConnection, "sharedConnection");
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
		setProperty(props, pn_client_txLevel, null);
		setProperty(props, pn_client_txTimeout, null);
		setProperty(props, pn_client_customAuth, null);
		setProperty(props, pn_client_contentSerializers, pv_client_defaultSerializers);
		setProperty(props, pn_client_contentSerializer + "." + "MAP", ObjectMapContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "BMAP", ByteMapContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "SMAP", StringMapContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "JSON", StringContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "XML", StringContentSerializer.class.getName());
		return props;
	}

	private void initializeFromProperties(Properties props) {
		clientMgr = new ClientManagementImpl();
		clientId = UUID.randomUUID().toString();
		hzClient = clientMgr.connect(clientId, props);
		com.hazelcast.client.impl.HazelcastClientProxy proxy = (com.hazelcast.client.impl.HazelcastClientProxy) hzClient; 
		schemaName = proxy.getClientConfig().getGroupConfig().getName();
		initializeServices(props);
		initializeSerializers(props);
	}

	private void initializeSerializers(Properties props) {
		String srs = props.getProperty(pn_client_contentSerializers);
		if (srs == null) {
			srs = pv_client_defaultSerializers;
		}
		String[] sra = srs.split(" ");
		for (String csn: sra) {
			String csp = pn_client_contentSerializer + "." + csn;
			String csc = props.getProperty(csp);
			if (csc != null) {
				try {
					Class<?> cs = Class.forName(csc);
					css.put(csn, (ContentSerializer<?>) cs.newInstance());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
					logger.warn("initializeSerializers. error instaintiating serializer '{}' for format '{}'", csc, csn, ex);
				}
			}
		}
		logger.info("initializeSerializers.exit; css: {}", css);
		//if (!css.containsKey("MAP")) {
		//	css.put("MAP", new StringMapContentSerializer());
		//}
		//if (!css.containsKey("BMAP")) {
		//	css.put("BMAP", new ByteMapContentSerializer());
		//}
		//if (!css.containsKey("JSON")) {
		//	css.put("JSON", new StringContentSerializer());
		//}
		//if (!css.containsKey("XML")) {
		//	css.put("XML", new StringContentSerializer());
		//}
	}
	
	private void initializeServices(Properties props) {
		//execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
		setAccessManagement(new AccessManagementImpl());
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
			value = props.getProperty(pn_client_txLevel);
			if (value != null) {
				((QueryManagementImpl) getQueryManagement()).setDefaultTxLevel(value);
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
	public ContentSerializer<?> getSerializer(String dataFormat) {
		ContentSerializer<?> cs = css.get(dataFormat);
		if (cs == null) {
			logger.info("getSerializer; no serializer for type: {}; css: {}; this: {}", dataFormat, css, this);
		}
		return cs;
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
