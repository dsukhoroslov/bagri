package com.bagri.xdm.access.hazelcast.impl;

import static com.bagri.xdm.access.api.XDMCacheConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.xdm.access.api.XDMSchemaDictionaryBase;
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.domain.XDMNamespace;
import com.bagri.xdm.domain.XDMPath;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class HazelcastSchemaDictionary extends XDMSchemaDictionaryBase { 

	private IMap<String, XDMPath> pathCache;
	private IMap<String, XDMNamespace> nsCache;
	private IMap<String, XDMDocumentType> typeCache;
	private IdGenerator<Long> nsGen;
	private IdGenerator<Long> pathGen;
	private IdGenerator<Long> typeGen;
	
	//@Autowired
	//private HazelcastInstance hzInstance;

	public HazelcastSchemaDictionary() {
		super();
	}
	
	public HazelcastSchemaDictionary(HazelcastInstance hzInstance) {
		super();
		initialize(hzInstance);
	}
	
	@SuppressWarnings("unchecked")
	private void initialize(HazelcastInstance hzInstance) {
		nsCache = hzInstance.getMap(CN_XDM_NAMESPACE_DICT);
		pathCache = hzInstance.getMap(CN_XDM_PATH_DICT);
		typeCache = hzInstance.getMap(CN_XDM_DOCTYPE_DICT);
		nsGen = new HazelcastIdGenerator(hzInstance.getAtomicLong(SQN_NAMESPACE));
		pathGen = new HazelcastIdGenerator(hzInstance.getAtomicLong(SQN_PATH));
		typeGen = new HazelcastIdGenerator(hzInstance.getAtomicLong(SQN_DOCTYPE));
	}
	
	protected Map<String, XDMNamespace> getNamespaceCache() {
		return nsCache;
	}
	
	protected Map<String, XDMPath> getPathCache() {
		return pathCache;
	}
	
	protected Map<String, XDMDocumentType> getTypeCache() {
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

	public void setNamespaceCache(IMap<String, XDMNamespace> nsCache) {
		this.nsCache = nsCache;
	}
	
	public void setPathCache(IMap<String, XDMPath> pathCache) {
		this.pathCache = pathCache;
	}
	
	public void setTypeCache(IMap<String, XDMDocumentType> typeCache) {
		this.typeCache = typeCache;
	}
	
	public void setNamespaceGen(IAtomicLong nsGen) {
		this.nsGen = new HazelcastIdGenerator(nsGen);
	}

	public void setPathGen(IAtomicLong pathGen) {
		this.pathGen = new HazelcastIdGenerator(pathGen);
	}
	
	public void setTypeGen(IAtomicLong typeGen) {
		this.typeGen = new HazelcastIdGenerator(typeGen);
	}

	@Override
	public XDMPath getPath(int pathId) {

		Predicate f = Predicates.equal("pathId", pathId);
		Collection<XDMPath> entries = pathCache.values(f);
		if (entries.isEmpty()) {
			return null;
		}
		// check size > 1 ??
		return entries.iterator().next();
	}
	
	@Override
	public Collection<XDMPath> getTypePaths(int typeId) {
		
		Predicate f = Predicates.equal("typeId", typeId);
		Collection<XDMPath> entries = pathCache.values(f);
		if (entries.isEmpty()) {
			return entries;
		}
		// check size > 1 ??
		List<XDMPath> result = new ArrayList<XDMPath>(entries);
		Collections.sort(result);
		if (getLogger().isTraceEnabled()) {
			getLogger().trace("getTypePath; returning {} for type {}", result, typeId);
		}
		return result;
	}
	
	@Override
	protected XDMDocumentType getDocumentTypeById(int typeId) {
		Predicate f = Predicates.equal("typeId",  typeId);
		Set<Map.Entry<String, XDMDocumentType>> types = typeCache.entrySet(f);
		if (types.size() == 0) {
			return null;
		}
		return (XDMDocumentType) types.iterator().next().getValue();
	}

	@Override
	protected Set getTypedPathEntries(int typeId) {
		Predicate f = Predicates.equal("typeId",  typeId);
		Set<Map.Entry<String, XDMPath>> entries = pathCache.entrySet(f);
		return entries;
	}
	
	@Override
	protected Set getTypedPathWithRegex(String regex, int typeId) {
		//Filter f = new AndFilter(new RegexFilter(new KeyExtractor(IdentityExtractor.INSTANCE), regex), 
		//		new EqualsFilter("getTypeId", typeId));
		Predicate f = Predicates.and(new Predicates.RegexPredicate("path", regex), Predicates.equal("typeId", typeId));
		Set<Map.Entry<String, XDMPath>> entries = pathCache.entrySet(f);
		return entries;
	}

	//private IMap getNamedCache(Map cache) {
	//	return (IMap) cache;
	//}

	@Override
	protected boolean lock(Map cache, Object key) {
		try {
			return ((IMap) cache).tryLock(key, timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			getLogger().error("Interrupted on lock", ex);
			return false;
		}
	}

	@Override
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
		getLogger().debug("putIfAbsent; got collision on cache: {}, key: {}; returning: {}", 
				new Object[] {cache.getName(), key, val2});
		return val2;
	}

	//@Override
	//public void registerSchema(String schema) {
	//	
	//	long stamp = System.currentTimeMillis();
	//	getLogger().trace("registerSchema.enter; schema: {}", schema);
	//	
	//	SchemaRegistrator task = new SchemaRegistrator(schema);
	//	Future<Integer> future = execService.submitToKeyOwner(task, 0);
	//	getLogger().trace("registerSchema; the task submit; feature: {}", future);
	//	Integer result;
	//	try {
	//		result = future.get();
	//		getLogger().trace("registerSchema.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
	//		//return (XDMDocument) result;
	//	} catch (Throwable ex) {
	//		getLogger().error("registerSchema: ", ex);
	//	}
	//}

	//@Override
	//public void registerSchemaUri(String schemaUri) {
	//	// TODO Auto-generated method stub
	//	
	//}
	
}
