package com.bagri.xdm.cache.hazelcast.store;

import java.util.Collection;
import java.util.Map;

import com.bagri.xdm.cache.api.DocumentStore;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.domain.Document;
import com.hazelcast.core.MapStore;

public class DocumentStoreAdapter extends DocumentLoaderAdapter implements MapStore<DocumentKey, Document> {
	
	public DocumentStoreAdapter(DocumentStore extStore) {
		super(extStore);
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

