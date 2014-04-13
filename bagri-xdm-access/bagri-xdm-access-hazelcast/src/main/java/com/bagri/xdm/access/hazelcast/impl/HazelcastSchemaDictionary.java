package com.bagri.xdm.access.hazelcast.impl;

import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_DOCTYPE_DICT;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_NAMESPACE_DICT;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_PATH_DICT;
import static com.bagri.xdm.access.api.XDMCacheConstants.SQN_DOCTYPE;
import static com.bagri.xdm.access.api.XDMCacheConstants.SQN_NAMESPACE;
import static com.bagri.xdm.access.api.XDMCacheConstants.SQN_PATH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.xdm.XDMDocumentType;
import com.bagri.xdm.XDMNamespace;
import com.bagri.xdm.XDMPath;
import com.bagri.xdm.access.api.XDMSchemaDictionaryBase;
import com.bagri.xdm.access.hazelcast.process.SchemaRegistrator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class HazelcastSchemaDictionary extends XDMSchemaDictionaryBase { 

	private Map<String, XDMNamespace> nsCache;
	private Map<String, XDMPath> pathCache;
	private Map<String, XDMDocumentType> typeCache;
	private IdGenerator<Long> nsGen;
	private IdGenerator<Long> pathGen;
	private IdGenerator<Long> typeGen;
	
	//@Autowired
	private HazelcastInstance hzInstance;

	public HazelcastSchemaDictionary(HazelcastInstance hzInstance) {
		super();
		this.hzInstance = hzInstance;
		initialize();
	}
	
	///public void setHzInstance(HazelcastInstance hzInstance) {
	//	this.hzInstance = hzInstance;
		//logger.debug("setHzInstange; got instance: {}", hzInstance.getName()); 
	//	initialize();
	//}
	
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
    
	@SuppressWarnings("unchecked")
	private void initialize() {
		nsCache = hzInstance.getMap(CN_XDM_NAMESPACE_DICT);
		pathCache = hzInstance.getMap(CN_XDM_PATH_DICT);
		typeCache = hzInstance.getMap(CN_XDM_DOCTYPE_DICT);
		nsGen = new HazelcastIdGenerator(hzInstance.getIdGenerator(SQN_NAMESPACE));
		pathGen = new HazelcastIdGenerator(hzInstance.getIdGenerator(SQN_PATH));
		typeGen = new HazelcastIdGenerator(hzInstance.getIdGenerator(SQN_DOCTYPE));
		getLogger().debug("initialize.exit; nsCache: {}", nsCache); 
	}
	
	@Override
	public XDMPath getPath(int pathId) {

		Predicate f = Predicates.equal("pathId", pathId);
		Set<Map.Entry<String, XDMPath>> entries = getNamedCache(pathCache).entrySet(f);
		if (entries.isEmpty()) {
			return null;
		}
		// check size > 1 ??
		return entries.iterator().next().getValue();
	}
	
	@Override
	public Collection<XDMPath> getTypePaths(int typeId) {
		
		Predicate f = Predicates.equal("typeId", typeId);
		Collection<XDMPath> entries = getNamedCache(pathCache).values(f);
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
		
		//Filter f = new EqualsFilter("getTypeId", typeId);
		Predicate f = Predicates.equal("typeId",  typeId);
		Set<Map.Entry<String, XDMDocumentType>> types = getNamedCache(typeCache).entrySet(f);
		if (types.size() == 0) {
			return null;
		}
		return (XDMDocumentType) types.iterator().next().getValue();
	}

	@Override
	protected Set getTypedPathEntries(int typeId) {
		//Filter f = new EqualsFilter("getTypeId", typeId);
		Predicate f = Predicates.equal("typeId",  typeId);
		Set<Map.Entry<String, XDMPath>> entries = getNamedCache(pathCache).entrySet(f);
		return entries;
	}
	
	@Override
	protected Set getTypedPathWithRegex(String regex, int typeId) {
		//Filter f = new AndFilter(new RegexFilter(new KeyExtractor(IdentityExtractor.INSTANCE), regex), 
		//		new EqualsFilter("getTypeId", typeId));
		Predicate f = Predicates.and(new Predicates.RegexPredicate("path", regex), Predicates.equal("typeId", typeId));
		Set<Map.Entry> entries = getNamedCache(pathCache).entrySet(f);
		return entries;
	}

	private IMap getNamedCache(Map cache) {
		return (IMap) cache;
	}

	@Override
	protected boolean lock(Map cache, Object key) {
		try {
			return getNamedCache(cache).tryLock(key, timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			getLogger().error("Interrupted on lock", ex);
			return false;
		}
	}

	@Override
	protected void unlock(Map cache, Object key) {
		getNamedCache(cache).unlock(key);
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
