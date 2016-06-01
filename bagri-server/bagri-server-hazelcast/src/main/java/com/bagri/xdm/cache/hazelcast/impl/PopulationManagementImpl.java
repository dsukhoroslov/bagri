package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.*;
import static com.bagri.xdm.cache.api.XDMCacheConstants.*;
import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.*;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.cache.hazelcast.store.DocumentMemoryStore;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaPopulator;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMKeyFactory;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMTransaction;
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

public class PopulationManagementImpl implements ManagedService, 
	MembershipListener, MigrationListener, LifecycleListener,
	MapClearedListener, MapEvictedListener, 
	EntryAddedListener<XDMDocumentKey, XDMDocument>, 
	EntryEvictedListener<XDMDocumentKey, XDMDocument>, 
	EntryUpdatedListener<XDMDocumentKey, XDMDocument>, 
	EntryRemovedListener<XDMDocumentKey, XDMDocument>, 
	EntryMergedListener<XDMDocumentKey, XDMDocument>  { 

    private static final transient Logger logger = LoggerFactory.getLogger(PopulationManagementImpl.class);

    private boolean enabled;
    private String schemaName;
    private int populationSize;
    private NodeEngine nodeEngine;

    private XDMKeyFactory xFactory;
	private IMap<Long, XDMTransaction> xtxCache;
	//private IMap<XDMDocumentKey, XDMDocument> xddCache;
	private IMap xddCache;
	private DocumentMemoryStore docStore;
	private DocumentManagementImpl docMgr;

	@Override
	public void init(NodeEngine nodeEngine, Properties properties) {
		logger.info("init; got properties: {}", properties); 
		this.nodeEngine = nodeEngine;
		this.schemaName = properties.getProperty(xdm_schema_name);
		this.populationSize = Integer.parseInt(properties.getProperty(xdm_schema_population_size));
		this.enabled = Boolean.parseBoolean(properties.getProperty(xdm_schema_store_enabled));
		String dataPath = properties.getProperty(xdm_schema_store_data_path);
		String nodeNum = properties.getProperty(xdm_node_instance);
		int buffSize = 2048*100;
		String bSize = properties.getProperty(xdm_schema_population_buffer_size);
		if (bSize != null) {
			buffSize = Integer.parseInt(bSize);
		}
		if (enabled) {
			logger.info("init; will open doc store from path: {}; instance: {}; buffer size: {} docs", dataPath, nodeNum, buffSize);
			docStore = new DocumentMemoryStore(dataPath, nodeNum, buffSize);
			
			nodeEngine.getPartitionService().addMigrationListener(this);
			nodeEngine.getHazelcastInstance().getCluster().addMembershipListener(this);
			nodeEngine.getHazelcastInstance().getLifecycleService().addLifecycleListener(this);
			nodeEngine.getHazelcastInstance().getUserContext().put("popManager", this);
		} else {
			logger.info("init; persistent store is disabled");
		}
	}
	
	@Override
	public void reset() {
		logger.info("reset"); 
	}

	@Override
	public void shutdown(boolean terminate) {
		logger.info("shutdown; terminate: {}", terminate);
		if (enabled) {
			docStore.close();
		}
	}

	public void checkPopulation(int currentSize) {
		logger.info("checkPopulation; populationSize: {}; currentSize: {}", populationSize, currentSize);
		if (enabled) {
			activateDocStore();
			xddCache.addEntryListener(this, true);
		}
    	if (populationSize == currentSize && xddCache.size() == 0) {
    		SchemaPopulator pop = new SchemaPopulator(schemaName);
    		nodeEngine.getHazelcastInstance().getExecutorService(PN_XDM_SCHEMA_POOL).submitToMember(pop, nodeEngine.getLocalMember());
    		//nodeEngine.getHazelcastInstance().getExecutorService(PN_XDM_SCHEMA_POOL).submitToAllMembers(pop); 
    	}
    }
	
	public XDMDocument getDocument(Long docKey) {
		return enabled ? docStore.getEntry(docKey) : null;
	}
	
	public int getDocumentCount() {
		return enabled ? docStore.getFullEntryCount() : 0;
	}
	
	public Set<XDMDocumentKey> getDocumentKeys() {
		if (!enabled) {
			return null;
		}
		
		if (docStore.getEntryKeys().size() == 0) {
			return null;
		}
		
		Set<XDMDocumentKey> result = new HashSet<>();
		XDMKeyFactory factory = getXDMFactory();
		for (Long docKey: docStore.getEntryKeys()) {
			result.add(factory.newXDMDocumentKey(docKey));
		}
		logger.info("getDocumentKeys; returning {} keys", result.size());
		return result;
	}
	
	private void activateDocStore() {

		if (docStore.isActivated()) {
			logger.info("activateDocStore; the document store has been already activated");
			return;
		}

		docStore.init(xddCache);

		ApplicationContext schemaCtx = (ApplicationContext) getContext(schemaName, schema_context);
		docMgr = schemaCtx.getBean(DocumentManagementImpl.class);
		
		// only local HM should be notified!
		HealthManagementImpl hMgr = schemaCtx.getBean(HealthManagementImpl.class);
		int actCount = docStore.getActiveEntryCount();
		int docCount = docStore.getFullEntryCount();
		hMgr.initState(actCount, docCount - actCount);
	}
	
	private XDMKeyFactory getXDMFactory() {
		if (xFactory == null) {
			ApplicationContext schemaCtx = (ApplicationContext) getContext(schemaName, schema_context);
			xFactory = schemaCtx.getBean("xdmFactory", XDMKeyFactory.class);
		}
		return xFactory;
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
		logger.info("memberRemoved; event: {}; docs size: {}", membershipEvent, xddCache.size());
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		logger.trace("memberAttributeChaged; event: {}", memberAttributeEvent);
	}

	@Override
	public void migrationStarted(MigrationEvent migrationEvent) {
		logger.info("migrationStarted; event: {}; docs size: {}", migrationEvent); //, xddCache.size());
	}

	@Override
	public void migrationCompleted(MigrationEvent migrationEvent) {
		logger.info("migrationCompleted; event: {}; docs size: {}", migrationEvent); //), xddCache.size());
	}

	@Override
	public void migrationFailed(MigrationEvent migrationEvent) {
		logger.info("migrationFailed; event: {}; docs size: {}", migrationEvent); //, xddCache.size());
	}

	//@Override
	public void migrationInitialized(MigrationEvent migrationEvent) {
		logger.info("migrationInitialized; event: {}", migrationEvent);
	}

	//@Override
	public void migrationFinalized(MigrationEvent migrationEvent) {
		logger.info("migrationFinalized; event: {}", migrationEvent);
	}

	@Override
	public void mapEvicted(MapEvent event) {
		logger.trace("mapEvicted; event: {}", event);
	}

	@Override
	public void mapCleared(MapEvent event) {
		logger.trace("mapCleared; event: {}", event);
	}

	@Override
	public void entryAdded(EntryEvent<XDMDocumentKey, XDMDocument> event) {
		logger.trace("entryAdded.enter; event: {}", event);
		boolean added = docStore.putEntry(event.getKey().getKey(), event.getValue(), false);
		logger.trace("entryAdded.exit; added: {}", added);
	}

	@Override
	public void entryUpdated(EntryEvent<XDMDocumentKey, XDMDocument> event) {
		logger.trace("entryUpdated.enter; event: {}", event);
		boolean updated = docStore.putEntry(event.getKey().getKey(), event.getValue(), true);
		logger.trace("entryUpdated.exit; updated: {}", updated);
	}

	@Override
	public void entryRemoved(EntryEvent<XDMDocumentKey, XDMDocument> event) {
		logger.trace("entryRemoved.enter; event: {}", event);
		boolean removed = docStore.clearEntry(event.getKey().getKey(), event.getValue(), true);
		logger.trace("entryRemoved.exit; removed: {}", removed);
	}

	@Override
	public void entryEvicted(EntryEvent<XDMDocumentKey, XDMDocument> event) {
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
	public void entryMerged(EntryEvent<XDMDocumentKey, XDMDocument> event) {
		logger.trace("entryMerged; event: {}", event);
	}

}

