package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_client_memberId;
import static com.bagri.core.Constants.pn_schema_user;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.server.api.ClientManagement;
import com.bagri.support.stats.StatisticsEvent;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.ReplicatedMap;

public class ClientManagementImpl implements ClientManagement, ClientListener, EntryListener<String, Properties> {
	
	private static final transient Logger logger = LoggerFactory.getLogger(ClientManagementImpl.class);
	
	private SchemaRepositoryImpl repo;
    private HazelcastInstance hzInstance;
	//private IMap<String, Properties> clientsCache;
	private ReplicatedMap<String, Properties> clientsCache;
	
    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;
	
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		hzInstance.getClientService().addClientListener(this);
		logger.trace("setHzInstange; got instance: {}", hzInstance.getName());
	}
	
	public void setClientsCache(ReplicatedMap<String, Properties> clientsCache) {
		this.clientsCache = clientsCache;
		clientsCache.addEntryListener(this); //, false);
	}
	
    public void setRepository(SchemaRepositoryImpl repo) {
    	this.repo = repo;
    }	
	
    public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
    	this.queue = queue;
    }

    public void setStatsEnabled(boolean enable) {
    	this.enableStats = enable;
    }

	private void updateStats(String name, boolean success, long duration) {
		if (enableStats) {
			if (!queue.offer(new StatisticsEvent(name, success, new Object[] {duration}))) {
				logger.warn("updateStats; queue is full!!");
			}
		}
	}
    
	@Override
	public String[] getClients() {
		Set<String> clients = clientsCache.keySet(); 
		return clients.toArray(new String[clients.size()]);
	}

	public String getClientUser(String clientId) {
		Properties props = clientsCache.get(clientId);
		if (props != null) {
			return props.getProperty(pn_schema_user);
		}
		return null;
	}
	
	@Override
	public String getCurrentUser() {
		String clientId = repo.getClientId();
		logger.trace("getCurrentUser.enter; client: {}", clientId); 
		if (clientId != null) {
			return getClientUser(clientId);
		}
		return null;
	}
	
	@Override
	public Properties getClientProperties(String clientId) {
		return clientsCache.get(clientId);
	}
	
	@Override
	public void clientConnected(Client client) {
		String clientId = client.getUuid();
		logger.info("clientConnected.enter; client: {}", clientId); 
		// create queue
		//IQueue queue = hzInstance.getQueue("client:" + clientId);
		// create/cache new XQProcessor
		//XQProcessor proc = getXQProcessor(client.getUuid());
		//logger.trace("clientConnected.exit; queue {} created for client: {}; XQProcessor: {}", 
		//		queue.getName(), clientId, proc);
	}

	@Override
	public void clientDisconnected(Client client) {
		String clientId = client.getUuid();
		logger.trace("clientDisconnected.enter; client: {}", clientId);
		
		// TODO: repair this!
		//PropertyPredicate pp = new PropertyPredicate(pn_client_memberId, clientId);
		//Set<String> members = clientsCache.localKeySet(pp);
		//for (String member: members) {
		//	clientsCache.delete(member);
		//}
		
		// TODO: check and destroy client's resources

		//removeClient(clientId);
		//XQProcessor proc = processors.remove(client.getUuid());
	}
	
	private boolean removeClient(String clientId) {
		String qName = "client:" + clientId;
		boolean removed = false;
		java.util.Collection<DistributedObject> all = hzInstance.getDistributedObjects();
		int sizeBefore = all.size();
		for (DistributedObject obj: all) {
			if (qName.equals(obj.getName())) {
				// remove queue
				obj.destroy();
				removed = true;
				break;
			}
		}
		int sizeAfter = hzInstance.getDistributedObjects().size(); 
		logger.debug("removeClient.exit; queue {} {} for client: {}; size before: {}, after: {}", 
				qName, removed ? "destroyed" : "skipped", clientId, sizeBefore, sizeAfter); 
		return removed;
	}

	@Override
	public void entryAdded(EntryEvent<String, Properties> event) {
		logger.trace("clientAdded.enter; key: {}; value: {}", event.getKey(), event.getValue());
	}

	@Override
	public void entryUpdated(EntryEvent<String, Properties> event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void entryRemoved(EntryEvent<String, Properties> event) {
		logger.trace("clientRemoved.enter; key: {}; value: {}", event.getKey(), event.getValue());
		removeClient(event.getKey());
	}

	@Override
	public void entryEvicted(EntryEvent<String, Properties> event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mapCleared(MapEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mapEvicted(MapEvent event) {
		// TODO Auto-generated method stub
	}

}
