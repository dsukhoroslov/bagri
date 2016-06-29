package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.cache.api.CacheConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.xdm.api.ModelManagement;
import com.bagri.xdm.api.impl.ModelManagementBase;
import com.bagri.xdm.domain.DocumentType;
import com.bagri.xdm.domain.Namespace;
import com.bagri.xdm.domain.Path;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;
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

	@Override
	public Path getPath(int pathId) {
		Predicate f = Predicates.equal("pathId", pathId);
		Collection<Path> entries = pathCache.values(f);
		if (entries.isEmpty()) {
			return null;
		}
		// check size > 1 ??
		return entries.iterator().next();
	}
	
	@Override
	public Collection<Path> getTypePaths(int typeId) {
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
	protected Set getTypedPathEntries(int typeId) {
		Predicate f = Predicates.equal("typeId",  typeId);
		Set<Map.Entry<String, Path>> entries = pathCache.entrySet(f);
		return entries;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	protected Set getTypedPathWithRegex(String regex, int typeId) {
		Predicate filter;
		if (typeId > 0) {
			filter = Predicates.and(new RegexPredicate("path", regex), Predicates.equal("typeId", typeId));
		} else {
			filter = new RegexPredicate("path", regex);
		}
		Set<Map.Entry<String, Path>> entries = pathCache.entrySet(filter);
		return entries;
	}

	//private IMap getNamedCache(Map cache) {
	//	return (IMap) cache;
	//}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean lock(Map cache, Object key) {
		try {
			return ((IMap) cache).tryLock(key, timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			logger.error("Interrupted on lock", ex);
			return false;
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void unlock(Map cache, Object key) {
		((IMap) cache).unlock(key);
	}

	@Override
	protected <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
		IMap<K, V> cache = (IMap<K, V>) map;
		V val2 = cache.putIfAbsent(key, value);
		if (val2 == null) {
			return value;
		}
		logger.debug("putIfAbsent; got collision on cache: {}, key: {}; returning: {}", 
				new Object[] {cache.getName(), key, val2});
		return val2;
	}

}
