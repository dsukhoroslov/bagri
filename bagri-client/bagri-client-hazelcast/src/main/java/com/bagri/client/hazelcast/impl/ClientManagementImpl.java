package com.bagri.client.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_CLIENT;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.serialize.XQItemSerializer;
import com.bagri.client.hazelcast.serialize.XQItemTypeSerializer;
import com.bagri.client.hazelcast.serialize.XQSequenceSerializer;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.xqj.BagriXQDataFactory;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.client.impl.clientside.HazelcastClientProxy;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;

public class ClientManagementImpl {
	
    private final static Logger logger = LoggerFactory.getLogger(ClientManagementImpl.class);
    private final static String key_separator = "::"; 
	
    private final static Map<String, String> ids = new ConcurrentHashMap<>();
    private final static Map<String, ClientContainer> clients = new ConcurrentHashMap<>();
    
    public HazelcastInstance connect(final String clientId, Properties props) {
		ClientContainer cc = null;
    	String cKey = getConnectKey(props);
    	boolean shareConnect = Boolean.parseBoolean(props.getProperty(pn_client_sharedConnection, "true"));
		synchronized (clients) {
			// register ShutdownHook
			if (clients.isEmpty()) {
				Runtime.getRuntime().addShutdownHook(new ClientTerminationHook());
			}
			
			if (shareConnect) {
				cc = clients.get(cKey);
			} else {
				cKey += key_separator + clientId;
			}
			ids.put(clientId, cKey);
   			if (cc == null) {
   				HazelcastInstance hzClient = initializeHazelcast(props);
   				cc = new ClientContainer(cKey, hzClient);
   				clients.put(cKey, cc);
				logger.info("connect; new HZ instance created for clientId: {}", clientId);
   			} else {
   				// TODO: check password -> authenticate(); ??
   			}
		}
		props.remove(pn_client_dataFactory);
		addClient(cc, clientId, props);
    	return cc.hzInstance;
    }

    public void connect(final String clientId, HazelcastClientProxy hzProxy) {
    	String cKey = getConnectKey(hzProxy);
    	ClientContainer cNew = new ClientContainer(cKey, hzProxy);
    	//ClientContainer cOld = clients.putIfAbsent(cKey, cNew);
    	ClientContainer cOld = clients.put(cKey, cNew);
    	if (cOld == null) {
			logger.info("connect; new container created for clientId: {}", clientId);
		} else {
			// check password -> authenticate(); ??
			cNew = cOld;
		}
    	ids.put(clientId, cKey);
		Properties props = new Properties();
    	ClientConfig config = hzProxy.getClientConfig(); 
		props.setProperty(pn_schema_name, config.getGroupConfig().getName());
		props.setProperty(pn_schema_address, config.getNetworkConfig().getAddresses().toString());
		props.setProperty(pn_schema_user, config.getCredentials().getPrincipal());
		props.setProperty(pn_client_smart, String.valueOf(config.getNetworkConfig().isSmartRouting()));
		props.setProperty(pn_client_bufferSize, String.valueOf(config.getNetworkConfig().getSocketOptions().getBufferSize()));
		addClient(cNew, clientId, props);
    }
    
    private void addClient(final ClientContainer cc, final String clientId, Properties props) {
    	HazelcastInstance hzClient = cc.hzInstance; 
    	if (cc.addClient(clientId)) {
    		ReplicatedMap<String, Properties> clientProps = hzClient.getReplicatedMap(CN_XDM_CLIENT);
    		HazelcastClientProxy proxy = (HazelcastClientProxy) hzClient; 
    		props.setProperty(pn_client_memberId, proxy.client.getClientClusterService().getLocalClient().getUuid());
    		props.setProperty(pn_client_connectedAt, new java.util.Date(proxy.getCluster().getClusterTime()).toString());
    		clientProps.put(clientId, props);
			logger.debug("addClient; got new connection for clientId: {}", clientId);
    	} else {
			logger.trace("addClient; got existing connection for clientId: {}", clientId);
    	}
    }
    
    public static void disconnect(final String clientId) {
   		logger.debug("disconnect; disconnecting client: {}", clientId);
    	String cKey = ids.remove(clientId);
    	boolean hzShutdown = false;
    	ClientContainer cc = clients.get(cKey);
    	if (cc != null) { 
    		cc.removeClient(clientId);
       		logger.trace("disconnect; client: {}; clients left in container: {}", clientId, cc.getSize());
       		
			if (cc.isEmpty()) {
        		if (clients.remove(cc.clientKey) != null) {
					logger.debug("disconnect; client container is empty, disposed");
					hzShutdown = true;
    			} else {
					logger.info("disconnect; concurrently removing container for found key: {}", cc.clientKey);
	    		}
    		} else  {
				logger.debug("disconnect; disconnected  client: {}; remaining clients: {}", clientId, cc.getSize());
			}

			final boolean shutdown = hzShutdown;
			final HazelcastInstance hzClient = cc.hzInstance;
			new Thread() {
				@Override
				public void run() {
					if (hzClient.getLifecycleService().isRunning()) {
						ReplicatedMap<String, Properties> clientProps = hzClient.getReplicatedMap(CN_XDM_CLIENT);
						clientProps.remove(clientId);
						if (shutdown) {
							logger.info("disconnect; shutting down HZ instance: {}", hzClient.getLocalEndpoint().getUuid());
							long stamp = System.currentTimeMillis();
							hzClient.getLifecycleService().shutdown();
							logger.info("disconnect; instance shutdown, time taken: {}", System.currentTimeMillis() - stamp);
						}
					} else {
						logger.info("disconnect; attempted to shutdown not-running client!");
					}
				}
			}.start();
    	} else {
    		logger.info("disconnect; can't find container for client: {}; clients: {}", clientId, clients);
    	}
    }
    
