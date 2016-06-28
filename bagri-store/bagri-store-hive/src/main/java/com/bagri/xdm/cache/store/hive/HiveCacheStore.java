package com.bagri.xdm.cache.store.hive;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hive.HiveTemplate;

import com.bagri.xdm.cache.api.XDMDocumentStore;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.domain.Document;

public class HiveCacheStore implements XDMDocumentStore {

    private static final Logger logger = LoggerFactory.getLogger(HiveCacheStore.class);
    
    private HiveTemplate hiveTemplate;
    
    public void setHiveTemplate(HiveTemplate hiveTemplate) {
    	this.hiveTemplate =  hiveTemplate;
		logger.trace("setHiveTemplate; template: {}", hiveTemplate);
    }

	@Override
	public void init(Map<String, Object> context) {
		logger.trace("init.enter; context: {}", context);
	}

	@Override
	public void close() {
		logger.trace("close; ");
	}

	@Override
	public Document loadDocument(DocumentKey key) {
		logger.trace("load.enter; key: {}", key);
		return null;
	}

	@Override
	public Map<DocumentKey, Document> loadAllDocuments(Collection<DocumentKey> keys) {
		logger.trace("loadAll.enter; keys: {}", keys.size());
		return null;
	}

	@Override
	public Set<DocumentKey> loadAllDocumentKeys() {
		logger.trace("loadAllKeys.enter;");
		return null;
	}

	@Override
	public void storeDocument(DocumentKey key, Document value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
	}

	@Override
	public void storeAllDocuments(Map<DocumentKey, Document> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
	}

	@Override
	public void deleteDocument(DocumentKey key) {
		logger.trace("delete.enter; key: {}", key);
	}

	@Override
	public void deleteAllDocuments(Collection<DocumentKey> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
	}

}
