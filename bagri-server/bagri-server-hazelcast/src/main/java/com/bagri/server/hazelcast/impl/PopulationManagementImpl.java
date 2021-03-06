package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.server.hazelcast.util.SpringContextHolder.*;
import static com.hazelcast.spi.ExecutionService.MAP_LOADER_EXECUTOR;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.map.impl.MapContainer;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.map.impl.MapServiceContext;
import com.hazelcast.map.impl.MapStoreWrapper;
import com.hazelcast.map.impl.mapstore.MapStoreContext;
import com.hazelcast.map.impl.recordstore.RecordStore;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryMergedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;
import com.hazelcast.spi.ExecutionService;
//import com.hazelcast.map.listener.MapListener;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.executor.ManagedExecutorService;

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
    private int popClusterSize = 0;
    private boolean allowPopulation = true;

    private AtomicLong startTime = new AtomicLong(0);
    private AtomicLong stopTime = new AtomicLong(0);
    private AtomicInteger startedBatchCount = new AtomicInteger(0);
    private AtomicInteger finishedBatchCount = new AtomicInteger(0);
    private AtomicInteger errorCount = new AtomicInteger(0);
    private AtomicInteger loadingCount = new AtomicInteger(0);
    private AtomicInteger loadedCount = new AtomicInteger(0);
    private Map<String, String> loaders = new ConcurrentHashMap<>(16); 
    
    private NodeEngine nodeEngine;
    private KeyFactory xFactory;
	private IMap<Long, Transaction> xtxCache;
    private IMap<DocumentKey, String> keyCache;
	//private IMap<DocumentKey, Document> xddCache;
	private IMap xddCache;

    private boolean useCatalog = false;
	private DocumentMemoryStore catalog;

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
		this.popClusterSize = Integer.parseInt(properties.getProperty(pn_schema_population_size, "0"));
		this.useCatalog = Boolean.parseBoolean(properties.getProperty(pn_schema_population_use_catalog, "true"));
		if (useCatalog) {
			String dataPath = properties.getProperty(pn_schema_store_data_path);
			String nodeNum = properties.getProperty(pn_node_instance);
			int buffSize = 2048*100;
			String bSize = properties.getProperty(pn_schema_population_buffer_size);
			if (bSize != null) {
				buffSize = Integer.parseInt(bSize);
			}
			logger.info("init; will open doc store from path: {}; instance: {}; buffer size: {} docs", dataPath, nodeNum, buffSize);
			catalog = new DocumentMemoryStore(dataPath, nodeNum, buffSize);
		}
			
		nodeEngine.getPartitionService().addMigrationListener(this);
		nodeEngine.getHazelcastInstance().getCluster().addMembershipListener(this);
		nodeEngine.getHazelcastInstance().getLifecycleService().addLifecycleListener(this);
		nodeEngine.getHazelcastInstance().getUserContext().put(ctx_popService, this);
	}
	
	@Override
	public void reset() {
		logger.info("reset"); 
		allowPopulation = true;
		clearLoadStats();
	}

	@Override
	public void shutdown(boolean terminate) {
		logger.info("shutdown; terminate: {}", terminate);
		if (useCatalog) {
			catalog.close();
		}
	}

	public void checkPopulation(int currentSize) throws Exception {
		logger.info("checkPopulation; will start population at {} cluster size; current size is: {}", popClusterSize, currentSize);
		if (useCatalog) {
			activateDocStore();
			xddCache.addEntryListener(this, true);
		}
    	if (popClusterSize > 0 && popClusterSize == currentSize && getLoadedCount() == 0) {
    		SchemaPopulator pop = new SchemaPopulator(schemaName, false, false);
    		// we can't call it directly as it'll block current thread for a long time..
    		nodeEngine.getHazelcastInstance().getExecutorService(PN_XDM_SCHEMA_POOL).submitToAllMembers(pop);
    	}
    }
	
	public void clearLoadStats() {
		startTime.set(0);
		startedBatchCount.set(0);
		finishedBatchCount.set(0);
		errorCount.set(0);
		loadingCount.set(0);
		loadedCount.set(0);
		stopTime.set(0);
		loaders.clear();
	}
	
	public Document getDocument(Long docKey) {
		if (useCatalog) {
			return catalog.getEntry(docKey);
		} 
		return null;
	}
	
	public int getActiveCount() {
		if (useCatalog) {
			return catalog.getActiveEntryCount();
		}
		return 0;
	}
	
	public int getDocumentCount() {
		if (useCatalog) {
			return catalog.getFullEntryCount();
		}
		return 0;
	}
	
	public int getStartedBatchCount() {
		return startedBatchCount.get();
	}
	
	public int getFinishedBatchCount() {
		return finishedBatchCount.get();
	}
	
	public int getErrorCount() {
		return errorCount.get();
	}
	
	public int getKeyCount() {
		return keyCache.localKeySet().size();
	}
	
	public int getLoadingCount() {
		return loadingCount.get();
	}
	
	public int getLoadedCount() {
		return loadedCount.get();
	}
	
	public int getLoadThreadCount() {
		return loaders.size();
	}
	
	public long getStartTime() {
		return startTime.get();
	}
	
	public long getLastTime() {
		return stopTime.get();
	}
	
	public int getUpdatingDocumentCount() {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext xddCtx = svc.getMapServiceContext();
		// this does not work for some reason
		//return xddCtx.getWriteBehindQueueItemCounter().get();
		int cnt = 0;
		List<Integer> parts = nodeEngine.getPartitionService().getMemberPartitions(nodeEngine.getThisAddress());
		for (int part: parts) {
			RecordStore rs = xddCtx.getRecordStore(part, CN_XDM_DOCUMENT);
			if (rs != null) {
				cnt += rs.getMapDataStore().notFinishedOperationsCount();
			}
		}
		return cnt;
	}
	
	public Set<DocumentKey> getDocumentKeys() {
		if (useCatalog && !catalog.getEntryKeys().isEmpty()) {
			Set<DocumentKey> result = new HashSet<>();
			KeyFactory factory = getKeyFactory();
			for (Long docKey: catalog.getEntryKeys()) {
				result.add(factory.newDocumentKey(docKey));
			}
			logger.info("getDocumentKeys; returning {} keys, out of total {}", result.size(), catalog.getEntryKeys().size());
			return result;
		}
		return null;
	}
	
	public long getMaxTransactionId() {
		if (useCatalog) {
			return catalog.getMaxTransactionId();
		}
		return 0; //TX_NO;
	}
	
	public void addLoadingCounts(int loading) {
		startTime.compareAndSet(0, nodeEngine.getClusterService().getClusterTime());
		startedBatchCount.incrementAndGet();
		loadingCount.addAndGet(loading);
		stopTime.set(nodeEngine.getClusterService().getClusterTime());
		loaders.putIfAbsent(Thread.currentThread().getName(), "" + Thread.currentThread().getId());
	}
	
	public void addLoadedCounts(int errors, int loaded) {
		finishedBatchCount.incrementAndGet();
		errorCount.addAndGet(errors);
		loadingCount.addAndGet(-errors - loaded);
		loadedCount.addAndGet(loaded);
		stopTime.set(nodeEngine.getClusterService().getClusterTime());
	}
	
	private void activateDocStore() {

		if (catalog.isActivated()) {
			logger.info("activateDocStore; the document store has been already activated");
			return;
		}

		catalog.init(xddCache);

		//ApplicationContext schemaCtx = getContext(schemaName);
		//docMgr = schemaCtx.getBean(DocumentManagementImpl.class);

		KeyFactory factory = getKeyFactory();
		Collection<Long> docKeys = catalog.getEntryKeys();
		Map<DocumentKey, String> mappings = new HashMap<>(docKeys.size());
		for (Long docKey: docKeys) {
			Document doc = catalog.getEntry(docKey);
			if (doc != null && doc.isActive()) {
				mappings.put(factory.newDocumentKey(docKey), doc.getUri());
			}
		}
		setKeyMappings(mappings);
	}
	
	private KeyFactory getKeyFactory() {
		if (xFactory == null) {
			ApplicationContext schemaCtx = getContext(schemaName);
			xFactory = schemaCtx.getBean("xdmFactory", KeyFactory.class);
		}
		return xFactory;
	}

	public MapStore getMapStore(String mapName) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapContainer mc = svc.getMapServiceContext().getMapContainer(mapName);
		if (mc != null) {
			MapStoreContext msc = mc.getMapStoreContext();
			if (msc != null) {
				MapStoreWrapper msw = msc.getMapStoreWrapper();
				if (msw != null) {
					return (MapStore) msw.getImpl();
				}
			}
		}
		return null;
	}
	
	public boolean isPopulationAllowed() {
		return allowPopulation;
	}
	
	public boolean populateSchema(boolean overrideExisting) {
		clearLoadStats();
		allowPopulation = true;
		startTime.set(nodeEngine.getClusterService().getClusterTime());

		int partId = nodeEngine.getPartitionService().getPartitionId(CN_XDM_DOCUMENT);
		if (nodeEngine.getPartitionService().isPartitionOwner(partId)) {
			// don't load transactions if schema non-transactional?
			xtxCache.loadAll(false);
	    	logger.info("populateSchema; transactions size after loadAll: {}", xtxCache.size());
	
			xddCache.loadAll(overrideExisting);
	    	logger.info("populateSchema; documents size after loadAll: {}", xddCache.size());
			stopTime.set(nodeEngine.getClusterService().getClusterTime());
			return true;
		}
		return false;
	}
	
	public void stopPopulation() {
		ExecutionService svc = nodeEngine.getExecutionService(); 
		ManagedExecutorService mes = svc.getExecutor(MAP_LOADER_EXECUTOR);
		if (mes != null) {
	    	logger.info("stopPopulation; stopping.. completed load tasks: {}; remaining load tasks: {}", 
	    			mes.getCompletedTaskCount(), mes.getQueueSize());
		}
		allowPopulation = false;
		stopTime.set(nodeEngine.getClusterService().getClusterTime());
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
		logger.info("memberRemoved; event: {}", membershipEvent);
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
		boolean added = catalog.putEntry(event.getKey().getKey(), event.getValue(), false);
		logger.trace("entryAdded.exit; added: {}", added);
	}

	@Override
	public void entryUpdated(EntryEvent<DocumentKey, Document> event) {
		logger.trace("entryUpdated.enter; event: {}", event);
		boolean updated;
		//if (event.getValue().getVersion() > event.getOldValue().getVersion()) {
			updated = catalog.putEntry(event.getKey().getKey(), event.getValue(), true);
		//} else {
			// update it inplace as the entry size shouldn't be changed
		//	updated = docStore.updateEntry(event.getKey().getKey(), event.getValue());
		//}
		logger.trace("entryUpdated.exit; updated: {}", updated);
	}

	@Override
	public void entryRemoved(EntryEvent<DocumentKey, Document> event) {
		logger.trace("entryRemoved.enter; event: {}", event);
		boolean removed = catalog.clearEntry(event.getKey().getKey(), event.getValue(), true);
		logger.trace("entryRemoved.exit; removed: {}", removed);
	}

	@Override
	public void entryEvicted(EntryEvent<DocumentKey, Document> event) {
		logger.trace("entryEvicted.enter; event: {}", event);
	}

	@Override
	public void entryMerged(EntryEvent<DocumentKey, Document> event) {
		logger.trace("entryMerged; event: {}", event);
	}

}

