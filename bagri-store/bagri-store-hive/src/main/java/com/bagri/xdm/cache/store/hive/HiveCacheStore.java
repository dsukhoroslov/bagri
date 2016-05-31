package com.bagri.xdm.cache.store.hive;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hive.HiveTemplate;

import com.bagri.xdm.common.XDMDocumentStore;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMDocument;

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
	public XDMDocument loadDocument(XDMDocumentKey key) {
		logger.trace("load.enter; key: {}", key);
		return null;
	}

	@Override
	public Map<XDMDocumentKey, XDMDocument> loadAllDocuments(Collection<XDMDocumentKey> keys) {
		logger.trace("loadAll.enter; keys: {}", keys.size());
		return null;
	}

	@Override
	public Set<XDMDocumentKey> loadAllDocumentKeys() {
		logger.trace("loadAllKeys.enter;");
		return null;
	}

	@Override
	public void storeDocument(XDMDocumentKey key, XDMDocument value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
	}

	@Override
	public void storeAllDocuments(Map<XDMDocumentKey, XDMDocument> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
	}

	@Override
	public void deleteDocument(XDMDocumentKey key) {
		logger.trace("delete.enter; key: {}", key);
	}

	@Override
	public void deleteAllDocuments(Collection<XDMDocumentKey> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
	}

}
