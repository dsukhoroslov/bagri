package com.bagri.xdm.cache.hazelcast.store.xml;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.xml.XDMStaxParser;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.MapStore;

public class ElementCacheStore extends XmlCacheStore implements MapStore<XDMDataKey, XDMElement> {

	private static final Logger logger = LoggerFactory.getLogger(ElementCacheStore.class);
	    
    private IdGenerator docGen;
	protected XDMFactory keyFactory;
    private com.bagri.common.idgen.IdGenerator<Long> idGen;
    private DocumentCacheStore docStore;
    private Map<XDMDataKey, XDMElement> elements;
    
    public void setDocumentIdGenerator(IdGenerator docGen) {
    	this.docGen = docGen;
    }
    
    public void setDocumentStore(DocumentCacheStore docStore) {
    	this.docStore = docStore;
    }

    public void setElementIdGenerator(com.bagri.common.idgen.IdGenerator<Long> idGen) {
    	this.idGen = idGen;
    }

	public void setKeyFactory(XDMFactory factory) {
		this.keyFactory = factory;
	}
    
	@Override
	public XDMElement load(XDMDataKey key) {
		logger.trace("load.enter; key: {}", key);
		XDMElement elt = elements.get(key);
		if (elt != null) {
			elements.remove(key);
		}
		logger.trace("load.exit; returning: {}", elt);
		return elt;
	}

	@Override
	public Map<XDMDataKey, XDMElement> loadAll(Collection<XDMDataKey> keys) {
		logger.trace("loadAll.enter; keys: {}", keys.size());
		Map<XDMDataKey, XDMElement> result = new HashMap<XDMDataKey, XDMElement>(keys.size());
		for (XDMDataKey key: keys) {
			XDMElement elt = elements.get(key);
			if (elt != null) {
				result.put(key, elt);
				elements.remove(key);
			}
		}
		logger.trace("loadAll.exit; returning: {}", result.size());
		return result;
	}

	@Override
	public Set<XDMDataKey> loadAllKeys() {
		logger.trace("loadAllKeys.enter; docStore: {}", docStore);
		long stamp = System.currentTimeMillis();
		Map<String, Long> docKeys = docStore.getDocumentKeys();
		elements = new HashMap<XDMDataKey, XDMElement>(docKeys.size());
		for (Map.Entry<String, Long> key: docKeys.entrySet()) {
			try {
				String xml = FileUtils.readTextFile(key.getKey());
				long docId = key.getValue();
				List<XDMElement> elts = XDMStaxParser.parseDocument(null, docId, idGen, xml);
				for (XDMElement elt: elts) {
					XDMDataKey xKey = keyFactory.newXDMDataKey(elt.getElementId(), docId);
					elements.put(xKey, elt);
				}
			} catch (IOException | XMLStreamException ex) {
				logger.error("loadAllKeys.error; key: " + key, ex);
			}
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("loadAllKeys.exit; loaded keys: {}; time taken: {}", elements.size(), stamp);
		return elements.keySet();
	}

	@Override
	public void store(XDMDataKey key, XDMElement value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
	}

	@Override
	public void storeAll(Map<XDMDataKey, XDMElement> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
	}

	@Override
	public void delete(XDMDataKey key) {
		logger.trace("delete.enter; key: {}", key);
	}

	@Override
	public void deleteAll(Collection<XDMDataKey> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
	}
    
}
