package com.bagri.server.hazelcast.impl;

import static com.bagri.core.server.api.CacheConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.bagri.client.hazelcast.impl.IdGeneratorImpl;
import com.bagri.core.model.DocumentType;
import com.bagri.core.model.Namespace;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementBase;
import com.bagri.support.idgen.IdGenerator;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.impl.predicates.RegexPredicate;

public class ModelManagementImpl extends ModelManagementBase implements ModelManagement { 

	protected IMap<String, Path> pathCache;
	//protected ReplicatedMap<String, XDMNamespace> nsCache;
	protected IMap<String, Namespace> nsCache;
	protected IMap<String, DocumentType> typeCache;
	private IdGenerator<Long> nsGen;
	private IdGenerator<Long> pathGen;
	private IdGenerator<Long> typeGen;
	
	private ConcurrentMap<Integer, Path> cachePath = new ConcurrentHashMap<>();
	private ConcurrentMap<Integer, Set<Path>> cacheType = new ConcurrentHashMap<>();

	//@Autowired
	//private HazelcastInstance hzInstance;

	public ModelManagementImpl() {
		super();
	}
	
	public ModelManagementImpl(HazelcastInstance hzInstance) {
		super();
		initialize(hzInstance);
	}
	
	private void initialize(HazelcastInstance hzInstance) {
		nsCache = hzInstance.getMap(CN_XDM_NAMESPACE_DICT);
		pathCache = hzInstance.getMap(CN_XDM_PATH_DICT);
		typeCache = hzInstance.getMap(CN_XDM_DOCTYPE_DICT);
		nsGen = new IdGeneratorImpl(hzInstance.getAtomicLong(SQN_NAMESPACE));
		pathGen = new IdGeneratorImpl(hzInstance.getAtomicLong(SQN_PATH));
		typeGen = new IdGeneratorImpl(hzInstance.getAtomicLong(SQN_DOCTYPE));
		// init listeners here
		pathCache.addEntryListener(new PathCacheListener(), true);
	}
	
	protected Map<String, Namespace> getNamespaceCache() {
		return nsCache;
	}
	
	protected Map<String, Path> getPathCache() {
		return pathCache;
	}
	
	protected Map<String, DocumentType> getTypeCache() {
		return typeCache;
	}
	
	protected IdGenerator<Long> getNamespaceGen() {
		return nsGen;
	}
	
	protected IdGenerator<Long> getPathGen() {
		return pathGen;
	}
	
	protected IdGenerator<Long> getTypeGen() {
		return typeGen;
	}

	public void setNamespaceCache(IMap<String, Namespace> nsCache) {
		this.nsCache = nsCache;
	}
	
	public void setPathCache(IMap<String, Path> pathCache) {
		this.pathCache = pathCache;
	}
	
	public void setTypeCache(IMap<String, DocumentType> typeCache) {
		this.typeCache = typeCache;
	}
	
	public void setNamespaceGen(IAtomicLong nsGen) {
		this.nsGen = new IdGeneratorImpl(nsGen);
	}

	public void setPathGen(IAtomicLong pathGen) {
		this.pathGen = new IdGeneratorImpl(pathGen);
	}
	
	public void setTypeGen(IAtomicLong typeGen) {
		this.typeGen = new IdGeneratorImpl(typeGen);
	}

	private Path getPathInternal(int pathId) {
		Predicate f = Predicates.equal("pathId", pathId);
		Collection<Path> entries = pathCache.values(f);
		if (entries.isEmpty()) {
			return null;
		}
		// check size > 1 ??
		return entries.iterator().next();
	}
	
	@Override
	public Path getPath(int pathId) {
		Path result = cachePath.get(pathId);
		if (result == null) {
			result = getPathInternal(pathId);
			if (result != null) {
				cachePath.putIfAbsent(pathId, result);
			}
		}
		return result;
	}
	
	private Collection<Path> getTypePathsInternal(int typeId) {
		Predicate<String, Path> f = Predicates.equal("typeId", typeId);
		Collection<Path> entries = pathCache.values(f);
		if (entries.isEmpty()) {
			return entries;
		}
		// check size > 1 ??
		List<Path> result = new ArrayList<Path>(entries);
		Collections.sort(result);
		if (logger.isTraceEnabled()) {
			logger.trace("getTypePath; returning {} for type {}", result, typeId);
		}
		return result;
	}
	
