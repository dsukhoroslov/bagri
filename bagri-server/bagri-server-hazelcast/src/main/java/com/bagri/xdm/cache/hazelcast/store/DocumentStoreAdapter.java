package com.bagri.xdm.cache.hazelcast.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.bagri.xdm.cache.api.XDMDocumentStore;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.domain.Document;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class DocumentStoreAdapter implements MapStore<DocumentKey, Document>, MapLoaderLifecycleSupport {
	
	private XDMDocumentStore extStore;
	
	public DocumentStoreAdapter(XDMDocumentStore extStore) {
		this.extStore = extStore;
	}
	
	@Override
	public void init(HazelcastInstance hzInstance, Properties properties, String mapName) {
		Map<String, Object> ctx = new HashMap<>();
		for (Object key: properties.keySet()) {
			ctx.put(key.toString(), properties.get(key));
		}
		ctx.putAll(hzInstance.getUserContext());
		ctx.put("hzInstance", hzInstance);
		// TODO: get persistence properties from repo.getSchema and pass them to extStore too!
		extStore.init(ctx);
	}

	@Override
	public void destroy() {
		extStore.close();
	}

	@Override
	public Document load(DocumentKey key) {
		return extStore.loadDocument(key);
	}

	@Override
	public Map<DocumentKey, Document> loadAll(Collection<DocumentKey> keys) {
		return extStore.loadAllDocuments(keys);
	}

	@Override
	public Iterable<DocumentKey> loadAllKeys() {
		return extStore.loadAllDocumentKeys();
	}

	@Override
	public void store(DocumentKey key, Document value) {
		extStore.storeDocument(key, value);
	}

	@Override
	public void storeAll(Map<DocumentKey, Document> map) {
		extStore.storeAllDocuments(map);
	}

	@Override
	public void delete(DocumentKey key) {
		extStore.deleteDocument(key);
	}

	@Override
	public void deleteAll(Collection<DocumentKey> keys) {
		extStore.deleteAllDocuments(keys);
	}

}
