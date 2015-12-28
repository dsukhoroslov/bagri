package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_CLIENT;
import static com.bagri.xdm.common.XDMConstants.pn_client_bufferSize;
import static com.bagri.xdm.common.XDMConstants.pn_client_connectAttempts;
import static com.bagri.xdm.common.XDMConstants.pn_client_loginTimeout;
import static com.bagri.xdm.common.XDMConstants.pn_client_poolSize;
import static com.bagri.xdm.common.XDMConstants.pn_client_smart;
import static com.bagri.xdm.common.XDMConstants.pn_data_factory;
import static com.bagri.xdm.common.XDMConstants.pn_schema_address;
import static com.bagri.xdm.common.XDMConstants.pn_schema_name;
import static com.bagri.xdm.common.XDMConstants.pn_schema_password;
import static com.bagri.xdm.common.XDMConstants.pn_schema_user;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.client.hazelcast.serialize.SecureCredentials;
import com.bagri.xdm.client.hazelcast.serialize.XQItemSerializer;
import com.bagri.xdm.client.hazelcast.serialize.XQItemTypeSerializer;
import com.bagri.xdm.client.hazelcast.serialize.XQSequenceSerializer;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.api.XQProcessor;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ClientManagementImpl {
	
    private final static Logger logger = LoggerFactory.getLogger(ClientManagementImpl.class);
	
    private final static Map<String, ClientContainer> clients = new HashMap<>();

    public HazelcastInstance connect(String clientId, Properties props) {
    	String cKey = getConnectKey(props);
		synchronized (clients) {
			ClientContainer cc = clients.get(cKey);
   			if (cc == null) {
   				HazelcastInstance hzClient = initializeHazelcast(props);
   				cc = new ClientContainer(cKey, hzClient);
   				clients.put(cKey, cc);
				logger.info("connect; new HZ instance created for clientId: {}", clientId);
   			} else {
   				// check password -> authenticate();
   			}

   	    	HazelcastInstance hzClient = cc.hzInstance; 
   	    	if (cc.addClient(clientId)) {
   	    		IMap<String, Properties> clientProps = hzClient.getMap(CN_XDM_CLIENT);
   	    		props.remove(pn_data_factory);
   	    		clientProps.set(clientId, props);
   				logger.trace("connect; got new connection for clientId: {}", clientId);
   	    	} else {
   				logger.info("connect; got existing connection for clientId: {}", clientId);
   	    	}
   	    	return hzClient;
		}
    }

    public void connect(String clientId, HazelcastClientProxy hzProxy) {
    	String cKey = getConnectKey(hzProxy);
		synchronized (clients) {
			ClientContainer cc = clients.get(cKey);
   			if (cc == null) {
   				cc = new ClientContainer(cKey, hzProxy);
   				clients.put(cKey, cc);
				logger.info("connect; new container created for clientId: {}", clientId);
   			} else {
   				// check password -> authenticate();
   			}
   			
   	    	HazelcastInstance hzClient = cc.hzInstance; 
   	    	if (cc.addClient(clientId)) {
   	    		//IMap<String, Properties> clientProps = hzClient.getMap(CN_XDM_CLIENT);
   	    		//props.remove(pn_data_factory);
   	    		//clientProps.set(clientId, props);
   				logger.trace("connect; got new connection for clientId: {}", clientId);
   	    	} else {
   				logger.info("connect; got existing connection for clientId: {}", clientId);
   	    	}
		}
    }
    
    //private void addClient(ClientContainer cc, String clientId) {

    //	HazelcastInstance hzClient = cc.hzInstance; 
    //	if (cc.addClient(clientId)) {
    //		IMap<String, Properties> clientProps = hzClient.getMap(CN_XDM_CLIENT);
    //		props.remove(pn_data_factory);
    //		clientProps.set(clientId, props);
	//		logger.trace("connect; got new connection for clientId: {}", clientId);
    //	} else {
	//		logger.info("connect; got existing connection for clientId: {}", clientId);
    //	}
    //	
    //}
    
    public void disconnect(String clientId) {
    	synchronized (clients) {
        	ClientContainer found = null;
	    	for (ClientContainer cc: clients.values()) {
				logger.trace("disconnect; disconnecting: {}; current clients: {}", clientId, cc.getSize());
	    		if (cc.removeClient(clientId)) {
	        		IMap<String, Properties> clientProps = cc.hzInstance.getMap(CN_XDM_CLIENT);
	        		clientProps.delete(clientId);
	    			logger.trace("disconnect; clientId {} successfuly disconnected", clientId);
	    			found = cc;
	        		break;
	    		} else {
	    			logger.info("disconnect; container don't see client ID: {}; existing: {}", clientId, cc.getClients());
	    		}
	    	}
	
	    	if (found != null) {
	    		if (found.isEmpty()) {
					if (found.hzInstance.getLifecycleService().isRunning()) {
						logger.info("disconnect; going to close HZ instance: {}", found.hzInstance);
						found.hzInstance.getLifecycleService().shutdown();
						// probably, should do something like this:
						//execService.awaitTermination(100, TimeUnit.SECONDS);
						clients.remove(found.clientKey);
					} else {
						logger.info("disconnect; an attempt to close not-running client!");
					}
	    		} else  {
					logger.trace("disconnect; disconnected: {}; remaining clients: {}", clientId, found.getSize());
				}
	    	} else {
	    		logger.info("disconnect; can not find container for client: {}", clientId);
	    	}
    	}
    }
    
    public String getUserName(String clientId) {
    	ClientContainer cc = getClientContainer(clientId);
    	if (cc == null) {
    		return null;
    	}
    	String[] parts = cc.clientKey.split("::");
    	return parts[2];
    }
    
    private String getConnectKey(Properties props) {
		String schema = props.getProperty(pn_schema_name);
		String address = props.getProperty(pn_schema_address);
		String user = props.getProperty(pn_schema_user);
		String smart = props.getProperty(pn_client_smart);
		String buffer = props.getProperty(pn_client_bufferSize); 
		return schema + "::" + address + "::" + user + "::" + smart + "::" + buffer;
    }

    private String getConnectKey(HazelcastClientProxy hzProxy) {
    	ClientConfig config = hzProxy.getClientConfig(); 
    	return config.getGroupConfig().getName() + "::" +
    		   config.getNetworkConfig().getAddresses().toString() + "::" +
    		   "admin::" +
    		   config.getNetworkConfig().isSmartRouting() + "::" + 
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
		
		config.setProperty("hazelcast.logging.type", "slf4j");
		//SecureCredentials creds = new SecureCredentials(user, password);
		SecureCredentials creds = new SecureCredentials(schema, password);
		//config.getSecurityConfig().setCredentials(creds);
		config.setCredentials(creds);

		XQProcessor proc = null;
		BagriXQDataFactory xqFactory = (BagriXQDataFactory) props.get(pn_data_factory);
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
		private Set<String> clientIds = new HashSet<>();
		
		ClientContainer(String clientKey, HazelcastInstance hzInstance) {
			this.clientKey = clientKey;
			this.hzInstance = hzInstance;
		}
		
		boolean addClient(String clientId) {
			return clientIds.add(clientId);
		}
		
		Set<String> getClients() {
			return clientIds;
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
		
	}
    
}
