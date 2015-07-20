package com.bagri.xdm.cache.hazelcast.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.stats.StatisticsEvent;
import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.cache.api.XDMIndexManagement;
import com.bagri.xdm.cache.hazelcast.task.index.ValueIndexator;
import com.bagri.xdm.client.hazelcast.impl.ModelManagementImpl;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMIndexedDocument;
import com.bagri.xdm.domain.XDMIndexedValue;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.domain.XDMUniqueDocument;
import com.bagri.xdm.system.XDMIndex;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.MapListener;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class IndexManagementImpl implements XDMIndexManagement, EntryAddedListener, Predicate<XDMDataKey, XDMElements> { //, StatisticsProvider {
	
	private static final transient Logger logger = LoggerFactory.getLogger(IndexManagementImpl.class);
	private IMap<Integer, XDMIndex> idxDict;
    private IMap<XDMIndexKey, XDMIndexedValue<?>> idxCache;
    private IMap<XDMDataKey, XDMElements> xdmCache;
	private IExecutorService execService;

	private XDMFactory factory;
    private ModelManagementImpl mdlMgr;
    
    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;

	protected XDMFactory getXdmFactory() {
		return this.factory;
	}
	
	public void setXdmFactory(XDMFactory factory) {
		this.factory = factory;
	}
    
	protected ModelManagementImpl getModelManager() {
		return this.mdlMgr;
	}
	
	protected Map<Integer, XDMIndex> getIndexDictionary() {
		return idxDict;
	}
	
    IMap<XDMIndexKey, XDMIndexedValue<?>> getIndexCache() {
    	return idxCache;
    }

    public void setDataCache(IMap<XDMDataKey, XDMElements> xdmCache) {
    	this.xdmCache = xdmCache;
    }
    
	public void setIndexDictionary(IMap<Integer, XDMIndex> idxDict) {
		this.idxDict = idxDict;
	}
	
    public void setIndexCache(IMap<XDMIndexKey, XDMIndexedValue<?>> idxCache) {
    	this.idxCache = idxCache;
    }
    
    public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
    	this.queue = queue;
    }

    public void setStatsEnabled(boolean enable) {
    	this.enableStats = enable;
    }

    public void setExecService(IExecutorService execService) {
		this.execService = execService;
	}
    
	public void setModelManager(ModelManagementImpl mdlMgr) {
		this.mdlMgr = mdlMgr;
	}
    
	@Override
	public boolean isPathIndexed(int pathId) {
		// TODO: it takes a lot of time to perform these two gets
		// even on local cache! think about better solution!
		//XDMPath xPath = mdlMgr.getPath(pathId);
		//if (xPath == null) {
		//	logger.warn("isPathIndexed; got unknown pathId: {}", pathId);
		//	return false;
		//}
		
		// well, one get is better :)
		// how near cache does works on cache miss!? does is go to 
		// original cache? 
		// shouldn't we also check index.isEnabled() here?
		return idxDict.get(pathId) != null;
		//return pathId == 2;
	}

	public boolean isIndexEnabled(int pathId) {
		XDMIndex idx = idxDict.get(pathId);
		if (idx != null) {
			return idx.isEnabled();
		}
		return false;
	}
	
	@Override
	public XDMPath createIndex(XDMIndex index) {
		XDMPath xPath = getPathForIndex(index);
		idxDict.putIfAbsent(xPath.getPathId(), index);
		//indexStats.initStats(index.getName());
		if (index.isRange()) {
			// register listener ..
			//String uuid = xdmCache.addEntryListener(this, this, false);
			//logger.trace("createIndex; registered listener {} for range index {}", uuid, index.getName());
		}
		return xPath;
	}
	
	@Override
	public XDMPath deleteIndex(XDMIndex index) {
		// we must not do translate here!
		XDMPath xPath = getPathForIndex(index);
		//if (idxDict.remove(xPath.getPathId()) != null) {
		//	return xPath;
		//}
		//return null;
		idxDict.remove(xPath.getPathId());
		//indexStats.deleteStats(index.getName());
		if (index.isRange()) {
			// unregister listener ..
		}
		return xPath;
	}
	
	private XDMPath getPathForIndex(XDMIndex index) {
		int docType = mdlMgr.translateDocumentType(index.getDocumentType());
		String path = index.getPath();
		XDMNodeKind kind = path.endsWith("/text()") ? XDMNodeKind.text : XDMNodeKind.attribute;
		XDMPath xPath = mdlMgr.translatePath(docType, path, kind);
		logger.trace("getPathForIndex; returning: {}", xPath);
		return xPath;
	}
	
	public void addIndex(long docId, int pathId, Object value) {
		// add index !
		if (value != null) {
			XDMIndex idx = idxDict.get(pathId);
			if (idx == null) {
				return;
			}
			
			// TODO: get data type in some other way..
			if (!idx.isCaseSensitive() && "xs:string".equals(idx.getDataType())) {
				value = ((String) value).toLowerCase();
			}

			XDMIndexKey xid = factory.newXDMIndexKey(pathId, value);
			XDMIndexedValue xidx = idxCache.get(xid);
			if (idx.isUnique()) {
				long id = XDMDocumentKey.toDocumentId(docId);
				// check xidx.docIds - update document UC..
				if (xidx != null) {
					long currId = xidx.getDocumentId();
					if (id != XDMDocumentKey.toDocumentId(currId)) {
						throw new IllegalStateException("unique index '" + idx.getName() + "' violated for docId: " + docId + ", pathId: " + pathId + ", value: " + value);
					}
				}

				xidx = new XDMUniqueDocument(pathId, value, docId);
				xidx = idxCache.putIfAbsent(xid, xidx);
				if (xidx != null) {
					// but what if it is not commited yet!?
					long currId = xidx.getDocumentId();
					if (id != XDMDocumentKey.toDocumentId(currId)) {
						throw new IllegalStateException("unique index '" + idx.getName() + "' violated for docId: " + docId + ", pathId: " + pathId + ", value: " + value);
					}
				}
			} else {
				if (xidx == null) {
					xidx = new XDMIndexedDocument(pathId, value, docId);
				} else { 
					xidx.addDocument(docId, XDMTransactionManagement.TX_NO);
				}
				idxCache.put(xid, xidx);
			}
			
			// collect static index stats right here?
			
			// it works asynch. but major time is taken in isPathIndexed method..
			//ValueIndexator indexator = new ValueIndexator(docId);
			//idxCache.submitToKey(xid, indexator);
			//logger.trace("addIndex; index submit for key {}", xid);
		}
	}
	
	public void dropIndex(long docId, int pathId, Object value) {
		XDMIndexKey iKey = factory.newXDMIndexKey(pathId, value);
		// will have collisions here when two threads change/delete the same index!
		// but not for unique index!
		XDMIndexedValue xIdx = idxCache.get(iKey);
		if (xIdx != null) {
			// get current txId..
			long txId = XDMTransactionManagement.TX_NO;
			if (xIdx.removeDocument(docId, txId)) {
				if (xIdx.getCount() > 0) {
					idxCache.put(iKey, xIdx);
				} else {
	 				idxCache.delete(iKey);
				}
			}
		}
	}
	
	public Set<Long> getIndexedDocuments(int pathId, String value) {
		XDMIndex idx = idxDict.get(pathId);
		// can't be null ?!
		XDMIndexKey idxk = factory.newXDMIndexKey(pathId, value);
		XDMIndexedValue xidv = idxCache.get(idxk);
		if (xidv != null) {
			updateStats(idx.getName(), true, 1);
			return xidv.getDocumentIds();
		}
		updateStats(idx.getName(), false, 1);
		return null;
	}
	
	public Set<Long> getIndexedDocuments(int pathId, Iterable values) {
		XDMIndex idx = idxDict.get(pathId);
		// can't be null ?!
		Set<XDMIndexKey> keys = new HashSet<>();
		for (Object value: values) {
			keys.add(factory.newXDMIndexKey(pathId, value));
		}
		Map<XDMIndexKey, XDMIndexedValue<?>> xidv = idxCache.getAll(keys);
		Set<Long> ids = new HashSet<>(xidv.size());
		for (XDMIndexedValue value: xidv.values()) {
			ids.addAll(value.getDocumentIds());
		}
		updateStats(idx.getName(), true, xidv.size());
		updateStats(idx.getName(), false, keys.size() - xidv.size());
		return ids;
	}
	
	public Collection<Integer> getTypeIndexes(int docType, boolean uniqueOnly) {
		String root = mdlMgr.getDocumentRoot(docType);
		Predicate p = Predicates.equal("typePath", root);
		if (uniqueOnly) {
			Predicate u = Predicates.equal("unique", true);
			p = Predicates.and(p, u);
		}
		return idxDict.keySet(p);
	}
	
	private void updateStats(String name, boolean success, int count) {
		if (enableStats) {
			if (!queue.offer(new StatisticsEvent(name, success, count))) {
				logger.warn("updateStats; queue is full!!");
			}
		}
	}
	
	public TabularData getIndexStats() {

		Set<XDMIndexKey> keys = idxCache.localKeySet();
		Map<XDMIndexKey, XDMIndexedValue<?>> locals = idxCache.getAll(keys);
		
        TabularData result = null;
		for (XDMIndex idx: idxDict.values()) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("index", idx.getName());
            stats.put("path", idx.getPath());
    		XDMPath xPath = getPathForIndex(idx);
    		long size = 0;
    		int count = 0;
    		int unique = 0;
    		for (Map.Entry<XDMIndexKey, XDMIndexedValue<?>> e: locals.entrySet()) {
    			if (e.getKey().getPathId() == xPath.getPathId()) {
    				count += e.getValue().getCount();
    				unique++;
    				size += 8 + 8 + 8 + //sizeof(e.getKey().getValue()) 
    						8 + 8 + e.getValue().getCount() * 16;
    			}
    		}
    		stats.put("indexed documents", count);
    		stats.put("distinct values", unique);
    		stats.put("consumed size", size);
            
            try {
                CompositeData data = JMXUtils.mapToComposite("Name", "Header", stats);
                result = JMXUtils.compositeToTabular("Name", "Header", "index", result, data);
            } catch (Exception ex) {
                logger.error("getIndexStats; error", ex);
            }
			
		}
        return result;
	}
	
	@Override
	public boolean rebuildIndex(int pathId) {
		// TODO Auto-generated method stub
		return false;
	}

/*	
	@Override
	public void entryAdded(EntryEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryUpdated(EntryEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryRemoved(EntryEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryEvicted(EntryEvent event) {
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
*/
	@Override
	public boolean apply(Entry<XDMDataKey, XDMElements> mapEntry) {
		return idxDict.containsKey(mapEntry.getKey().getPathId());
	}

	@Override
	public void entryAdded(EntryEvent event) {
		// TODO Auto-generated method stub
		logger.trace("entryAdded; got event: {}", event);
	}

}
