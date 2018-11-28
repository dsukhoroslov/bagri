package com.bagri.client.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.support.util.PropUtils.setProperty;
import static com.bagri.core.server.api.CacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.serialize.ByteMapContentSerializer;
import com.bagri.client.hazelcast.serialize.ObjectMapContentSerializer;
import com.bagri.client.hazelcast.serialize.StringContentSerializer;
import com.bagri.client.hazelcast.serialize.StringMapContentSerializer;
import com.bagri.client.hazelcast.task.auth.ChildIdsRegistrator;
import com.bagri.core.api.ContentSerializer;
import com.bagri.core.api.DocumentDistributionStrategy;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.HealthCheckState;
import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.impl.DefaultDocumentDistributor;
import com.bagri.core.api.impl.SchemaRepositoryBase;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.client.impl.clientside.HazelcastClientProxy;

public class SchemaRepositoryImpl extends SchemaRepositoryBase implements SchemaRepository {
	
    private static final Logger logger = LoggerFactory.getLogger(SchemaRepositoryImpl.class);
    private static final ThreadLocal<SchemaRepository> repo = new ThreadLocal<>();
    private static SchemaRepository stRepo = null;
    
	private String clientId;
    private AtomicLong childIdx = new AtomicLong(0);
    private String[] childIdsPool = null;

	private String schemaName;
	private ClientManagementImpl clientMgr;
	private HazelcastInstance hzClient;
	private Map<String, ContentSerializer<?>> css = new HashMap<>(); //ConcurrentHashMap<>();
	private DocumentDistributionStrategy distributor;
	
	public static SchemaRepository getRepository() {
		SchemaRepository r = repo.get();
		if (r == null) {
			r = stRepo;
		}
		return r;
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
		HazelcastClientProxy proxy = (HazelcastClientProxy) hzClient;
		schemaName = proxy.getClientConfig().getGroupConfig().getName();

		clientMgr = new ClientManagementImpl();
		clientId = UUID.randomUUID().toString();
		clientMgr.connect(clientId, proxy);
		
		logger.debug("<init>; connected to HZ server as: {}; {}", clientId, proxy);
		initializeServices(null);
		// TODO: serializers!?
		repo.set(this);
	}
	
	public void setStaticRepository() {
		stRepo = this;
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
		setProperty(props, pn_client_idCount, null);
		setProperty(props, pn_client_connectAttempts, "3"); 
		setProperty(props, pn_client_poolSize, "8");
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
		setProperty(props, pn_document_distribution, null);
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
		setProperty(original, props, pn_client_idCount, null);
		setProperty(original, props, pn_client_connectAttempts, null);

		Object factory = original.get(pn_client_dataFactory);
		if (factory != null) {
			props.put(pn_client_dataFactory, factory);
		}
		
		setProperty(original, props, pn_client_smart, null); 
		setProperty(original, props, pn_client_poolSize, null);
		setProperty(original, props, pn_client_healthCheck, null);
		setProperty(original, props, pn_client_queryCache, null);
		setProperty(original, props, pn_client_txLevel, null);
		setProperty(original, props, pn_client_txTimeout, null);
		setProperty(original, props, pn_client_customAuth, null);

		//setProperty(original, props, pn_client_submitTo, null);
		//setProperty(original, props, pn_client_ownerParam, null);
		setProperty(original, props, pn_client_fetchAsynch, null);
		setProperty(original, props, pn_client_fetchSize, null);
		setProperty(original, props, pn_client_fetchType, null);
		setProperty(original, props, pn_client_storeMode, null);
		
		setProperty(props, pn_client_contentSerializers, pv_client_defaultSerializers);
		setProperty(props, pn_client_contentSerializer + "." + "MAP", ObjectMapContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "BMAP", ByteMapContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "SMAP", StringMapContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "JSON", StringContentSerializer.class.getName());
		setProperty(props, pn_client_contentSerializer + "." + "XML", StringContentSerializer.class.getName());

		setProperty(props, pn_document_distribution, null);
		//-Dbdb.document.compress=false
		//-Dbdb.document.data.format=BMAP
		//-Dbdb.document.map.merge=true
		return props;
	}

