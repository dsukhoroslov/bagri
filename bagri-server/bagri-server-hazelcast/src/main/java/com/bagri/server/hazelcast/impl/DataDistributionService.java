package com.bagri.server.hazelcast.impl;

import static com.bagri.core.server.api.CacheConstants.CN_XDM_CONTENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_ELEMENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_INDEX;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_KEY;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_RESULT;
import static com.bagri.core.model.Document.dvFirst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.PartitionStatistics;
import com.bagri.core.DataKey;
import com.bagri.core.DocumentKey;
import com.bagri.core.KeyFactory;
import com.bagri.core.model.Document;
import com.bagri.core.model.Elements;
import com.bagri.core.server.api.SchemaRepository;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.map.impl.MapServiceContext;
import com.hazelcast.map.impl.mapstore.MapDataStore;
import com.hazelcast.map.impl.query.Query;
import com.hazelcast.map.impl.query.QueryResult;
import com.hazelcast.map.impl.query.QueryResultRow;
import com.hazelcast.map.impl.query.ResultSegment;
import com.hazelcast.map.impl.recordstore.RecordStore;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.IterationType;
import com.hazelcast.wan.impl.CallerProvenance;

public class DataDistributionService implements ManagedService {

    private static final transient Logger logger = LoggerFactory.getLogger(DataDistributionService.class);

	private KeyFactory factory;
    private NodeEngine nodeEngine;
    private SchemaRepository repo;
    