	@Override
	public Collection<Path> getTypePaths(int typeId) {
		Collection<Path> result = cacheType.get(typeId);
		// TODO: think why the result is empty? happens from ModelManagementImplTest only?
		if (result == null || result.isEmpty()) {
			result = getTypePathsInternal(typeId);
			if (result != null) {
				Set<Path> paths = new HashSet<>(result);
				paths = new HashSet<>();
				cacheType.putIfAbsent(typeId, paths);
			}
		}
		return result;
	}
	
	
	@Override
	protected DocumentType getDocumentTypeById(int typeId) {
		Predicate f = Predicates.equal("typeId", typeId);
		Set<Map.Entry<String, DocumentType>> types = typeCache.entrySet(f);
		if (types.size() == 0) {
			return null;
		}
		return (DocumentType) types.iterator().next().getValue();
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected Set<Map.Entry<String, Path>> getTypedPathEntries(int typeId) {
		Predicate f = Predicates.equal("typeId",  typeId);
		Set<Map.Entry<String, Path>> entries = pathCache.entrySet(f);
		return entries;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	protected Set<Map.Entry<String, Path>> getTypedPathWithRegex(String regex, int typeId) {
		Predicate filter = new RegexPredicate("path", regex);
		if (typeId > 0) {
			filter = Predicates.and(filter, Predicates.equal("typeId", typeId));
		}
		Set<Map.Entry<String, Path>> entries = pathCache.entrySet(filter);
		return entries;
	}

	//private IMap getNamedCache(Map cache) {
	//	return (IMap) cache;
	//}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <K> boolean lock(Map<K, ?> cache, K key) {
		try {
			return ((IMap) cache).tryLock(key, timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			logger.error("Interrupted on lock", ex);
			return false;
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <K> void unlock(Map<K, ?> cache, K key) {
		((IMap) cache).unlock(key);
	}

	@Override
	protected <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
		IMap<K, V> cache = (IMap<K, V>) map;
		V val2 = cache.putIfAbsent(key, value);
		if (val2 == null) {
			return value;
		}
		logger.debug("putIfAbsent; got collision on cache: {}, key: {}; returning: {}", cache.getName(), key, val2);
		return val2;
	}


	private class PathCacheListener implements MapClearedListener, MapEvictedListener,
		EntryAddedListener<String, Path>, EntryRemovedListener<String, Path>, EntryUpdatedListener<String, Path> {
	
		@Override
		public void mapEvicted(MapEvent event) {
			// don't think we have to clear everything in this case
		}
	
		@Override
		public void mapCleared(MapEvent event) {
			cachePath.clear();
			cacheType.clear();
		}
		
		@Override
		public void entryUpdated(EntryEvent<String, Path> event) {
			Path path = event.getValue();
			cachePath.put(path.getPathId(), path);
			Set<Path> paths = cacheType.get(path.getTypeId());
			if (paths == null) {
				paths = new HashSet<>();
				Set<Path> paths2 = cacheType.putIfAbsent(path.getTypeId(), paths);
				if (paths2 != null) {
					paths = paths2;
				}
			}
			paths.add(path);
		}
	
		@Override
		public void entryRemoved(EntryEvent<String, Path> event) {
			Path path = event.getValue();
			cachePath.remove(path.getPathId());
			Set<Path> paths = cacheType.get(path.getTypeId());
			if (paths != null) {
				paths.remove(path);
			}
		}
	
		@Override
		public void entryAdded(EntryEvent<String, Path> event) {
			Path path = event.getValue();
			cachePath.putIfAbsent(path.getPathId(), path);
			Set<Path> paths = cacheType.get(path.getTypeId());
			if (paths == null) {
				paths = new HashSet<>();
				Set<Path> paths2 = cacheType.putIfAbsent(path.getTypeId(), paths);
				if (paths2 != null) {
					paths = paths2;
				}
			}
			paths.add(path);
		}
	
	}
	
}
