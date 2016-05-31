package com.bagri.xdm.cache.hazelcast.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.bagri.xdm.common.XDMDocumentStore;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class DocumentStoreAdapter implements MapStore<XDMDocumentKey, XDMDocument>, MapLoaderLifecycleSupport {
	
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
	public XDMDocument load(XDMDocumentKey key) {
		return extStore.loadDocument(key);
	}

	@Override
	public Map<XDMDocumentKey, XDMDocument> loadAll(Collection<XDMDocumentKey> keys) {
		return extStore.loadAllDocuments(keys);
	}

	@Override
	public Iterable<XDMDocumentKey> loadAllKeys() {
		return extStore.loadAllDocumentKeys();
	}

	@Override
	public void store(XDMDocumentKey key, XDMDocument value) {
		extStore.storeDocument(key, value);
	}

	@Override
	public void storeAll(Map<XDMDocumentKey, XDMDocument> map) {
		extStore.storeAllDocuments(map);
	}

	@Override
	public void delete(XDMDocumentKey key) {
		extStore.deleteDocument(key);
	}

	@Override
	public void deleteAll(Collection<XDMDocumentKey> keys) {
		extStore.deleteAllDocuments(keys);
	}

}
