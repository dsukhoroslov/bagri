package com.bagri.server.hazelcast.impl;

import static com.bagri.core.server.api.CacheConstants.CN_XDM_CONTENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_ELEMENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_INDEX;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_RESULT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.PartitionStatistics;
import com.bagri.core.DocumentKey;
import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.map.impl.MapServiceContext;
import com.hazelcast.map.impl.query.AggregationResult;
import com.hazelcast.map.impl.query.Query;
import com.hazelcast.map.impl.query.QueryResult;
import com.hazelcast.map.impl.query.QueryResultRow;
import com.hazelcast.map.impl.query.Result;
import com.hazelcast.map.impl.recordstore.RecordStore;
import com.hazelcast.query.Predicates;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.IterationType;

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
			RecordStore<?> drs = mapCtx.getExistingRecordStore(part, CN_XDM_DOCUMENT);
			RecordStore<?> crs = mapCtx.getExistingRecordStore(part, CN_XDM_CONTENT);
			RecordStore<?> ers = mapCtx.getExistingRecordStore(part, CN_XDM_ELEMENT);
			RecordStore<?> irs = mapCtx.getExistingRecordStore(part, CN_XDM_INDEX);
			RecordStore<?> rrs = mapCtx.getExistingRecordStore(part, CN_XDM_RESULT);
			//mapCtx.getPartitionContainer(part).
			stats.add(new PartitionStatistics(address, part, drs.size(), drs.getOwnedEntryCost(), drs.getMapDataStore().notFinishedOperationsCount(), 
					crs.size(), crs.getOwnedEntryCost(), ers.size(), ers.getOwnedEntryCost(), irs.size(), irs.getOwnedEntryCost(), rrs.size(), 
					rrs.getOwnedEntryCost()));
		}
		return stats;
	}
	
	public int getPartitionId(Object key) {
		return nodeEngine.getPartitionService().getPartitionId(key);
	}
	
	public boolean isLocalKey(Object key) {
		int partId = nodeEngine.getPartitionService().getPartitionId(key); 
		return nodeEngine.getPartitionService().isPartitionOwner(partId);
	}
	
	public RecordStore<?> getRecordStore(int partitionId, String storeName) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		return mapCtx.getExistingRecordStore(partitionId, storeName);
	}
	
	public RecordStore<?> getRecordStore(String uri, String storeName) {
		Integer hash = uri.hashCode();
		int partId = getPartitionId(hash);
		return getRecordStore(partId, storeName);
	}

	public DocumentKey getLastKeyForUri(String uri) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		//Query query = new Query(CN_XDM_DOCUMENT, Predicates.equal("hash", uri.hashCode()), IterationType.KEY, Aggregators.integerMax("version"), null);
		Query query = new Query(CN_XDM_DOCUMENT, Predicates.equal("uri", uri), IterationType.KEY, null, null);
		try {
			DocumentKey last = null; 
			QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runIndexOrPartitionScanQueryOnOwnedPartitions(query);
			for (QueryResultRow row: rs.getRows()) {
				DocumentKey key = nodeEngine.toObject(row.getKey());
				if (last == null) {
					last = key;
				} else {
					if (key.getVersion() > last.getVersion()) {
						last = key;
					}
				}
			}
			logger.trace("getLastKeyForUri; uri: {}; returning: {}", uri, last);
			return last;
		} catch (ExecutionException | InterruptedException ex) {
			logger.error("", ex);
		}
		return null;
	}
	
}
