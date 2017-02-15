package com.bagri.server.hazelcast.impl;

import static com.bagri.core.server.api.CacheConstants.CN_XDM_CONTENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_ELEMENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_INDEX;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_RESULT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.PartitionStatistics;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.map.impl.MapServiceContext;
import com.hazelcast.map.impl.recordstore.RecordStore;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;

public class DataDistributionService implements ManagedService {

    private static final transient Logger logger = LoggerFactory.getLogger(DataDistributionService.class);

    private NodeEngine nodeEngine;
    
	@Override
	public void init(NodeEngine nodeEngine, Properties properties) {
		logger.info("init; properties: {}", properties);
		this.nodeEngine = nodeEngine;
	}

	@Override
	public void reset() {
		logger.info("reset"); 
	}

	@Override
	public void shutdown(boolean terminate) {
		logger.info("shutdown; terminate: {}", terminate); 
	}
	
	public Collection<PartitionStatistics> getPartitionStatistics() {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		List<Integer> parts = nodeEngine.getPartitionService().getMemberPartitions(nodeEngine.getThisAddress());
		String address = nodeEngine.getThisAddress().toString();
		List<PartitionStatistics> stats = new ArrayList<>(parts.size());
		for (int part: parts) {
			RecordStore drs = mapCtx.getRecordStore(part, CN_XDM_DOCUMENT);
			RecordStore crs = mapCtx.getRecordStore(part, CN_XDM_CONTENT);
			RecordStore ers = mapCtx.getRecordStore(part, CN_XDM_ELEMENT);
			RecordStore irs = mapCtx.getRecordStore(part, CN_XDM_INDEX);
			RecordStore rrs = mapCtx.getRecordStore(part, CN_XDM_RESULT);
			//mapCtx.getPartitionContainer(part).
			//stats.add(new PartitionStatistics(address, part, drs.size(), drs.getHeapCost(), drs.getMapDataStore().notFinishedOperationsCount(), 
			//		crs.size(), crs.getHeapCost(), ers.size(), ers.getHeapCost(), irs.size(), irs.getHeapCost(), rrs.size(), rrs.getHeapCost()));
		}
		return stats;
	}
	
	
}
