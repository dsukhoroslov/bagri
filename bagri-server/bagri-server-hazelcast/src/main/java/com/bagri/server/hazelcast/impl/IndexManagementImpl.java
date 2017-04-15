package com.bagri.server.hazelcast.impl;

import static com.bagri.core.api.TransactionManagement.TX_NO;
import static com.bagri.support.util.XQUtils.getAtomicValue;
import static com.bagri.support.util.XQUtils.getBaseTypeForTypeName;
import static com.bagri.support.util.XQUtils.isStringTypeCompatible;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.DocumentKey;
import com.bagri.core.IndexKey;
import com.bagri.core.KeyFactory;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.IndexedDocument;
import com.bagri.core.model.IndexedValue;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.model.UniqueDocument;
import com.bagri.core.model.UniqueValue;
import com.bagri.core.query.Comparison;
import com.bagri.core.query.PathBuilder;
import com.bagri.core.query.PathExpression;
import com.bagri.core.server.api.IndexManagement;
import com.bagri.core.system.Index;
import com.bagri.support.stats.StatisticsEvent;
import com.bagri.support.util.JMXUtils;
import com.bagri.support.util.XQUtils;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class IndexManagementImpl implements IndexManagement { //, StatisticsProvider {
	
	private static final transient Logger logger = LoggerFactory.getLogger(IndexManagementImpl.class);
	private ReplicatedMap<Integer, Index> idxDict;
	private IMap<IndexKey, IndexedValue> idxCache;
	//private IExecutorService execService;
	private Map<Integer, TreeMap<Comparable, Integer>> rangeIndex = new HashMap<>();
	private Map<Index, Pattern> patterns = new HashMap<>();

	private KeyFactory factory;
	private ModelManagementImpl mdlMgr;
	private DocumentManagementImpl docMgr;
	private TransactionManagementImpl txMgr;
    
	private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;

	protected ModelManagementImpl getModelManager() {
		return this.mdlMgr;
	}
	
	protected Map<Integer, Index> getIndexDictionary() {
		return idxDict;
	}
	
	IMap<IndexKey, IndexedValue> getIndexCache() {
		return idxCache;
	}

	//public void setDataCache(IMap<XDMDataKey, XDMElements> xdmCache) {
	//	this.xdmCache = xdmCache;
	//}
    
	public void setIndexDictionary(ReplicatedMap<Integer, Index> idxDict) {
		this.idxDict = idxDict;
	}
	
	public void setIndexCache(IMap<IndexKey, IndexedValue> idxCache) {
		this.idxCache = idxCache;
	}
    
	public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
		this.queue = queue;
	}

	public void setStatsEnabled(boolean enable) {
		this.enableStats = enable;
	}

	public void setRepository(SchemaRepositoryImpl repo) {
		//this.repo = repo;
		this.factory = repo.getFactory();
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
		Index idx = idxDict.get(pathId);
		if (idx != null) {
			return idx.isEnabled();
		}
		return false;
	}
	
	@Override
	public Path[] createIndex(Index index) throws BagriException {
		Set<Integer> paths = getPathsForIndex(index);
		Path[] result = new Path[paths.size()];
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
			//String path = mdlMgr.normalizePath(index.getPath());
			String path = index.getPath();
			path = path.replaceAll("\\{", Matcher.quoteReplacement("\\{"));
			path = path.replaceAll("\\}", Matcher.quoteReplacement("\\}"));
			patterns.put(index, Pattern.compile(PathBuilder.regexFromPath(path)));
		}
		return result;
	}
	
	@Override
	public Path[] dropIndex(Index index) throws BagriException {
		// we must not do translate here!
		Set<Integer> paths = getPathsForIndex(index);
		Path[] result = new Path[paths.size()];
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
	
	private Set<Integer> getPathsForIndex(Index index) throws BagriException {
		int docType = mdlMgr.translateDocumentType(index.getDocumentType());
		String path = index.getPath();
		Set<Integer> result;
		if (PathBuilder.isRegexPath(path)) {
			//path = mdlMgr.normalizePath(path);
			result = mdlMgr.translatePathFromRegex(docType, PathBuilder.regexFromPath(path));
		} else {
			int dataType = XQUtils.getBaseTypeForTypeName(index.getDataType());
			Path xPath = mdlMgr.translatePath(docType, path, NodeKind.fromPath(path), dataType, Occurrence.zeroOrOne);
			result = new HashSet<>(1);
			result.add(xPath.getPathId());
		}
		logger.trace("getPathsForIndex; returning {} for index {}", result, index);
		return result;
	}
	
	private boolean isPatternIndex(Index index) {
		return PathBuilder.isRegexPath(index.getPath());
	}

	private Collection<Index> getPathIndexes(int pathId, String path) {
		Set<Index> result = new HashSet<>();
		Index idx = idxDict.get(pathId);
		if (idx != null) {
			result.add(idx);
		} else {
			for (Map.Entry<Index, Pattern> e: patterns.entrySet()) {
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
	
	public void addIndex(long docId, int pathId, String path, Object value) throws BagriException {

		// shouldn't we index NULL values too? create special NULL class for this..
		if (value != null) {
			Collection<Index> indexes = getPathIndexes(pathId, path);
			if (indexes.isEmpty()) {
				return;
			}
			
			for (Index idx: indexes) {
				indexPath(idx, docId, pathId, value);
			}
		}
	}
	
	private Object getIndexedValue(Index idx, int pathId, Object value) {
		int baseType = getBaseTypeForTypeName(idx.getDataType());
		if (isStringTypeCompatible(baseType)) {
			value = value.toString();
			if (!idx.isCaseSensitive()) {
				value = ((String) value).toLowerCase();
			}
		} else {
			Path xPath = mdlMgr.getPath(pathId);
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
	
	private void indexPath(Index idx, long docKey, int pathId, Object value) throws BagriException {

		value = getIndexedValue(idx, pathId, value);
		logger.trace("indexPath; index: {}, value: {}({})", idx, value.getClass().getName(), value);
			
		int oldCount = 0;
		boolean first = false;
		IndexKey xid = factory.newIndexKey(pathId, value);
		IndexedValue xidx = idxCache.get(xid);
		if (idx.isUnique()) {
			long id = DocumentKey.toDocumentId(docKey);
			if (!checkUniquiness((UniqueDocument) xidx, id)) {
				throw new BagriException("unique index '" + idx.getName() + "' violated for docKey: " + 
						docKey + ", pathId: " + pathId + ", value: " + value, BagriException.ecIndexUnique);
			}

			if (xidx == null) {
				xidx = new UniqueDocument();
				first = true;
			} else {
				oldCount = xidx.getCount();
			}
			xidx.addDocument(docKey, txMgr.getCurrentTxId());
			IndexedValue xidx2 = idxCache.put(xid, xidx);
			// why it is done second time here? because it can be new xidx!
			if (!checkUniquiness((UniqueDocument) xidx2, id)) {
				// shouldn't we delete just created xidx then ?
				throw new BagriException("unique index '" + idx.getName() + "' violated for docKey: " + 
						docKey + ", pathId: " + pathId + ", value: " + value, BagriException.ecIndexUnique);
			}
		} else {
			if (xidx == null) {
				xidx = new IndexedDocument(docKey);
				first = true;
			} else { 
				oldCount = xidx.getCount();
				xidx.addDocument(docKey, TransactionManagement.TX_NO);
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

	private boolean checkUniquiness(UniqueDocument uidx, long docId) throws BagriException {
		//long id = XDMDocumentKey.toDocumentId(docId);
		// check xidx.docIds - update document UC..
		if (uidx != null) {
			Collection<UniqueValue> ids = uidx.getDocumentValues();
			for (UniqueValue uv: ids) {
				long id = DocumentKey.toDocumentId(uv.getDocumentKey());
				if (id == docId) {
					// skipping index for the same document's family
					continue;
				}
				
				if (docMgr.checkDocumentCommited(uv.getDocumentKey(), 0) != null) {
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
		IndexKey iKey = factory.newIndexKey(pathId, value);
		// will have collisions here when two threads change/delete the same index!
		// but not for unique index!
		IndexedValue xIdx = idxCache.get(iKey);
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
				Index idx = idxDict.get(pathId);
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
		Index idx = idxDict.get(pathId);
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
	
	private Set<Long> getIndexedDocuments(Index idx, int pathId, Object value) {
		IndexKey idxk = factory.newIndexKey(pathId, value);
		IndexedValue xidv = idxCache.get(idxk);
		if (xidv != null) {
			updateStats(idx.getName(), true, 1);
			return xidv.getDocumentKeys();
		}
		updateStats(idx.getName(), false, 1);
		return Collections.emptySet();
	}
	
	private Set<Long> getIndexedDocuments(Index idx, int pathId, Iterable values) {
		Set<IndexKey> keys = new HashSet<>();
		for (Object value: values) {
			keys.add(factory.newIndexKey(pathId, value));
		}
		Map<IndexKey, IndexedValue> xidv = idxCache.getAll(keys);
		Set<Long> ids = new HashSet<>(xidv.size());
		for (IndexedValue value: xidv.values()) {
			ids.addAll(value.getDocumentKeys());
		}
		updateStats(idx.getName(), true, xidv.size());
		updateStats(idx.getName(), false, keys.size() - xidv.size());
		return ids;
	}
	
	@SuppressWarnings("rawtypes")
	private Set<Long> getIndexedDocuments(Index idx, int pathId, Comparison cmp, Comparable value) {
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
		Set<IndexKey> keys = new HashSet<>(subRange.size());
		for (Object val: subRange.keySet()) {
			keys.add(factory.newIndexKey(pathId, val));
		}
		Map<IndexKey, IndexedValue> values = idxCache.getAll(keys);
		Set<Long> ids = new HashSet<>(values.size());
		if (values.size() > 0) {
			for (IndexedValue val: values.values()) {
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
		for (Map.Entry<Integer, Index> e: idxDict.entrySet()) {
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
			if (!queue.offer(new StatisticsEvent(name, success, new Object[] {count}))) {
				logger.warn("updateStats; queue is full!!");
			}
		}
	}

	private void updateStats(String name, boolean add, boolean unique, int count, int size) {
		if (enableStats) {
			if (!queue.offer(new StatisticsEvent(name, add, new Object[] {count, size, unique}))) {
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