    public void setFactory(KeyFactory factory) {
    	this.factory = factory;
    }

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
	
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
	}
	
	public <T> T getCachedObject(String cacheName, Object key, boolean convert) {
		Data dKey = nodeEngine.toData(key);
		int partId = nodeEngine.getPartitionService().getPartitionId(dKey);
		boolean isLocal = nodeEngine.getPartitionService().isPartitionOwner(partId);
		logger.trace("getCachedObject; partition {} is {}local for key {} on cache {}", 
				partId,	isLocal ? "" : "not ", key, cacheName);
		if (isLocal) {
			RecordStore<?> cache = getRecordStore(partId, cacheName);
			if (cache == null) {
				// nothing stored in this partition yet
				return null;
			}
	
			Object data = cache.get(dKey, false, nodeEngine.getThisAddress());
			if (data != null && convert) {
				data = nodeEngine.toObject(data);
			} 
			return (T) data;
		}
		return null;
	}

	public <T> Collection<T> getCachedObjects(String cacheName, Collection<Object> keys, boolean convert) {
		Collection<T> results = new ArrayList<>(keys.size());
		for (Object key: keys) {
			T result = getCachedObject(cacheName, key, convert);
			if (result != null) {
				results.add(result);
			} 
		}
		return results;
	}

	public void deleteCachedObject(String cacheName, Object key) {
		RecordStore<?> cache = getRecordStore(key, cacheName);
		if (cache == null) {
			// nothing stored in this partition yet
			return;
		}
		
		Data dKey = nodeEngine.toData(key);
		// will it delete backups?!
		cache.delete(dKey, CallerProvenance.NOT_WAN);
	}
	
	public <T> T removeCachedObject(String cacheName, Object key, boolean convert) {
		RecordStore<?> cache = getRecordStore(key, cacheName);
		if (cache == null) {
			// nothing stored in this partition yet
			return null;
		}
		
		Data dKey = nodeEngine.toData(key);
		// will it remove backups?!
		Object data = cache.remove(dKey, CallerProvenance.NOT_WAN);
		if (data != null) {
			if (convert) {
				data = nodeEngine.toObject(data);
			}
		}
		return (T) data;
	}
	
	public Collection<PartitionStatistics> getPartitionStatistics() {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		List<Integer> parts = nodeEngine.getPartitionService().getMemberPartitions(nodeEngine.getThisAddress());
		String address = nodeEngine.getThisAddress().toString();
		List<PartitionStatistics> stats = new ArrayList<>(parts.size());
		for (int part: parts) {
			int dsize = 0; long dcost = 0; 
			RecordStore<?> drs = mapCtx.getExistingRecordStore(part, CN_XDM_DOCUMENT);
			if (drs != null) {
				dsize = drs.size(); dcost = drs.getOwnedEntryCost();
			}
			int csize = 0; long ccost = 0; 
			RecordStore<?> crs = mapCtx.getExistingRecordStore(part, CN_XDM_CONTENT);
			if (crs != null) {
				csize = crs.size(); ccost = crs.getOwnedEntryCost();
			}
			int esize = 0; long ecost = 0; 
			RecordStore<?> ers = mapCtx.getExistingRecordStore(part, CN_XDM_ELEMENT);
			if (ers != null) {
				esize = ers.size(); ecost = ers.getOwnedEntryCost();
			}
			int isize = 0; long icost = 0; 
			RecordStore<?> irs = mapCtx.getExistingRecordStore(part, CN_XDM_INDEX);
			if (irs != null) {
				isize = irs.size(); icost = irs.getOwnedEntryCost();
			}
			int rsize = 0; long rcost = 0; 
			RecordStore<?> rrs = mapCtx.getExistingRecordStore(part, CN_XDM_RESULT);
			if (rrs != null) {
				rsize = rrs.size(); rcost = rrs.getOwnedEntryCost();
			}
			MapDataStore<Data, Object> store = drs.getMapDataStore();
			//mapCtx.getPartitionContainer(part).
			stats.add(new PartitionStatistics(address, part, dsize, dcost, store == null ? 0 : store.notFinishedOperationsCount(), 
					csize, ccost, esize, ecost, isize, icost, rsize, rcost));
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
	
	public RecordStore<?> getRecordStore(Object key, String storeName) {
		int partId = nodeEngine.getPartitionService().getPartitionId(key); 
		return getRecordStore(partId, storeName);
	}
	
	public RecordStore<?> getRecordStore(String uri, String storeName) {
		int hash = repo.getDistributionStrategy().getDistributionHash(uri);
		int partId = getPartitionId(hash);
		return getRecordStore(partId, storeName);
	}
	
	private RecordStore<?> getRecordStore(int partitionId, String storeName) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		return mapCtx.getExistingRecordStore(partitionId, storeName);
	}
	
	public DocumentKey getLastRevisionKeyForUri(String uri) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		Integer hash = repo.getDistributionStrategy().getDistributionHash(uri);
		Query query = new Query(CN_XDM_DOCUMENT, Predicates.equal("__key#hash", hash), IterationType.ENTRY, null, null); 
		DocumentKey last = null;
		boolean foundUri = false;

		// this one just scans partition, it does not use index
		//QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runPartitionIndexOrPartitionScanQueryOnGivenOwnedPartition(query, partId);
		QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runIndexOrPartitionScanQueryOnOwnedPartitions(query);
		if (rs == null) {
			logger.info("getLastRevisionKeyForUri; got null QueryResult for uri: {}", uri);
		} else {
			for (QueryResultRow row: rs.getRows()) {
				DocumentKey key = nodeEngine.toObject(row.getKey());
				Document doc = nodeEngine.toObject(row.getValue());
				if (uri.equals(doc.getUri())) {
					if (!foundUri || key.getVersion() > last.getVersion()) {
						last = key;
					} 
					foundUri = true;
					continue;
				}
	
				if (!foundUri && (last == null || key.getRevision() > last.getRevision())) {
					last = key;
				}
			}
	
			if (!foundUri && last != null) {
				last = factory.newDocumentKey(uri, last.getRevision() + 1, dvFirst);
			}
			logger.trace("getLastRevisionKeyForUri; uri: {}; returning: {}", uri, last);
		}
		return last;
	}

	public DocumentKey getLastKeyForUri(String uri) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		Query query = new Query(CN_XDM_DOCUMENT, Predicates.equal("uri", uri), IterationType.KEY, null, null);
		DocumentKey last = null;
		QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runIndexOrPartitionScanQueryOnOwnedPartitions(query);
		if (rs == null) {
			logger.info("getLastKeyForUri; got null QueryResult for uri: {}", uri);
		} else {
			for (QueryResultRow row: rs.getRows()) {
				DocumentKey key = nodeEngine.toObject(row.getKey());
				if (last == null || key.getVersion() > last.getVersion()) {
					last = key;
				}
			}
			logger.trace("getLastKeyForUri; uri: {}; returning: {}", uri, last);
		}
		return last;	
		//List<DocumentKey> keys = getCachedObject(CN_XDM_KEY, new UrlHashKey(uri), true);
		//if (keys != null) {
		//	return keys.get(keys.size() - 1);
		//}
		//return null;
	}

	public Collection<DocumentKey> getLastKeysForQuery(Predicate<DocumentKey, Document> query) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		Query q = new Query(CN_XDM_DOCUMENT, query, IterationType.KEY, null, null);
		QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runIndexOrPartitionScanQueryOnOwnedPartitions(q);
		if (rs == null) {
			logger.info("getLastKeysForQuery; got null QueryResult for query: {}", query);
			return Collections.emptyList();
		} else {
			List<DocumentKey> results = new ArrayList<>(rs.size());
			// TODO: we must take latest keys only..
			for (QueryResultRow row: rs.getRows()) {
				DocumentKey key = nodeEngine.toObject(row.getKey());
				results.add(key);
			}
			logger.trace("getLastKeysForQuery; query: {}; returning: {}", query, results.size());
			return results;
		}
	}

	public Collection<DocumentKey> getLastKeysForQuery(Predicate<DocumentKey, Document> query, int fetchSize) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		Query q = new Query(CN_XDM_DOCUMENT, query, IterationType.KEY, null, null);
		List<DocumentKey> results = new ArrayList<>();
		List<Integer> parts = nodeEngine.getPartitionService().getMemberPartitions(nodeEngine.getThisAddress());
		for (Integer partId: parts) {
			int shift = 0;
			do {
				ResultSegment rs = mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runPartitionScanQueryOnPartitionChunk(q, partId, shift, fetchSize);
				QueryResult qr = (QueryResult) rs.getResult();
				for (QueryResultRow row: qr.getRows()) {
					DocumentKey key = nodeEngine.toObject(row.getKey());
					results.add(key);
					if (results.size() == fetchSize) {
						break;
					}
				}
				shift = rs.getNextTableIndexToReadFrom();
			} while (shift > 0);
		}

		//try {
		//	QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runIndexOrPartitionScanQueryOnOwnedPartitions(q);
			// TODO: we must take latest keys only..
		//	for (QueryResultRow row: rs.getRows()) {
		//		DocumentKey key = nodeEngine.toObject(row.getKey());
		//		results.add(key);
		//		if (results.size() == fetchSize) {
		//			break;
		//		}
		//	}
		//} catch (ExecutionException | InterruptedException ex) {
		//	logger.error("getLastDocumentsForQuery.error: ", ex);
		//}

		logger.trace("getLastKeysForQuery; query: {}; fSize: {}; returning: {}", query, fetchSize, results.size());
		return results;
	}

	public Document getLastDocumentForUri(String uri) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		Query query = new Query(CN_XDM_DOCUMENT, Predicates.equal("uri", uri), IterationType.VALUE, null, null);
		Document last = null;
		QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runIndexOrPartitionScanQueryOnOwnedPartitions(query);
		// rs can be null in case of cluster rebalancing!
		if (rs == null) {
			logger.info("getLastDocumentForUri; got null QueryResult for uri: {}", uri);
		} else {
			for (QueryResultRow row: rs.getRows()) {
				Document doc = nodeEngine.toObject(row.getValue());
				if (last == null || doc.getVersion() > last.getVersion()) {
					last = doc;
				}
			}
			logger.trace("getLastDocumentForUri; returning: {} for uri: {}", last, uri);
		}
		return last;
	}

	public Collection<Document> getLastDocumentsForQuery(Predicate<DocumentKey, Document> query) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		Query q = new Query(CN_XDM_DOCUMENT, query, IterationType.VALUE, null, null);
		QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runIndexOrPartitionScanQueryOnOwnedPartitions(q);
		if (rs == null) {
			logger.info("getLastDocumentsForQuery; got null QueryResult for query: {}", query);
			return Collections.emptyList();
		} else {
			Map<String, Document> results = new HashMap<>(rs.size());
			for (QueryResultRow row: rs.getRows()) {
				Document doc = nodeEngine.toObject(row.getValue());
				Document last = results.get(doc.getUri());
				if (last == null || doc.getVersion() > last.getVersion()) {
					results.put(doc.getUri(), doc);
				}
			}
			logger.trace("getLastDocumentsForQuery; returning: {}", results.size());
			return results.values();
		}
	}

	public Collection<Document> getLastDocumentsForQuery(Predicate<DocumentKey, Document> query, int fetchSize) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		Query q = new Query(CN_XDM_DOCUMENT, query, IterationType.VALUE, null, null);
		List<Document> results = new ArrayList<>(fetchSize);
		List<Integer> parts = nodeEngine.getPartitionService().getMemberPartitions(nodeEngine.getThisAddress());
		for (Integer partId: parts) {
			int shift = 0;
			do {
				ResultSegment rs = mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runPartitionScanQueryOnPartitionChunk(q, partId, shift, fetchSize);
				QueryResult qr = (QueryResult) rs.getResult();
				for (QueryResultRow row: qr.getRows()) {
					Document doc = nodeEngine.toObject(row.getValue());
					results.add(doc);
					if (results.size() == fetchSize) {
						break;
					}
				}
				shift = rs.getNextTableIndexToReadFrom();
			} while (shift > 0);
		}
		
		//try {
		//	QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_DOCUMENT).runIndexOrPartitionScanQueryOnOwnedPartitions(q);
		//	for (QueryResultRow row: rs.getRows()) {
		//		Document doc = nodeEngine.toObject(row.getValue());
				//Document last = results.get(doc.getUri());
				//if (last == null || doc.getVersion() > last.getVersion()) {
		//			results.add(doc);
				//}
		//		if (results.size() == fetchSize) {
		//			break;
		//		}
		//	}
		//} catch (ExecutionException | InterruptedException ex) {
		//	logger.error("getLastDocumentsForQuery.error: ", ex);
		//}
		
		return results;
	}
	
	public Collection<DataKey> getElementKeys(long docId) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		Query query = new Query(CN_XDM_ELEMENT, Predicates.equal("__key#documentKey", docId), IterationType.KEY, null, null);
		QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_ELEMENT).runIndexOrPartitionScanQueryOnOwnedPartitions(query);
		if (rs == null) {
			logger.info("getElementKeys; got null QueryResult for docId: {}", docId);
			return Collections.emptyList();
		}
		Collection<DataKey> result = new ArrayList<>(rs.size());
		for (QueryResultRow row: rs.getRows()) {
			result.add((DataKey) nodeEngine.toObject(row.getKey()));
		}
		return result;
	}
	
	public Map<DataKey, Elements> getElements(long docId) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		Query query = new Query(CN_XDM_ELEMENT, Predicates.equal("__key#documentKey", docId), IterationType.ENTRY, null, null);
		QueryResult rs = (QueryResult) mapCtx.getMapQueryRunner(CN_XDM_ELEMENT).runIndexOrPartitionScanQueryOnOwnedPartitions(query);
		if (rs == null) {
			logger.info("getElements; got null QueryResult for docId: {}", docId);
			return Collections.emptyMap();
		}
		Map<DataKey, Elements> result = new HashMap<>(rs.size());
		for (QueryResultRow row: rs.getRows()) {
			result.put((DataKey) nodeEngine.toObject(row.getKey()), (Elements) nodeEngine.toObject(row.getValue()));
		}
		return result;
	}
	
	public InternalSerializationService getSerializationService() {
		return (InternalSerializationService) nodeEngine.getSerializationService();
	}

	public Object storeData(Object key, Object value, String storeName) {
		MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = svc.getMapServiceContext();
		int partId = nodeEngine.getPartitionService().getPartitionId(key); 
		logger.trace("storeData; going to store key: {}; on partition: {}", key, partId);
		RecordStore<?> rs = mapCtx.getExistingRecordStore(partId, storeName);
		if (rs != null) {
			return rs.put(nodeEngine.toData(key), value, 0, 0);
		}
		return null;
	}
	
}
