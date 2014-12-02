package com.bagri.xdm.cache.hazelcast.store.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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

import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMCacheConstants;
import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.xml.XDMStaxParser;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMNodeKind;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.PartitionService;

public class DocumentCacheStore extends XmlCacheStore implements MapStore<Long, XDMDocument>, MapLoaderLifecycleSupport {

    private static final Logger logger = LoggerFactory.getLogger(DocumentCacheStore.class);
    private static final int defVersion = 1;
    private static final String defEncoding = "UTF-8";
    
    private HazelcastInstance hzInstance;
    //private IdGenerator docGen;
    private Map<Long, DocumentDataHolder> docKeys = new HashMap<Long, DocumentDataHolder>();
    
	//private String dataPath;
	private XDMFactory keyFactory;
    private XDMDocumentManagement docMgr;
    private XDMSchemaDictionary schemaDict;
    private IMap<XDMDataKey, XDMElements> xdmCache;
    
	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; properties: {}", properties);
		hzInstance = hazelcastInstance;
		//dataPath = (String) properties.get("dataPath");
		keyFactory = (XDMFactory) properties.get("keyFactory");
		docMgr = (XDMDocumentManagement) properties.get("xdmManager");
		schemaDict = (XDMSchemaDictionary) properties.get("xdmDictionary");
		//schemaDict = docMgr.getSchemaDictionary();
		xdmCache = hzInstance.getMap(XDMCacheConstants.CN_XDM_ELEMENT);
	}

	@Override
	public void destroy() {
		// do nothing
	}
    
	//Map<Long, DocumentDataHolder> getDocumentKeys() {
	//	return docKeys;
	//}
	
    //public void setDataPath(String dataPath) {
    //	this.dataPath = dataPath;
    //}
    
	private void processPathFiles(Path root, List<Path> files) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, "*.xml")) {
		    for (Path path: stream) {
		        if (Files.isDirectory(path)) {
		            processPathFiles(path, files);
		        } else {
		    		//logger.trace("processPathFiles; path: {}; uri: {}", path.toString(), path.toUri().toString());
		            files.add(path);
		        }
		    }
		}
	}

	private String normalizePath(Path path) {
		return path.toAbsolutePath().normalize().toString();
	}
	
    private XDMDocument newDocumentFromID(long docId) {
		DocumentDataHolder data = docKeys.get(docId);
		if (data != null) {
			Path path = Paths.get(data.uri);
	    	if (Files.exists(path)) {
	    		String uri = normalizePath(path);
	    		path = Paths.get(uri);
	    		uri = path.toUri().toString();
	    		
	    		try {
	        		Map<XDMDataKey, XDMElements> elements = loadElements(docId);
	        		xdmCache.putAll(elements);
	    			
					return new XDMDocument(docId, uri, data.docType, defVersion, 
						new Date(Files.getLastModifiedTime(path).toMillis()), Files.getOwner(path).getName(), 
						defEncoding);
				} catch (IOException | XMLStreamException ex) {
					logger.error("newDocumentFromID.error; path: " + path, ex);
				}
	    	}
		}
    	return null;
    }
    
	private XDMData getDataRoot(List<XDMData> elements) {

		for (XDMData data: elements) {
			if (data.getNodeKind() == XDMNodeKind.element) {
				return data;
			}
		}
		return null;
	}
    
	private Map<XDMDataKey, XDMElements> loadElements(long docId) throws IOException, XMLStreamException {
		logger.trace("loadElements.enter; docId: {}", docId);
		long stamp = System.currentTimeMillis();
		Map<XDMDataKey, XDMElements> elements = new HashMap<XDMDataKey, XDMElements>(); 

		DocumentDataHolder data = docKeys.get(docId);
		XDMStaxParser parser = new XDMStaxParser(schemaDict);
		List<XDMData> elts = parser.parse(new File(data.uri));
		XDMData root = getDataRoot(elts);

		//logger.trace("loadAllKeys; data: {}", elts);
				
		if (root != null) {
			int docType = schemaDict.translateDocumentType(root.getPath());
			for (XDMData elt: elts) {
				XDMDataKey xdk = keyFactory.newXDMDataKey(docId, elt.getPathId());
				XDMElements xdes = elements.get(xdk);
				if (xdes == null) {
					xdes = new XDMElements(elt.getPathId(), null);
					elements.put(xdk, xdes);
				}
				xdes.addElement(elt.getElement());
			}
			data.docType = docType;
			schemaDict.normalizeDocumentType(docType);
			// cache here loaded XML in xmlCache !?
		} else {
			logger.warn("loadElements; the document is not valid as it has no root element");
		}

		stamp = System.currentTimeMillis() - stamp;
		logger.trace("loadElements.exit; loaded elements: {}; time taken: {}", elements.size(), stamp);
		return elements;
	}
    
    
	@Override
	public XDMDocument load(Long key) {
		logger.trace("load.enter; key: {}", key);
    	XDMDocument result = newDocumentFromID(key);
		logger.trace("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Map<Long, XDMDocument> loadAll(Collection<Long> keys) {
		logger.trace("loadAll.enter; keys: {}; Cluster size: {}", keys, hzInstance.getCluster().getMembers().size());
		Map<Long, XDMDocument> result = new HashMap<Long, XDMDocument>(keys.size());
	    for (Long key: keys) {
	    	XDMDocument doc = newDocumentFromID(key);
	    	if (doc != null) {
	    		result.put(key, doc);
	    	}
	    }
		logger.trace("loadAll.exit; returning: {}", result);
		return result;
	}

	@Override
	public Set<Long> loadAllKeys() {
		if (schemaDict == null) {
			logger.trace("loadAllKeys.enter; store is not ready yet, skipping population");
			return null;
		}
		
		logger.trace("loadAllKeys.enter;");
	    Set<Long> docIds = null;
		Path root = Paths.get(getDataPath());
		try {
			docKeys.clear();
			List<Path> files = new ArrayList<Path>();
			processPathFiles(root, files);
			Collections.sort(files);
			long docId = 0;
			PartitionService ps = hzInstance.getPartitionService();
			for (Path path: files) {
				if (ps.getPartition(docId).getOwner().localMember()) {
					docKeys.put(docId, new DocumentDataHolder(normalizePath(path)));
				}
				docId++;
			}
			docIds = new HashSet<Long>(docKeys.keySet());
			// TODO: set DocumentIdGenerator to the current docId
			// ...
		} catch (IOException ex) {
			logger.error("loadAllKeys.error;", ex);
		}
		logger.trace("loadAllKeys.exit; returning: {}; docKeys: {}", docIds, docKeys);
		return docIds;
	}
	
	@Override
	public void store(Long key, XDMDocument value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		if (docMgr == null) {
			logger.trace("store; not ready yet, skipping store");
			return;
		}
		
		DocumentDataHolder data = docKeys.get(key);
		if (data == null) {
			// create a new document
			Path path = Paths.get(value.getUri());
			logger.trace("store; going to create new file {} at {}", path, getDataPath());
			if (!path.isAbsolute()) {
				Path root = Paths.get(getDataPath());
				path = root.resolve(path);
			}
    		String uri = normalizePath(path);
			logger.trace("store; got path: {}; uri: {}", path, uri);
    		data = new DocumentDataHolder(uri);
			docKeys.put(key, data);
		} else {
			// update existing document - put a new version
		}
		
		String xml = docMgr.getDocumentAsString(key);
		try {
			FileUtils.writeTextFile(data.uri, xml);
			logger.trace("store.exit; stored as: {}; length: {}", data.uri, xml.length());
		} catch (IOException ex) {
			logger.error("store.error; exception on store document: " + ex.getMessage(), ex);
		}
	}

	@Override
	public void storeAll(Map<Long, XDMDocument> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
		for (Map.Entry<Long, XDMDocument> entry: entries.entrySet()) {
			store(entry.getKey(), entry.getValue());
		}
		logger.trace("storeAll.exit; stored: {}", entries.size());
	}

	@Override
	public void delete(Long key) {
		logger.trace("delete.enter; key: {}", key);
    	boolean result = false;
		DocumentDataHolder data = docKeys.get(key);
		if (data != null) {
	    	Path path = Paths.get(data.uri);
			try {
				result = Files.deleteIfExists(path);
			} catch (IOException ex) {
				logger.error("delete.error; path: " + path, ex);
			}
		}
    	logger.trace("delete.exit; deleted: {}", result);
	}

	@Override
	public void deleteAll(Collection<Long> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
		int deleted = 0;
		for (Long key: keys) {
			DocumentDataHolder data = docKeys.get(key);
			if (data != null) {
		    	Path path = Paths.get(data.uri);
				try {
					if (Files.deleteIfExists(path)) {
						deleted++;
					}
				} catch (IOException ex) {
					logger.error("deleteAll.error; path: " + path, ex);
				}
			}
		}
		logger.trace("deleteAll.exit; deleted: {}", deleted);
	}
	
}
