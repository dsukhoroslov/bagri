package com.bagri.xdm.access.coherence.impl;

import static com.bagri.xdm.cache.api.XDMCacheConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.xdm.api.impl.ModelManagementBase;
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.domain.XDMNamespace;
import com.bagri.xdm.domain.XDMPath;
import com.oracle.coherence.common.sequencegenerators.ClusteredSequenceGenerator;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.RegexFilter;

public class CoherenceSchemaDictionary extends ModelManagementBase { 
	
	private Map<String, XDMNamespace> nsCache;
	private Map<String, XDMPath> pathCache;
	private Map<String, XDMDocumentType> typeCache;
	private IdGenerator<Long> nsGen;
	private IdGenerator<Long> pathGen;
	private IdGenerator<Long> typeGen;
	
	private ConfigurableCacheFactory factory;

	public CoherenceSchemaDictionary() {
		super();
	}
	
	public CoherenceSchemaDictionary(ConfigurableCacheFactory factory) {
		super();
		this.factory = factory;
		initialize();
	}
	
	//public void setCacheFactory(ConfigurableCacheFactory factory) {
	//	this.factory = factory;
	//}
	
	protected Map<String, XDMNamespace> getNamespaceCache() {
		if (nsCache == null) {
			nsCache = CacheFactory.getCache(CN_XDM_NAMESPACE_DICT);
		}
		return nsCache;
	}
	
	protected Map<String, XDMPath> getPathCache() {
		if (pathCache == null) {
			pathCache = CacheFactory.getCache(CN_XDM_PATH_DICT);
		}
		return pathCache;
	}
	
	protected Map<String, XDMDocumentType> getTypeCache() {
		if (typeCache == null) {
			typeCache = CacheFactory.getCache(CN_XDM_DOCTYPE_DICT);
		}
		return typeCache;
	}
	
	protected IdGenerator<Long> getNamespaceGen() {
		if (nsGen == null) {
			nsGen = new ClusteredIdGenerator(new ClusteredSequenceGenerator(SQN_NAMESPACE, 1));
		}
		return nsGen;
	}
	
	protected IdGenerator<Long> getPathGen() {
		if (pathGen == null) {
			pathGen = new ClusteredIdGenerator(new ClusteredSequenceGenerator(SQN_PATH, 1));
		}
		return pathGen;
	}
	
	protected IdGenerator<Long> getTypeGen() {
		if (typeGen == null) {
			typeGen = new ClusteredIdGenerator(new ClusteredSequenceGenerator(SQN_DOCTYPE, 1));
		}
		return typeGen;
	}
    
	
	//@SuppressWarnings("unchecked")
	private void initialize() {
		
		nsCache = factory.ensureCache(CN_XDM_NAMESPACE_DICT, Thread.currentThread().getContextClassLoader());
		pathCache = factory.ensureCache(CN_XDM_PATH_DICT, Thread.currentThread().getContextClassLoader());
		typeCache = factory.ensureCache(CN_XDM_DOCTYPE_DICT, Thread.currentThread().getContextClassLoader());
		
		nsGen = new ClusteredIdGenerator(new ClusteredSequenceGenerator(SQN_NAMESPACE, 1));
		pathGen = new ClusteredIdGenerator(new ClusteredSequenceGenerator(SQN_PATH, 1));
		typeGen = new ClusteredIdGenerator(new ClusteredSequenceGenerator(SQN_DOCTYPE, 1));
	}
	
	@Override
	public XDMPath getPath(int pathId) {

		Filter f = new EqualsFilter("getPathId", pathId);
		Set<Map.Entry<String, XDMPath>> entries = getNamedCache(getPathCache()).entrySet(f);
		if (entries.isEmpty()) {
			return null;
		}
		// check size > 1 ??
		return entries.iterator().next().getValue();
	}
	
	@Override
	public Collection<XDMPath> getTypePaths(int typeId) {
		
		Filter f = new EqualsFilter("getTypeId", typeId);
		Set<Map.Entry<String, XDMPath>> entries = getNamedCache(getPathCache()).entrySet(f);
		if (entries.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		// check size > 1 ??
		List<XDMPath> result = new ArrayList<XDMPath>(entries.size());
		for (Map.Entry<String, XDMPath> e: entries) {
			result.add(e.getValue());
		}
		Collections.sort(result);
		if (getLogger().isTraceEnabled()) {
			getLogger().trace("getTypePath; returning {} for type {}", result, typeId);
		}
		return result;
	}
	
	
	@Override
	protected XDMDocumentType getDocumentTypeById(int typeId) {
		
		Filter f = new EqualsFilter("getTypeId", typeId);
		Set<Map.Entry<String, XDMDocumentType>> types = getNamedCache(getTypeCache()).entrySet(f);
		if (types.size() == 0) {
			return null;
		}
		return (XDMDocumentType) types.iterator().next().getValue();
	}

	@Override
	protected Set getTypedPathEntries(int typeId) {
		Filter f = new EqualsFilter("getTypeId", typeId);
		Set<Map.Entry<String, XDMPath>> entries = getNamedCache(getPathCache()).entrySet(f);
		return entries;
	}
	
	@Override
	protected Set getTypedPathWithRegex(String regex, int typeId) {
		Filter f = new AndFilter(new RegexFilter(new KeyExtractor(IdentityExtractor.INSTANCE), regex), 
				new EqualsFilter("getTypeId", typeId));
		Set<Map.Entry> entries = getNamedCache(getPathCache()).entrySet(f);
		return entries;
	}

	private NamedCache getNamedCache(Map cache) {
		return (NamedCache) cache;
	}

	@Override
	protected boolean lock(Map cache, Object key) {
		return getNamedCache(cache).lock(key, timeout);
	}

	@Override
	protected void unlock(Map cache, Object key) {
		getNamedCache(cache).unlock(key);
	}

	@Override
	protected <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
		NamedCache cache = (NamedCache) map;
		try {
			boolean locked = cache.lock(key, timeout);
			if (!locked) {
				throw new IllegalStateException("Can't get lock on cache " + cache.getCacheName() + " for key " + key);
			}

			V val2 = (V) cache.get(key);
			if (val2 == null) {
				map.put(key, value);
				return value;
			}
			getLogger().debug("putIfAbsent; got collision on cache: {}, key: {}; returning: {}", 
					new Object[] {cache.getCacheName(), key, val2});
			return val2;
		} finally {
			cache.unlock(key); 
		}
	}

}
