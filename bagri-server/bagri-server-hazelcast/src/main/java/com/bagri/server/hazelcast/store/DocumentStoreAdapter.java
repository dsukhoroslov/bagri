package com.bagri.server.hazelcast.store;

import java.util.Collection;
import java.util.Map;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.DocumentStore;
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

