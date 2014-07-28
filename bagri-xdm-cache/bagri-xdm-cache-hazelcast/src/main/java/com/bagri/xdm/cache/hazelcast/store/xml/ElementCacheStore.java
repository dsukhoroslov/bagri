package com.bagri.xdm.cache.hazelcast.store.xml;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.xml.XDMStaxParser;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class ElementCacheStore extends XmlCacheStore implements MapStore<XDMDataKey, XDMElement>, MapLoaderLifecycleSupport {

	private static final Logger logger = LoggerFactory.getLogger(ElementCacheStore.class);
	    
	protected XDMFactory keyFactory;
    private IdGenerator<Long> idGen;
    private DocumentCacheStore docStore;
    private Map<XDMDataKey, XDMElement> elements;
    private XDMSchemaDictionary schemaDict;

	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; properties: {}", properties);
		schemaDict = (XDMSchemaDictionary) properties.get("xdmDictionary");
		idGen = (IdGenerator<Long>) properties.get("elementIdGenerator");
		keyFactory = (XDMFactory) properties.get("keyFactory");
	}

	@Override
	public void destroy() {
		// do nothing
	}
    
    public void setDocumentStore(DocumentCacheStore docStore) {
    	this.docStore = docStore;
    }

    //public void setElementIdGenerator(com.bagri.common.idgen.IdGenerator<Long> idGen) {
    //	this.idGen = idGen;
    //}

	//public void setKeyFactory(XDMFactory factory) {
	//	this.keyFactory = factory;
	//}
    
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

	private XDMElement getDocumentRoot(List<XDMElement> elements) {

		for (Iterator<XDMElement> itr = elements.iterator(); itr.hasNext();) {
			XDMElement xdm = itr.next();
			if (xdm.getKind() == XDMNodeKind.element) {
				return xdm;
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
		Map<String, DocumentDataHolder> docKeys = docStore.getDocumentKeys();
		elements = new HashMap<XDMDataKey, XDMElement>(docKeys.size());
		for (Map.Entry<String, DocumentDataHolder> key: docKeys.entrySet()) {
			try {
				String xml = FileUtils.readTextFile(key.getKey());
				DocumentDataHolder data = key.getValue();
				List<XDMElement> elts = XDMStaxParser.parseDocument(null, data.docId, idGen, xml);

				if (schemaDict != null) {
					XDMElement root = getDocumentRoot(elts);
					if (root != null) {
						int docType = schemaDict.translateDocumentType(root.getPath());
						for (XDMElement elt: elts) {
							XDMDataKey xKey = keyFactory.newXDMDataKey(elt.getElementId(), data.docId);
							elt.setPathId(schemaDict.translatePath(docType, elt.getPath(), XDMNodeKind.fromPath(elt.getPath())));
							elements.put(xKey, elt);
						}
						data.docType = docType;
						schemaDict.normalizeDocumentType(docType);
						//logger.trace("createDocument.exit; returning: {}", doc);
					} else {
						//logger.warn("createDocument.exit; the document is not valid as it has no root element, returning null");
					}
				} else {				
					for (XDMElement elt: elts) {
						XDMDataKey xKey = keyFactory.newXDMDataKey(elt.getElementId(), data.docId);
						elements.put(xKey, elt);
					}
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
