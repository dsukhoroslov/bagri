package com.bagri.xdm.cache.hazelcast.store.xml;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.xml.XDMStaxParser;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMNodeKind;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class ElementCacheStore extends XmlCacheStore implements MapStore<XDMDataKey, XDMElements>, MapLoaderLifecycleSupport {

	private static final Logger logger = LoggerFactory.getLogger(ElementCacheStore.class);
	    
    private HazelcastInstance hzInstance;
	protected XDMFactory keyFactory;
    private DocumentCacheStore docStore;
    private Map<XDMDataKey, XDMElements> elements;
    private XDMSchemaDictionary schemaDict;
    
    // @TODO: refactor it to use XDMElements..

	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; properties: {}", properties);
		hzInstance = hazelcastInstance;
		schemaDict = (XDMSchemaDictionary) properties.get("xdmDictionary");
		keyFactory = (XDMFactory) properties.get("keyFactory");
	}

	@Override
	public void destroy() {
		// do nothing
	}
    
    public void setDocumentStore(DocumentCacheStore docStore) {
    	this.docStore = docStore;
    }

	//public void setKeyFactory(XDMFactory factory) {
	//	this.keyFactory = factory;
	//}
    
	@Override
	public XDMElements load(XDMDataKey key) {
		logger.trace("load.enter; key: {}", key);
		if (elements == null) {
			logger.trace("load.exit; not elements were loaded yet");
			return null;
		}
		
		XDMElements elt = elements.get(key);
		if (elt != null) {
			elements.remove(key);
		}
		logger.trace("load.exit; returning: {}", elt);
		return elt;
	}

	@Override
	public Map<XDMDataKey, XDMElements> loadAll(Collection<XDMDataKey> keys) {
		logger.trace("loadAll.enter; keys: {}", keys.size());
		if (elements == null) {
			logger.trace("loadAll.exit; not elements were loaded yet");
			return null;
		}

		Map<XDMDataKey, XDMElements> result = new HashMap<XDMDataKey, XDMElements>(keys.size());
		for (XDMDataKey key: keys) {
			XDMElements elt = elements.get(key);
			if (elt != null) {
				result.put(key, elt);
				elements.remove(key);
			}
		}
		logger.trace("loadAll.exit; returning: {}", result.size());
		return result;
	}

	private XDMData getDataRoot(List<XDMData> elements) {

		for (XDMData data: elements) {
			if (data.getNodeKind() == XDMNodeKind.element) {
				return data;
			}
		}
		return null;
	}
	
	@Override
	public Set<XDMDataKey> loadAllKeys() {
		if (schemaDict == null) {
			logger.trace("loadAllKeys.enter; dictionary is not set yet, skipping population");
			return null;
		}

		logger.trace("loadAllKeys.enter; dictionary: {}", schemaDict);
		long stamp = System.currentTimeMillis();
		Map<Long, DocumentDataHolder> docKeys = null; //docStore.getDocumentKeys();
		elements = new HashMap<XDMDataKey, XDMElements>(docKeys.size()); // size is much bigger!
		for (Map.Entry<Long, DocumentDataHolder> key: docKeys.entrySet()) {
			try {
				long docId = key.getKey();
				DocumentDataHolder data = key.getValue();
				String xml = FileUtils.readTextFile(data.uri);
				List<XDMData> elts = XDMStaxParser.parseDocument(schemaDict, xml);
				XDMData root = getDataRoot(elts);

				//logger.trace("loadAllKeys; data: {}", elts);
				
				if (root != null) {
					int docType = schemaDict.translateDocumentType(root.getPath());
					for (XDMData elt: elts) {
						XDMDataKey xdk = keyFactory.newXDMDataKey(docId, elt.getPathId());
						XDMElements xdes = elements.get(xdk);
						if (xdes == null) {
							xdes = new XDMElements();
							elements.put(xdk, xdes);
						}
						xdes.addElement(elt.getElement());
					}
					data.docType = docType;
					schemaDict.normalizeDocumentType(docType);
					//logger.trace("createDocument.exit; returning: {}", doc);
				} else {
					//logger.warn("createDocument.exit; the document is not valid as it has no root element, returning null");
				}
			} catch (IOException | XMLStreamException ex) {
				logger.error("loadAllKeys.error; key: " + key, ex);
			}
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("loadAllKeys.exit; loaded keys: {}; time taken: {}", elements.size(), stamp);
		return new HashSet<XDMDataKey>(elements.keySet());
	}

	@Override
	public void store(XDMDataKey key, XDMElements value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
	}

	@Override
	public void storeAll(Map<XDMDataKey, XDMElements> entries) {
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
