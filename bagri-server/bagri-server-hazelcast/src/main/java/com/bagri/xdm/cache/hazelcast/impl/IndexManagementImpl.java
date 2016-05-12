package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;
import static com.bagri.xqj.BagriXQUtils.getAtomicValue;
import static com.bagri.xqj.BagriXQUtils.getBaseTypeForTypeName;
import static com.bagri.xqj.BagriXQUtils.isStringTypeCompatible;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.PathBuilder;
import com.bagri.common.query.PathExpression;
import com.bagri.common.stats.StatisticsEvent;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.cache.api.XDMIndexManagement;
import com.bagri.xdm.client.hazelcast.impl.ModelManagementImpl;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.domain.XDMIndexedDocument;
import com.bagri.xdm.domain.XDMIndexedValue;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMOccurence;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.domain.XDMUniqueDocument;
import com.bagri.xdm.domain.XDMUniqueValue;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xqj.BagriXQUtils;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class IndexManagementImpl implements XDMIndexManagement { //, StatisticsProvider {
	
	private static final transient Logger logger = LoggerFactory.getLogger(IndexManagementImpl.class);
	private ReplicatedMap<Integer, XDMIndex> idxDict;
    private IMap<XDMIndexKey, XDMIndexedValue> idxCache;
	//private IExecutorService execService;
	private Map<Integer, TreeMap<Comparable, Integer>> rangeIndex = new HashMap<>();
	private Map<XDMIndex, Pattern> patterns = new HashMap<>();

	private XDMFactory factory;
    private ModelManagementImpl mdlMgr;
	private DocumentManagementImpl docMgr;
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
	
    IMap<XDMIndexKey, XDMIndexedValue> getIndexCache() {
    	return idxCache;
    }

    //public void setDataCache(IMap<XDMDataKey, XDMElements> xdmCache) {
    //	this.xdmCache = xdmCache;
    //}
    
	public void setIndexDictionary(ReplicatedMap<Integer, XDMIndex> idxDict) {
		this.idxDict = idxDict;
	}
	
    public void setIndexCache(IMap<XDMIndexKey, XDMIndexedValue> idxCache) {
    	this.idxCache = idxCache;
    }
    
    public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
    	this.queue = queue;
    }

    public void setStatsEnabled(boolean enable) {
    	this.enableStats = enable;
    }

    public void setRepository(RepositoryImpl repo) {
    	//this.repo = repo;
    	//this.model = repo.getModelManagement();
    	//this.docMgr = (DocumentManagementImpl) repo.getDocumentManagement();
    	//this.txMgr = (TransactionManagementImpl) repo.getTxManagement();
    	//this.xddCache = docMgr.getDocumentCache();
    	//this.xdmCache = docMgr.getElementCache();
    	//docMgr.setRepository(repo);
    }

    //public void setExecService(IExecutorService execService) {
	//	this.execService = execService;
	//}

	public void setDocumentManager(DocumentManagementImpl docMgr) {
		this.docMgr = docMgr;
	}
    
	public void setModelManager(ModelManagementImpl mdlMgr) {
		this.mdlMgr = mdlMgr;
	}
    
	public void setTxManager(TransactionManagementImpl txMgr) {
		this.txMgr = txMgr;
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
		//return idxDict.get(pathId) != null;
		return idxDict.containsKey(pathId);
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
	public XDMPath[] createIndex(XDMIndex index) throws XDMException {
		Set<Integer> paths = getPathsForIndex(index);
		XDMPath[] result = new XDMPath[paths.size()];
		int idx = 0;
		for (Integer pathId: paths) {
			//idxDict.putIfAbsent(pathId, index);
			idxDict.put(pathId, index);
			//indexStats.initStats(index.getName());
			if (index.isRange()) {
				rangeIndex.put(pathId, new TreeMap<Comparable, Integer>());
				logger.trace("createIndex; registered range index path: {}", index.getName());
			}
			result[idx] = mdlMgr.getPath(pathId);
			idx++;
		}
		if (isPatternIndex(index)) {
			String path = mdlMgr.normalizePath(index.getPath());
			patterns.put(index, Pattern.compile(PathBuilder.regexFromPath(path)));
		}
		return result;
	}
	
	@Override
	public XDMPath[] dropIndex(XDMIndex index) throws XDMException {
		// we must not do translate here!
		Set<Integer> paths = getPathsForIndex(index);
		XDMPath[] result = new XDMPath[paths.size()];
		int idx = 0;
		for (Integer pathId: paths) {
			idxDict.remove(pathId);
			//indexStats.deleteStats(index.getName());
			if (index.isRange()) {
				rangeIndex.remove(pathId);
				logger.trace("deleteIndex; unregistered range index path: {}", index.getName());
			}
			result[idx] = mdlMgr.getPath(pathId);
			idx++;
		}
		patterns.remove(index);
		return result;
	}
	
	private Set<Integer> getPathsForIndex(XDMIndex index) throws XDMException {
		int docType = mdlMgr.translateDocumentType(index.getDocumentType());
		String path = index.getPath();
		Set<Integer> result;
		if (PathBuilder.isRegexPath(path)) {
			path = mdlMgr.normalizePath(path);
			result = mdlMgr.translatePathFromRegex(docType, PathBuilder.regexFromPath(path));
		} else {
			int dataType = BagriXQUtils.getBaseTypeForTypeName(index.getDataType());
			XDMPath xPath = mdlMgr.translatePath(docType, path, XDMNodeKind.fromPath(path), dataType, XDMOccurence.zeroOrOne);
			result = new HashSet<>(1);
			result.add(xPath.getPathId());
		}
		logger.trace("getPathsForIndex; returning {} for index {}", result, index);
		return result;
	}
	
	private boolean isPatternIndex(XDMIndex index) {
		return PathBuilder.isRegexPath(index.getPath());
	}

	private Collection<XDMIndex> getPathIndexes(int pathId, String path) {
		Set<XDMIndex> result = new HashSet<>();
		XDMIndex idx = idxDict.get(pathId);
		if (idx != null) {
			result.add(idx);
		} else {
			for (Map.Entry<XDMIndex, Pattern> e: patterns.entrySet()) {
				Matcher m = e.getValue().matcher(path);
				boolean match = m.matches(); 
				if (match) {
					result.add(e.getKey());
					// TODO: do we put multiple indexes for the same path?! think about it..
					idxDict.put(pathId, e.getKey());
					result.add(e.getKey());
				}
				logger.trace("getPathIndexes; pattern {} {}matched for path {}", e.getValue().pattern(), match ? "" : "not ", path);
			}
		}
		return result;
	}
	
	public void addIndex(long docId, int pathId, String path, Object value) throws XDMException {

		// shouldn't we index NULL values too? create special NULL class for this..
		if (value != null) {
			Collection<XDMIndex> indexes = getPathIndexes(pathId, path);
			if (indexes.isEmpty()) {
				return;
			}
			
			for (XDMIndex idx: indexes) {
				indexPath(idx, docId, pathId, value);
			}
		}
	}
	
	private Object getIndexedValue(XDMIndex idx, int pathId, Object value) {
		int baseType = getBaseTypeForTypeName(idx.getDataType());
		if (isStringTypeCompatible(baseType)) {
			value = value.toString();
			if (!idx.isCaseSensitive()) {
				value = ((String) value).toLowerCase();
			}
		} else {
			XDMPath xPath = mdlMgr.getPath(pathId);
			if (xPath.getDataType() != baseType) {
				logger.info("getIndexedValue; index [{}] and path [{}] types are not compatible; value: {}({})", 
						baseType, xPath.getDataType(), value.getClass().getName(), value);
				try {
					// conversion from path type to index type
					value = getAtomicValue(baseType, value.toString());
				} catch (Exception ex) {
					// just log error and use old value
					logger.error("getIndexedValue.error: " + ex, ex);
				}
			}
		}
		return value;
	}
	
	private void indexPath(XDMIndex idx, long docKey, int pathId, Object value) throws XDMException {

		value = getIndexedValue(idx, pathId, value);
		logger.trace("indexPath; index: {}, value: {}({})", idx, value.getClass().getName(), value);
			
		int oldCount = 0;
		boolean first = false;
		XDMIndexKey xid = factory.newXDMIndexKey(pathId, value);
		XDMIndexedValue xidx = idxCache.get(xid);
		if (idx.isUnique()) {
			long id = XDMDocumentKey.toDocumentId(docKey);
			if (!checkUniquiness((XDMUniqueDocument) xidx, id)) {
				throw new XDMException("unique index '" + idx.getName() + "' violated for docKey: " + 
						docKey + ", pathId: " + pathId + ", value: " + value, XDMException.ecIndexUnique);
			}

			if (xidx == null) {
				xidx = new XDMUniqueDocument();
				first = true;
			} else {
				oldCount = xidx.getCount();
			}
			xidx.addDocument(docKey, txMgr.getCurrentTxId());
			XDMIndexedValue xidx2 = idxCache.put(xid, xidx);
			// why it is done second time here? because it can be new xidx!
			if (!checkUniquiness((XDMUniqueDocument) xidx2, id)) {
				// shouldn't we delete just created xidx then ?
				throw new XDMException("unique index '" + idx.getName() + "' violated for docKey: " + 
						docKey + ", pathId: " + pathId + ", value: " + value, XDMException.ecIndexUnique);
			}
		} else {
			if (xidx == null) {
				xidx = new XDMIndexedDocument(docKey);
				first = true;
			} else { 
				oldCount = xidx.getCount();
				xidx.addDocument(docKey, XDMTransactionManagement.TX_NO);
			}
			idxCache.set(xid, xidx);
			if (idx.isRange()) {
				TreeMap<Comparable, Integer> range = rangeIndex.get(pathId);
				Integer count = range.get(value);
				if (count == null) {
					count = 1;
				} else {
					count++;
				}
				range.put((Comparable) value, count);
			}
		}
		updateStats(idx.getName(), true, first, xidx.getCount() - oldCount, xidx.getSize());
		
		//if (isPatternIndex(idx)) {
		//	logger.info("indexPath; indexed pattern: {}, dataType: {}, value: {}", idx, dataType, value);
		//}
			
		// it works asynch. but major time is taken in isPathIndexed method..
		//ValueIndexator indexator = new ValueIndexator(docId);
		//idxCache.submitToKey(xid, indexator);
		//logger.trace("addIndex; index submit for key {}", xid);
	}

	private boolean checkUniquiness(XDMUniqueDocument uidx, long docId) throws XDMException {
		//long id = XDMDocumentKey.toDocumentId(docId);
		// check xidx.docIds - update document UC..
		if (uidx != null) {
			Collection<XDMUniqueValue> ids = uidx.getDocumentValues();
			for (XDMUniqueValue uv: ids) {
				long id = XDMDocumentKey.toDocumentId(uv.getDocumentKey());
				if (id == docId) {
					// skipping index for the same document's family
					continue;
				}
				
				if (docMgr.checkDocumentCommited(uv.getDocumentKey())) {
					if (uv.getTxFinish() > TX_NO) {
						if (txMgr.isTxVisible(uv.getTxFinish())) {
							// rolledBack, ok
						} else {
							// finish is not visible yet! should we lock and wait here??
						}
					} else {
						if (txMgr.isTxVisible(uv.getTxStart())) {
							// unique index violation
							return false;
						} else {
							// start is not visible yet! thus it is not yet commited.. should we lock?
						}
					}
				}
			}

			//long currId = xidx.getDocumentId();
			//if (currId > 0 && id != XDMDocumentKey.toDocumentId(currId)) {
			//	throw new XDMException("unique index '" + idx.getName() + "' violated for docId: " + docId + ", pathId: " + pathId + ", value: " + value);
			//}
		}
		return true;
	}
	
	public void removeIndex(long docId, int pathId, Object value) {
		XDMIndexKey iKey = factory.newXDMIndexKey(pathId, value);
		// will have collisions here when two threads change/delete the same index!
		// but not for unique index!
		XDMIndexedValue xIdx = idxCache.get(iKey);
		if (xIdx != null) {
			long txId = txMgr.getCurrentTxId();
			int oldCount = xIdx.getCount(); 
			if (xIdx.removeDocument(docId, txId)) {
				boolean last = false;
				if (xIdx.getCount() > 0) {
					idxCache.put(iKey, xIdx);
				} else {
	 				idxCache.delete(iKey);
	 				last = true;
				}
				XDMIndex idx = idxDict.get(pathId);
				updateStats(idx.getName(), false, last, oldCount - xIdx.getCount(), xIdx.getSize());
				TreeMap<Comparable, Integer> range = rangeIndex.get(pathId);
				if (range != null) {
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
				logger.trace("dropIndex; dropped index for docKey: {}, pathId: {}, value: {}", docId, pathId, value);
			}
		}
	}
	
	public Set<Long> getIndexedDocuments(int pathId, PathExpression pex, Object value) {
		logger.trace("getIndexedDocuments.enter; pathId: {}, PEx: {}, value: {}", pathId, pex, value);
		Set<Long> result = null;
		XDMIndex idx = idxDict.get(pathId);
		if (idx != null && idx.isEnabled()) {
			if (Comparison.EQ.equals(pex.getCompType())) {
				if (value instanceof Collection) {
					result = getIndexedDocuments(idx, pathId, (Iterable) value);
				} else {
					result = getIndexedDocuments(idx, pathId, value);
				}
			} else {
				value = getIndexedValue(idx, pathId, value);
				if (idx.isRange() && value instanceof Comparable) {
					result = getIndexedDocuments(idx, pathId, pex.getCompType(), (Comparable) value);
				} else {
					logger.debug("getIndexedDocuments; value is not comparable: {}", value.getClass());
				}
			}
		}
		logger.trace("getIndexedDocuments.exit; returning: {}", result == null ? null : result.size());
		return result;
	}
	
	private Set<Long> getIndexedDocuments(XDMIndex idx, int pathId, Object value) {
		XDMIndexKey idxk = factory.newXDMIndexKey(pathId, value);
		XDMIndexedValue xidv = idxCache.get(idxk);
		if (xidv != null) {
			updateStats(idx.getName(), true, 1);
			return xidv.getDocumentKeys();
		}
		updateStats(idx.getName(), false, 1);
		return Collections.emptySet();
	}
	
	private Set<Long> getIndexedDocuments(XDMIndex idx, int pathId, Iterable values) {
		Set<XDMIndexKey> keys = new HashSet<>();
		for (Object value: values) {
			keys.add(factory.newXDMIndexKey(pathId, value));
		}
		Map<XDMIndexKey, XDMIndexedValue> xidv = idxCache.getAll(keys);
		Set<Long> ids = new HashSet<>(xidv.size());
		for (XDMIndexedValue value: xidv.values()) {
			ids.addAll(value.getDocumentKeys());
		}
		updateStats(idx.getName(), true, xidv.size());
		updateStats(idx.getName(), false, keys.size() - xidv.size());
		return ids;
	}
	
	@SuppressWarnings("rawtypes")
	private Set<Long> getIndexedDocuments(XDMIndex idx, int pathId, Comparison cmp, Comparable value) {
		TreeMap<Comparable, Integer> range = rangeIndex.get(pathId);
		Map<Comparable, Integer> subRange;
		switch (cmp) {
			case GT: {
				subRange = range.tailMap(value);
				break;
			}
			case GE: {
				subRange = range.tailMap(value, true);
				break;
			}
			case LE: {
				subRange = range.headMap(value, true);
				break;
			}
			case LT: {
				subRange = range.headMap(value);
				break;
			}
			// TODO: implement other comparisons!
			default: subRange = Collections.emptyMap();
		}
		logger.trace("getIndexedDocuments; got subRange of length {}", subRange.size());
		Set<XDMIndexKey> keys = new HashSet<>(subRange.size());
		for (Object val: subRange.keySet()) {
			keys.add(factory.newXDMIndexKey(pathId, val));
		}
		Map<XDMIndexKey, XDMIndexedValue> values = idxCache.getAll(keys);
		Set<Long> ids = new HashSet<>(values.size());
		if (values.size() > 0) {
			for (XDMIndexedValue val: values.values()) {
				ids.addAll(val.getDocumentKeys());
			}
			updateStats(idx.getName(), true, 1);
		} else {
			updateStats(idx.getName(), false, 1);
		}
		return ids;
	}
	
	public Collection<Integer> getTypeIndexes(int docType, boolean uniqueOnly) {
		String root = mdlMgr.getDocumentRoot(docType);
		//Predicate p = Predicates.equal("typePath", root);
		//if (uniqueOnly) {
		//	Predicate u = Predicates.equal("unique", true);
		//	p = Predicates.and(p, u);
		//}
		Collection<Integer> result = new HashSet<>(); //idxDict.keySet(p);
		for (Map.Entry<Integer, XDMIndex> e: idxDict.entrySet()) {
			if (root.equals(e.getValue().getTypePath())) {
				if (uniqueOnly) {
					if (!e.getValue().isUnique()) {
						continue;
					}
				}
				result.add(e.getKey());
			}
		}
		logger.trace("getTypeIndexes.exit; returning {} path for type: {}, unique: {}", result.size(), docType, uniqueOnly);
		return result;
	}
	
	private void updateStats(String name, boolean success, int count) {
		if (enableStats) {
			if (!queue.offer(new StatisticsEvent(name, success, count))) {
				logger.warn("updateStats; queue is full!!");
			}
		}
	}

	private void updateStats(String name, boolean add, boolean unique, int count, int size) {
		if (enableStats) {
			if (!queue.offer(new StatisticsEvent(name, add, count, size, unique))) {
				logger.warn("updateIndexStats; queue is full!!");
			}
		}
	}
	
	@Override
	public boolean rebuildIndex(int pathId) {
		// TODO Auto-generated method stub
		return false;
	}

}
