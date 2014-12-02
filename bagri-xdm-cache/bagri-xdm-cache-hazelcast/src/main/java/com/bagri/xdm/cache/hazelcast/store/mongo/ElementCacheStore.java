package com.bagri.xdm.cache.hazelcast.store.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.convert.MongoConverter;

import com.bagri.xdm.access.hazelcast.data.DocumentPathKey;
import com.bagri.xdm.domain.XDMElement;
import com.hazelcast.core.MapStore;
//import com.hazelcast.spring.mongodb.MongoMapStore;
import com.mongodb.DBObject;

public class ElementCacheStore implements MapStore<DocumentPathKey, XDMElement> {

    private static final Logger logger = LoggerFactory.getLogger(ElementCacheStore.class);
    
    //@Autowired
    //private MongoMapStore mongoCacheStore;
    private MapStore mongoCacheStore; 
    
    //public void setMongoCacheStore(MongoMapStore store) {
    //	this.mongoCacheStore = store;
    //}

	@Override
	public XDMElement load(DocumentPathKey key) {
		logger.trace("load.enter; key: {}", key);
		//key = getMongoTemplate().getConverter().convertToMongoType(key);
		Object mongoKey = null; //mongoCacheStore.getMongoTemplate().getConverter().convertToMongoType(key);
		XDMElement result = (XDMElement) mongoCacheStore.load(mongoKey);
		logger.trace("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Map<DocumentPathKey, XDMElement> loadAll(Collection<DocumentPathKey> keys) {
		logger.trace("loadAll.enter; keys: {}", keys.size());
		List mongoKeys = new ArrayList(keys.size());
		MongoConverter mc = null; //mongoCacheStore.getMongoTemplate().getConverter();
		for (Object key: keys) {
			mongoKeys.add(mc.convertToMongoType(key));
		}
		Map result = mongoCacheStore.loadAll(mongoKeys);
		logger.trace("loadAll; intermediate result: {}", result);
		Map<DocumentPathKey, XDMElement> result2 = new HashMap<DocumentPathKey, XDMElement>(result.size());
		for (Object o: result.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			DocumentPathKey key = mc.read(DocumentPathKey.class, (DBObject) e.getKey());
			result2.put(key, (XDMElement) e.getValue());
		}
		logger.trace("loadAll.exit; returning: {}", result2);
		return result2;
	}

	@Override
	public Set<DocumentPathKey> loadAllKeys() {
		logger.trace("loadAllKeys.enter;");
		Set result = mongoCacheStore.loadAllKeys();
		logger.trace("loadAllKeys; intermediate result: {}", result);
		Set<DocumentPathKey> result2 = new HashSet<DocumentPathKey>(result.size());
		MongoConverter mc = null; //mongoCacheStore.getMongoTemplate().getConverter();
		for (Object key: result) {
			if (key instanceof DBObject) {
				result2.add(mc.read(DocumentPathKey.class, (DBObject) key));
			}
		}
		logger.trace("loadAllKeys.exit; returning: {}", result2);
		return result2;
	}

	@Override
	public void store(DocumentPathKey key, XDMElement value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		//key = "" + ((DataDocumentKey) key).getDataId() + ":" + ((DataDocumentKey) key).getDocumentId();
		Object mongoKey = null; //mongoCacheStore.getMongoTemplate().getConverter().convertToMongoType(key);
		mongoCacheStore.store(mongoKey, value);
		logger.trace("store.exit;");
	}

	@Override
	public void storeAll(Map<DocumentPathKey, XDMElement> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
		Map map = new HashMap(entries.size());
		MongoConverter mc = null; //mongoCacheStore.getMongoTemplate().getConverter();
		for (Map.Entry<DocumentPathKey, XDMElement> entry: entries.entrySet()) {
			map.put(mc.convertToMongoType(entry.getKey()), entry.getValue());
		}
		mongoCacheStore.storeAll(map);
		logger.trace("storeAll.exit;");
	}

	@Override
	public void delete(DocumentPathKey key) {
		logger.trace("delete.enter; key: {}", key);
		Object mongoKey = null; //mongoCacheStore.getMongoTemplate().getConverter().convertToMongoType(key);
		mongoCacheStore.delete(mongoKey);
		logger.trace("delete.exit;");
	}

	@Override
	public void deleteAll(Collection<DocumentPathKey> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
		List mongoKeys = new ArrayList(keys.size());
		MongoConverter mc = null; //mongoCacheStore.getMongoTemplate().getConverter();
		for (Object key: keys) {
			mongoKeys.add(mc.convertToMongoType(key));
		}
		mongoCacheStore.deleteAll(mongoKeys);
		logger.trace("deleteAll.exit;");
	}

	
}
