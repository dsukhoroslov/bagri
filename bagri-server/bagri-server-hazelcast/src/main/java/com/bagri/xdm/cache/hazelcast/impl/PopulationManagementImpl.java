package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.XDMDocumentKey.*;
import static com.bagri.xdm.client.common.XDMCacheConstants.*;
import static com.bagri.common.config.XDMConfigConstants.*;
import static com.bagri.common.util.FileUtils.buildStoreFileName;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.task.schema.SchemaPopulator;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMTransaction;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
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
	private static final int szInt = 4;

    private String schemaName;
    private int populationSize;
    private NodeEngine nodeEngine;

	private IMap<Long, XDMTransaction> xtxCache;
	private IMap<XDMDocumentKey, XDMDocument> xddCache;
    
    private int bSize = 2048;
    private FileChannel fc;
	private RandomAccessFile raf;
    private MappedByteBuffer buff;
    private Map<Long, Long> documents;
    
	@Override
	public void init(NodeEngine nodeEngine, Properties properties) {
		logger.info("init; got properties: {}", properties); 
		this.nodeEngine = nodeEngine;
		this.schemaName = properties.getProperty(xdm_schema_name);
		this.populationSize = Integer.parseInt(properties.getProperty(xdm_schema_population_size));
		String dataPath = properties.getProperty(xdm_schema_store_data_path);
		String nodeNum = properties.getProperty(xdm_node_instance);
		String fileName = buildStoreFileName(dataPath, nodeNum, "catalog");
		String buffSize = properties.getProperty(xdm_schema_population_buffer_size);
		if (buffSize != null) {
			this.bSize = Integer.parseInt(buffSize);
		}
		logger.info("init; opening catalog from file: {}; buffer size: {} docs", fileName, bSize);
		
		nodeEngine.getPartitionService().addMigrationListener(this);
		nodeEngine.getHazelcastInstance().getCluster().addMembershipListener(this);
		nodeEngine.getHazelcastInstance().getLifecycleService().addLifecycleListener(this);
		nodeEngine.getHazelcastInstance().getUserContext().put("popManager", this);
		
		readCatalog(fileName);
	}
	
	@Override
	public void reset() {
		logger.info("reset"); 
	}

	@Override
	public void shutdown(boolean terminate) {
		logger.info("shutdown; terminate: {}", terminate);
		try {
			//buff.compact();
			fc.close();
			raf.close();
		} catch (IOException ex) {
			logger.error("shutdown.error", ex);
		}
	}

	public void checkPopulation(int currentSize) {
		logger.info("checkPopulation; populationSize: {}; currentSize: {}", populationSize, currentSize);
    	if (populationSize <= currentSize) {
    		SchemaPopulator pop = new SchemaPopulator(schemaName);
    		nodeEngine.getHazelcastInstance().getExecutorService(PN_XDM_SCHEMA_POOL).submitToMember(pop, 
    				nodeEngine.getLocalMember());
    	}
    }
	
	public int getDocumentCount() {
		return documents.size();
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
			xddCache.addEntryListener(this, true);
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
		checkPopulation(membershipEvent.getMembers().size());
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		logger.trace("memberRemoved; event: {}", membershipEvent);
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		logger.trace("memberAttributeChaged; event: {}", memberAttributeEvent);
	}

	@Override
	public void migrationStarted(MigrationEvent migrationEvent) {
		logger.trace("migrationStarted; event: {}", migrationEvent);
	}

	@Override
	public void migrationCompleted(MigrationEvent migrationEvent) {
		logger.trace("migrationCompleted; event: {}", migrationEvent);
	}

	@Override
	public void migrationFailed(MigrationEvent migrationEvent) {
		logger.trace("migrationFailed; event: {}", migrationEvent);
	}

	//@Override
	public void migrationInitialized(MigrationEvent migrationEvent) {
		logger.trace("migrationInitialized; event: {}", migrationEvent);
	}

	//@Override
	public void migrationFinalized(MigrationEvent migrationEvent) {
		logger.trace("migrationFinalized; event: {}", migrationEvent);
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
		long pos = buff.position();
		documents.put(event.getKey().getKey(), pos);
		buff.putInt(0, documents.size());
		buff.reset();
		writeDocument(event.getValue());
		buff.mark();
		logger.trace("entryAdded.exit; documents: {}", documents.size());
	}

	@Override
	public void entryUpdated(EntryEvent<XDMDocumentKey, XDMDocument> event) {
		logger.trace("entryUpdated; event: {}", event);
		Long pos = documents.get(event.getKey().getKey());
		if (pos != null) {
			buff.position(pos.intValue());
			long txFinish = event.getValue().getTxFinish(); 
			if (txFinish > 0) {
				buff.putLong(txFinish);
			} else {
				// just mark the doc as inactive
				buff.putLong(-1);
				buff.reset();
				entryAdded(event);
			}
		} else {
			logger.info("entryUpdated; unknown document: {}", event.getKey());
		}
	}

	@Override
	public void entryRemoved(EntryEvent<XDMDocumentKey, XDMDocument> event) {
		logger.trace("entryRemoved; event: {}", event);
	}

	@Override
	public void entryEvicted(EntryEvent<XDMDocumentKey, XDMDocument> event) {
		logger.trace("entryEvicted; event: {}", event);
	}

	@Override
	public void entryMerged(EntryEvent<XDMDocumentKey, XDMDocument> event) {
		logger.trace("entryMerged; event: {}", event);
	}

	private void readCatalog(String fileName) {
		int size = bSize*100; // document record size is raughly 100 bytes
		try {
			raf = new RandomAccessFile(fileName, "rw");
			if (raf.length() > 0) {
				logger.info("init; opened catalog with length: {}", raf.length());
				if (raf.length() > size) {
					size += (int) raf.length();
				}
			}
		    int docCount = 0;
			fc = raf.getChannel();
			buff = fc.map(MapMode.READ_WRITE, 0, size);
			if (raf.length() > 0) {
				docCount = buff.getInt();
				documents = new HashMap<>(docCount);
			} else {
				buff.position(szInt);
				documents = new HashMap<>();
			}
			logger.info("readCatalog; doc buffer initialized; doc count: {}", docCount);
			int actCount = loadDocuments(docCount);
			logger.info("readCatalog; documents loaded; active count: {}", actCount);
		} catch (IOException ex) {
			logger.error("init.error", ex);
			throw new RuntimeException("Cannot read catalog", ex);
		}
	}

	private int loadDocuments(int docCount) {
		logger.trace("loadDocuments.enter; docCount: {}", docCount);
		int idx = 0;
		int actCount = 0;
		documents.clear();
		//buff.position(szInt);
		while (idx < docCount) {
			int pos = buff.position();
			XDMDocument xdoc = readDocument();
			documents.put(xdoc.getDocumentKey(), (long) pos);
			idx++;
			if (xdoc.getTxFinish() == 0) {
				actCount++;
			}
		}
		//buff.position(nextBit()); 
		buff.mark();
		logger.trace("loadDocuments.exit; active docs: {}; documents: {}", actCount, documents);
		return actCount;
	}

	private XDMDocument readDocument() {
		long txFinish = buff.getLong();
		long docKey = buff.getLong();
		String uri = getString(buff);
		int typeId = buff.getInt();
		long txStart = buff.getLong();
		Date createdAt = new Date(buff.getLong());
		String createdBy = getString(buff);
		String encoding = getString(buff);
		long documentId = toDocumentId(docKey);
		int version = toVersion(docKey);
		// collections
		return new XDMDocument(documentId, version, uri, typeId, txStart, txFinish, createdAt, createdBy, encoding);
	}

	private void writeDocument(XDMDocument xdoc) {
		buff.putLong(xdoc.getTxFinish());
		buff.putLong(xdoc.getDocumentKey());
		putString(buff, xdoc.getUri());
		buff.putInt(xdoc.getTypeId());
		buff.putLong(xdoc.getTxStart());
		buff.putLong(xdoc.getCreatedAt().getTime());
		putString(buff, xdoc.getCreatedBy());
		putString(buff, xdoc.getEncoding());
		// collections
	}
	
	private String getString(MappedByteBuffer buff) {
		int len = buff.getInt();
		byte[] str = new byte[len];
		buff.get(str);
		return new String(str);
	}

	private void putString(MappedByteBuffer buff, String val) {
		byte[] str = val.getBytes();
		buff.putInt(str.length);
		buff.put(str);
	}

}

