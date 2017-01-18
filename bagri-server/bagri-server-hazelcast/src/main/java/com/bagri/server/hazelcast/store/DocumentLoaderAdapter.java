package com.bagri.server.hazelcast.store;

import static com.bagri.core.Constants.ctx_cache;
import static com.bagri.core.Constants.ctx_context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.DocumentStore;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;

public class DocumentLoaderAdapter implements MapLoader<DocumentKey, Document>, MapLoaderLifecycleSupport {
	
	protected DocumentStore extStore;
	
	public DocumentLoaderAdapter(DocumentStore extStore) {
		this.extStore = extStore;
	}
	
	@Override
	public void init(HazelcastInstance hzInstance, Properties properties, String mapName) {
		Map<String, Object> ctx = new HashMap<>();
		for (Object key: properties.keySet()) {
			ctx.put(key.toString(), properties.get(key));
		}
		//ctx.putAll(hzInstance.getUserContext());
		ctx.put(ctx_cache, hzInstance);
		ctx.put(ctx_context, hzInstance.getUserContext());
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

}
