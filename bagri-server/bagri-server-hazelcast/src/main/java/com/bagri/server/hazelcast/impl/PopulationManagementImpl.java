package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.server.hazelcast.util.SpringContextHolder.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.bagri.core.DocumentKey;
import com.bagri.core.KeyFactory;
import com.bagri.core.model.Document;
import com.bagri.core.model.Transaction;
import com.bagri.core.server.api.PopulationManagement;
import com.bagri.server.hazelcast.store.DocumentMemoryStore;
import com.bagri.server.hazelcast.task.schema.SchemaPopulator;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryMergedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;
//import com.hazelcast.map.listener.MapListener;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;

public class PopulationManagementImpl implements PopulationManagement, ManagedService, 
	MembershipListener, MigrationListener, LifecycleListener,
	MapClearedListener, MapEvictedListener, 
	EntryAddedListener<DocumentKey, Document>, 
	EntryEvictedListener<DocumentKey, Document>, 
	EntryUpdatedListener<DocumentKey, Document>, 
	EntryRemovedListener<DocumentKey, Document>, 
	EntryMergedListener<DocumentKey, Document>  { 

    private static final transient Logger logger = LoggerFactory.getLogger(PopulationManagementImpl.class);

    private String schemaName;
    private int populationSize;
    private NodeEngine nodeEngine;

    private KeyFactory xFactory;
	private IMap<Long, Transaction> xtxCache;
    private IMap<DocumentKey, String> keyCache;
	//private IMap<DocumentKey, Document> xddCache;
	private IMap xddCache;
	private DocumentMemoryStore docStore;
	private DocumentManagementImpl docMgr;

	@Override
	public String getKeyMapping(DocumentKey key) {
		return keyCache.get(key);
	}

	@Override
	public void setKeyMapping(DocumentKey key, String mapping) {
		keyCache.put(key, mapping);
	}

	@Override
	public Map<DocumentKey, String> getKeyMappings(Set<DocumentKey> keys) {
		return keyCache.getAll(keys);
	}

	@Override
	public void setKeyMappings(Map<DocumentKey, String> mappings) {
		keyCache.putAll(mappings);
	}

	@Override
	public String deleteKeyMapping(DocumentKey key) {
		return keyCache.remove(key);
	}

	@Override
	public int deleteKeyMappings(Set<DocumentKey> keys) {
		int cnt = 0;
		for (DocumentKey key: keys) {
			if (keyCache.remove(key) != null) {
				cnt++;
			}
		}
		return cnt;
	}

	@Override
	public void init(NodeEngine nodeEngine, Properties properties) {
		logger.info("init; got properties: {}", properties); 
		this.nodeEngine = nodeEngine;
		this.schemaName = properties.getProperty(pn_schema_name);
		this.populationSize = Integer.parseInt(properties.getProperty(pn_schema_population_size));
		String dataPath = properties.getProperty(pn_schema_store_data_path);
		String nodeNum = properties.getProperty(pn_node_instance);
		int buffSize = 2048*100;
		String bSize = properties.getProperty(pn_schema_population_buffer_size);
		if (bSize != null) {
			buffSize = Integer.parseInt(bSize);
		}
		logger.info("init; will open doc store from path: {}; instance: {}; buffer size: {} docs", dataPath, nodeNum, buffSize);
		docStore = new DocumentMemoryStore(dataPath, nodeNum, buffSize);
			
		nodeEngine.getPartitionService().addMigrationListener(this);
		nodeEngine.getHazelcastInstance().getCluster().addMembershipListener(this);
		nodeEngine.getHazelcastInstance().getLifecycleService().addLifecycleListener(this);
		nodeEngine.getHazelcastInstance().getUserContext().put("popManager", this);
	}
	
	@Override
	public void reset() {
		logger.info("reset"); 
	}

	@Override
	public void shutdown(boolean terminate) {
		logger.info("shutdown; terminate: {}", terminate);
		docStore.close();
	}

	public void checkPopulation(int currentSize) throws Exception {
		logger.info("checkPopulation; populationSize: {}; currentSize: {}", populationSize, currentSize);
		activateDocStore();
		xddCache.addEntryListener(this, true);
    	if (populationSize == currentSize && xddCache.size() == 0) {
    		SchemaPopulator pop = new SchemaPopulator(schemaName);
    		// try to run it from the same thread..
    		nodeEngine.getHazelcastInstance().getExecutorService(PN_XDM_SCHEMA_POOL).submitToMember(pop, nodeEngine.getLocalMember());
    		//pop.call();
    	}
    }
	
	public Document getDocument(Long docKey) {
		return docStore.getEntry(docKey);
	}
	
	public int getActiveCount() {
		return docStore.getActiveEntryCount();
	}
	
	public int getDocumentCount() {
		return docStore.getFullEntryCount();
	}
	
	public Set<DocumentKey> getDocumentKeys() {
		if (docStore.getEntryKeys().size() == 0) {
			return null;
		}
		
		Set<DocumentKey> result = new HashSet<>();
		KeyFactory factory = getKeyFactory();
		for (Long docKey: docStore.getEntryKeys()) {
			result.add(factory.newDocumentKey(docKey));
		}
		logger.info("getDocumentKeys; returning {} keys", result.size());
		return result;
	}
	
	public long getMaxTransactionId() {
		return docStore.getMaxTransactionId();
	}
	
	private void activateDocStore() {

		if (docStore.isActivated()) {
			logger.info("activateDocStore; the document store has been already activated");
			return;
		}

		docStore.init(xddCache);

		ApplicationContext schemaCtx = (ApplicationContext) getContext(schemaName, schema_context);
		docMgr = schemaCtx.getBean(DocumentManagementImpl.class);
	}
	
	private KeyFactory getKeyFactory() {
		if (xFactory == null) {
			ApplicationContext schemaCtx = (ApplicationContext) getContext(schemaName, schema_context);
			xFactory = schemaCtx.getBean("xdmFactory", KeyFactory.class);
		}
		return xFactory;
	}
	
	private int checkPersistenceQueue() {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		return svc.getMapServiceContext().getWriteBehindQueueItemCounter().get();
	}
	
	//public ManagedService getHzService(String serviceName, String instanceName) {
	//	return nodeEngine.getHazelcastInstance().getDistributedObject(serviceName, instanceName);
	//}
	
	@Override
	public void stateChanged(LifecycleEvent event) {
		logger.info("stateChanged; event: {}", event);
		if (LifecycleState.STARTED == event.getState()) {
			xtxCache = nodeEngine.getHazelcastInstance().getMap(CN_XDM_TRANSACTION);
			xddCache = nodeEngine.getHazelcastInstance().getMap(CN_XDM_DOCUMENT);
			keyCache = nodeEngine.getHazelcastInstance().getMap(CN_XDM_KEY);
			//readCatalog(catalog);
			// too early
			//checkPopulation(nodeEngine.getClusterService().getSize());
		} else if (LifecycleState.SHUTTING_DOWN == event.getState()) {
			xtxCache.flush();
			xddCache.flush();
			logger.info("stateChanged; Maps were flushed");
		}
	}

	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		logger.info("memberAdded; event: {}", membershipEvent);
		// this does not work, unfortunately
		//if (membershipEvent.getMember().localMember()) {
		//	IMap<XDMDocumentKey, XDMDocument> xddCache = nodeEngine.getHazelcastInstance().getMap(CN_XDM_DOCUMENT);
		//	xddCache.addEntryListener(this, true);
		//}
		//checkPopulation(membershipEvent.getMembers().size());
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		logger.info("memberRemoved; event: {}; docs size: {}; active count: {}", membershipEvent, xddCache.size(), docStore.getActiveEntryCount());
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		logger.trace("memberAttributeChaged; event: {}", memberAttributeEvent);
	}

	@Override
	public void migrationStarted(MigrationEvent migrationEvent) {
		logger.debug("migrationStarted; event: {}", migrationEvent); 
	}

	@Override
	public void migrationCompleted(MigrationEvent migrationEvent) {
		logger.debug("migrationCompleted; event: {}", migrationEvent); 
	}

	@Override
	public void migrationFailed(MigrationEvent migrationEvent) {
		logger.debug("migrationFailed; event: {};", migrationEvent); 
	}

	//@Override
	//public void migrationInitialized(MigrationEvent migrationEvent) {
	//	logger.debug("migrationInitialized; event: {}", migrationEvent);
	//}

	//@Override
	//public void migrationFinalized(MigrationEvent migrationEvent) {
	//	logger.debug("migrationFinalized; event: {}", migrationEvent);
	//}

	@Override
	public void mapEvicted(MapEvent event) {
		logger.trace("mapEvicted; event: {}", event);
	}

	@Override
	public void mapCleared(MapEvent event) {
		logger.trace("mapCleared; event: {}", event);
	}

	@Override
	public void entryAdded(EntryEvent<DocumentKey, Document> event) {
		logger.trace("entryAdded.enter; event: {}", event);
		boolean added = docStore.putEntry(event.getKey().getKey(), event.getValue(), false);
		logger.trace("entryAdded.exit; added: {}", added);
	}

	@Override
	public void entryUpdated(EntryEvent<DocumentKey, Document> event) {
		logger.trace("entryUpdated.enter; event: {}", event);
		boolean updated = docStore.putEntry(event.getKey().getKey(), event.getValue(), true);
		logger.trace("entryUpdated.exit; updated: {}", updated);
	}

	@Override
	public void entryRemoved(EntryEvent<DocumentKey, Document> event) {
		logger.trace("entryRemoved.enter; event: {}", event);
		boolean removed = docStore.clearEntry(event.getKey().getKey(), event.getValue(), true);
		logger.trace("entryRemoved.exit; removed: {}", removed);
	}

	@Override
	public void entryEvicted(EntryEvent<DocumentKey, Document> event) {
		logger.trace("entryEvicted.enter; event: {}", event);
		// evict all document relatives: content, elements. do this on document owning node only. 
		// what about indices and results? what about older document versions? 
		// use document container for versions..
		boolean evicted = false;
		int partId = nodeEngine.getPartitionService().getPartitionId(event.getKey());
		if (nodeEngine.getPartitionService().isPartitionOwner(partId)) {
			// what if we're in migration right now? nodeEngine.getPartitionService().getActiveMigrations()
			docMgr.evictDocument(event.getKey(), event.getValue());
			evicted = true;
		}
		logger.trace("entryEvicted.exit; partition: {}; evicted: {}", partId, evicted);
	}

	@Override
	public void entryMerged(EntryEvent<DocumentKey, Document> event) {
		logger.trace("entryMerged; event: {}", event);
	}

}