	private void initializeFromProperties(Properties props) {
		clientMgr = new ClientManagementImpl();
		clientId = UUID.randomUUID().toString();
		logger.debug("initializeFromProperties; initializing with props: {}", props);
		hzClient = clientMgr.connect(clientId, props);
		HazelcastClientProxy proxy = (HazelcastClientProxy) hzClient; 
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
			value = props.getProperty(pn_client_idCount);
			if (value != null) {
				setChildIdsPool(Integer.parseInt(value));
			}
			value = props.getProperty(pn_client_queryCache);
			if (value != null) {
				((QueryManagementImpl) getQueryManagement()).setQueryCache(Boolean.parseBoolean(value));
			}
			value = props.getProperty(pn_client_txLevel);
			if (value != null) {
				((QueryManagementImpl) getQueryManagement()).setDefaultTxLevel(value);
			}
			value = props.getProperty(pn_document_distribution);
			if (value != null) {
				try {
					Class<?> procClass = Class.forName(value);
					Object instance = procClass.newInstance();
					distributor = (DocumentDistributionStrategy) instance;
				} catch (Exception ex) {
					logger.warn("initializeServices; can't instantiate distributor for class: {}", value);
					distributor = new DefaultDocumentDistributor();
				}
			} else {
				distributor = new DefaultDocumentDistributor();
			}
		}
	}
	
	private void setChildIdsPool(int poolSize) {
		if (poolSize > 0) {
			childIdsPool = new String[poolSize];
			for (int i=0; i < poolSize; i++) {
				childIdsPool[i] = UUID.randomUUID().toString();
			}
			
			ChildIdsRegistrator cir = new ChildIdsRegistrator(clientId, childIdsPool);
			hzClient.getExecutorService(PN_XDM_SCHEMA_POOL).submitToAllMembers(cir);
			// we don't need wait for response?
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
		// disconnect all child ids too...
		ClientManagementImpl.disconnect(clientId);
		logger.trace("close.exit;");
	}
	
	@Override
	public String getClientId() {
		return clientId;
	}

	public String getChildId() {
		if (childIdsPool == null) {
			return clientId;
		}
		long cIdx = childIdx.incrementAndGet();
		if (cIdx == Long.MAX_VALUE) {
			childIdx.set(0);
		}
		int idx = (int) (cIdx % childIdsPool.length);
		return childIdsPool[idx];
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
	public DocumentDistributionStrategy getDistributionStrategy() {
		return distributor;
	}
	
	@Override
	public String getUserName() {
		return clientMgr.getUserName(clientId);
	}
	
	@Override
	public String toString() {
		return "SchemaRepositoryImpl[" + clientId + "]";
	}
	
	public HazelcastInstance getHazelcastClient() {
		return hzClient;
	}
	
	String getSchemaName() {
		return schemaName;
	}

	long getTransactionId() {
		return ((TransactionManagementImpl) this.getTxManagement()).getTxId();
	}

}

/*
bdb.client.contentSerializer.MAP=com.bagri.client.hazelcast.serialize.ObjectMapContentSerializer, 
bdb.client.loginTimeout=30, bdb.schema.address=127.0.0.1:10400, bdb.client.connectAttempts=3, 
bdb.client.member=ab45fb92-d290-4ea0-8a5f-c470a87c8fc2, 
bdb.client.contentSerializer.JSON=com.bagri.client.hazelcast.serialize.StringContentSerializer, 
bdb.client.contentSerializers=MAP BMAP SMAP JSON XML, bdb.client.fetchSize=50, 
bdb.client.contentSerializer.XML=com.bagri.client.hazelcast.serialize.StringContentSerializer, 
bdb.client.connectedAt=Thu Nov 22 02:57:06 MSK 2018, 
bdb.client.contentSerializer.SMAP=com.bagri.client.hazelcast.serialize.StringMapContentSerializer, 
bdb.schema.password=5f4dcc3b5aa765d61d8327deb882cf99, bdb.schema.name=MP_GPC, bdb.client.bufferSize=32, 
bdb.client.contentSerializer.BMAP=com.bagri.client.hazelcast.serialize.ByteMapContentSerializer, 
bdb.schema.user=admin
*/