    public String getUserName(String clientId) {
    	ClientContainer cc = getClientContainer(clientId);
    	if (cc == null) {
    		return null;
    	}
    	String[] parts = cc.clientKey.split(key_separator);
    	return parts[2];
    }
    
    private String getConnectKey(Properties props) {
		String schema = props.getProperty(pn_schema_name);
		String address = props.getProperty(pn_schema_address);
		String user = props.getProperty(pn_schema_user);
		String smart = props.getProperty(pn_client_smart);
		String buffer = props.getProperty(pn_client_bufferSize); 
		return schema + key_separator + address + key_separator + user + key_separator + smart + key_separator + buffer;
    }

    private String getConnectKey(HazelcastClientProxy hzProxy) {
    	ClientConfig config = hzProxy.getClientConfig(); 
    	return config.getGroupConfig().getName() + key_separator +
    		   config.getNetworkConfig().getAddresses().toString() + key_separator +
    		   config.getCredentials().getPrincipal() + key_separator + 
    		   config.getNetworkConfig().isSmartRouting() + key_separator + 
    		   config.getNetworkConfig().getSocketOptions().getBufferSize();
    }
    
    private ClientContainer getClientContainer(String clientId) {
    	for (ClientContainer cc: clients.values()) {
    		if (cc.hasClient(clientId)) {
    			return cc;
    		}
    	}
    	return null;
    }
    
	private HazelcastInstance initializeHazelcast(Properties props) {
		String schema = props.getProperty(pn_schema_name);
		String address = props.getProperty(pn_schema_address);
		String user = props.getProperty(pn_schema_user);
		String password = props.getProperty(pn_schema_password);
		String smart = props.getProperty(pn_client_smart);
		String timeout = props.getProperty(pn_client_loginTimeout);
		String buffer = props.getProperty(pn_client_bufferSize); 
		String attempts = props.getProperty(pn_client_connectAttempts); 
		String pool = props.getProperty(pn_client_poolSize); 
		String custom = props.getProperty(pn_client_customAuth);

		//password = encrypt(password);
		
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
		if (pool != null) {
			int size = Integer.parseInt(pool);
			if (size > 0) {
				config.setExecutorPoolSize(size);
			}
		}
		
		//config.setProperty("hazelcast.logging.type", "slf4j");
		// for HZ-3.9
		//config.setProperty("hazelcast.socket.client.buffer.direct", "true");
		
		if (custom == null || "true".equalsIgnoreCase(custom)) {
			SecureCredentials creds = new SecureCredentials(user, password);
			//config.getSecurityConfig().setCredentials(creds);
			config.setCredentials(creds);
		}

		XQProcessor proc = null;
		BagriXQDataFactory xqFactory = (BagriXQDataFactory) props.get(pn_client_dataFactory);
		if (xqFactory != null) {
			proc = xqFactory.getProcessor();
		}
		xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(proc);
		
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
		
		logger.debug("initializeHazelcast; config: {}", config);
		HazelcastInstance hzClient;
		try {
			hzClient = HazelcastClient.newHazelcastClient(config);
			//logger.debug("initializeHazelcast; got HZ: {}", hzInstance);
		} catch (Throwable ex) {
			logger.error("initializeHazelcast.error", ex);
			throw ex;
		}
		return hzClient;
	}

	private class ClientContainer {
		
		private String clientKey;
		private HazelcastInstance hzInstance;
		private Set<String> clientIds = new ConcurrentSkipListSet<>();
		
		ClientContainer(String clientKey, HazelcastInstance hzInstance) {
			this.clientKey = clientKey;
			this.hzInstance = hzInstance;
		}
		
		boolean addClient(String clientId) {
			return clientIds.add(clientId);
		}
		
		int getSize() {
			return clientIds.size();
		}

		boolean hasClient(String clientId) {
			return clientIds.contains(clientId);
		}
		
		boolean isEmpty() {
			return clientIds.isEmpty();
		}
		
		boolean removeClient(String clientId) {
			return clientIds.remove(clientId);
		}
		
		@Override
		public String toString() {
			return "CC[key: " + clientKey + "; ids: " + clientIds + "]";
		}
		
	}
	
	private static class ClientTerminationHook extends Thread {
		
		@Override
		public void run() {
			String[] cids = ids.keySet().toArray(new String[ids.size()]);
			for (String clientId: cids) {
       				disconnect(clientId);
			}
			// causes NPEs on the server side!
			//ids.clear();
			//for (ClientContainer cc: clients.values()) {
			//	HazelcastInstance hzClient = cc.hzInstance;
			//	if (hzClient.getLifecycleService().isRunning()) {
			//		ReplicatedMap<String, Properties> clientProps = hzClient.getReplicatedMap(CN_XDM_CLIENT);
			//		clientProps.clear();
			//		logger.info("terminate; shutting down HZ instance: {}", hzClient.getLocalEndpoint().getUuid());
			//		hzClient.getLifecycleService().shutdown();
			//		logger.info("terminate; instance shutdown");
			//	}
			//}
			//clients.clear();
		}
		
	}
    
}
