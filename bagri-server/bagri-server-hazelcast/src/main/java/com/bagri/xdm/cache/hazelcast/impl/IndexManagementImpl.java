package com.bagri.xdm.cache.hazelcast.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.PathExpression;
import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.util.ReflectUtils;
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
	private Map<Integer, TreeMap<Comparable, Integer>> rangeIndex = new HashMap<>();

	private XDMFactory factory;
    private ModelManagementImpl mdlMgr;
    private TransactionManagementImpl txMgr;
    
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

	public void setTxManager(TransactionManagementImpl txMgr) {
		this.txMgr = txMgr;
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
			rangeIndex.put(xPath.getPathId(), new TreeMap<Comparable, Integer>());
			logger.trace("createIndex; registered range index path: {}", index.getName());
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
			rangeIndex.remove(xPath.getPathId());
			logger.trace("deleteIndex; unregistered range index path: {}", index.getName());
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
	
	private Class getDataType(String dataType) {
		int pos = dataType.indexOf(":");
		if (pos > 0) {
			String prefix = dataType.substring(0,  pos);
			dataType = dataType.substring(pos + 1);
			if (!prefix.equals("xs")) {
				// custom type ?
			}
		}
		switch (dataType) {
			case "boolean": return Boolean.class;
			case "byte": return Byte.class;
			case "char": return Character.class;
			case "date": 
			case "dateTime": return java.util.Date.class; 
			case "double": return Double.class;
			case "float": return Float.class;
			case "int":
			case "integer": return Integer.class;
			case "long": return Long.class;
			case "short": return Short.class;
		}
		return String.class;
	}
	
	public void addIndex(long docId, int pathId, Object value) {
		// add index !
		if (value != null) {
			XDMIndex idx = idxDict.get(pathId);
			if (idx == null) {
				return;
			}
			
			Class dataType = getDataType(idx.getDataType());
			if (dataType.isInstance("String")) {
				if (!idx.isCaseSensitive()) {
					value = ((String) value).toLowerCase();
				}
			} else {
				// convert value..
				try {
					value = ReflectUtils.getValue(dataType, (String) value);
				} catch (Exception ex) {
					// just log error and use old value
					logger.error("addIndex.error: " + ex, ex);
				}
			}
			logger.trace("addIndex; index: {}, dataType: {}, value: {}", idx, dataType, value);
			
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
				TreeMap<Comparable, Integer> range = rangeIndex.get(pathId);
				Integer count = range.get(value);
				if (count == null) {
					count = 1;
				} else {
					count++;
				}
				range.put((Comparable) value, count);
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
			long txId = txMgr.getCurrentTxId();
			if (xIdx.removeDocument(docId, txId)) {
				if (xIdx.getCount() > 0) {
					idxCache.put(iKey, xIdx);
				} else {
	 				idxCache.delete(iKey);
				}
				TreeMap<Comparable, Integer> range = rangeIndex.get(pathId);
				Integer count = range.get(value);
				if (count != null) {
					count++;
					if (count > 0) {
						range.put((Comparable) value, count);
					} else {
						range.remove(value);
					}
				}
			}
		}
	}
	
	public Set<Long> getIndexedDocuments(int pathId, PathExpression pex, Object value) {
		logger.trace("getIndexedDocuments.enter; pathId: {}, PEx: {}, value: {}", pathId, pex, value);
		Set<Long> result = null;
		if (Comparison.EQ.equals(pex.getCompType())) {
			if (value instanceof Collection) {
				Collection values = (Collection) value;
				if (values.size() == 0) {
					result = Collections.emptySet();
				} else {
					if (values.size() == 1) {
						result = getIndexedDocuments(pathId, values.iterator().next().toString());
					} else {
						result = getIndexedDocuments(pathId, (Iterable) value);
					}
				}
			} else {
				result = getIndexedDocuments(pathId, value.toString());
			}
		} else {
			if (value instanceof Collection) {
				Collection values = (Collection) value;
				if (values.size() == 0) {
					return Collections.emptySet();
				}
				if (values.size() == 1) {
					value = values.iterator().next();
				}
			}
			//value = value.toString();
			if (value instanceof Comparable) {
				XDMIndex idx = idxDict.get(pathId);
				if (idx.isRange()) {
					Comparable comp = (Comparable) value;
					TreeMap<Comparable, Integer> range = rangeIndex.get(pathId);
					Map<Comparable, Integer> subRange;
					switch (pex.getCompType()) {
						case GT: {
							subRange = range.tailMap(comp);
							break;
						}
						case GE: {
							subRange = range.tailMap(comp, true);
							break;
						}
						case LE: {
							subRange = range.headMap(comp, true);
							break;
						}
						case LT: {
							subRange = range.headMap(comp);
							break;
						}
						default: subRange = Collections.emptyMap();
					}
					logger.trace("getIndexedDocuments; got subRange of length {}", subRange.size());
					Set<XDMIndexKey> keys = new HashSet<>(subRange.size());
					for (Object o: subRange.keySet()) {
						keys.add(factory.newXDMIndexKey(pathId, o));
					}
					Map<XDMIndexKey, XDMIndexedValue<?>> values = idxCache.getAll(keys);
					result = new HashSet<>(values.size());
					if (values.size() > 0) {
						for (XDMIndexedValue val: values.values()) {
							result.addAll(val.getDocumentIds());
						}
						updateStats(idx.getName(), true, 1);
					} else {
						updateStats(idx.getName(), false, 1);
					}
				}
			} else {
				logger.trace("getIndexedDocuments; value is not comparable: {}", value.getClass());
			}
		}
		logger.trace("getIndexedDocuments.exit; returning: {}", result == null ? null : result.size());
		return result;
	}
	
	private Set<Long> getIndexedDocuments(int pathId, String value) {
		XDMIndex idx = idxDict.get(pathId);
		// can't be null ?!
		XDMIndexKey idxk = factory.newXDMIndexKey(pathId, value);
		XDMIndexedValue xidv = idxCache.get(idxk);
		if (xidv != null) {
			updateStats(idx.getName(), true, 1);
			return xidv.getDocumentIds();
		}
		updateStats(idx.getName(), false, 1);
		return Collections.emptySet();
	}
	
	private Set<Long> getIndexedDocuments(int pathId, Iterable values) {
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
