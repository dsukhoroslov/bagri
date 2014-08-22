package com.bagri.xdm.cache.hazelcast.store.xml;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class DocumentCacheStore extends XmlCacheStore implements MapStore<String, XDMDocument>, MapLoaderLifecycleSupport {

    private static final Logger logger = LoggerFactory.getLogger(DocumentCacheStore.class);
    private static final int defVersion = 1;
    private static final String defEncoding = "UTF-8";
    
    private HazelcastInstance hzInstance;
    private boolean ready = false;
    private IdGenerator docGen;
    private Map<String, DocumentDataHolder> docKeys = new HashMap<String, DocumentDataHolder>();
    
	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; properties: {}", properties);
		this.hzInstance = hazelcastInstance;
		Object prop = properties.get("ready");
		ready = prop != null && (Boolean) prop;
		Object o = properties.get("documentIdGenerator");
		if (o != null) {
			docGen = (IdGenerator) o;
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}
    
	Map<String, DocumentDataHolder> getDocumentKeys() {
		return docKeys;
	}
	
    //public void setDocumentIdGenerator(IdGenerator docGen) {
    //	this.docGen = docGen;
    //}
    
	private void processPathFiles(Path path) throws IOException {
		DirectoryStream<Path> ds = Files.newDirectoryStream(path, "*.xml");
	    for (Iterator<Path> itr = ds.iterator(); itr.hasNext();) {
	    	path = itr.next();
	        if (Files.isDirectory(path)) {
	            processPathFiles(path);
	        } else {
	    		//logger.trace("processPathFiles; path: {}; uri: {}", path.toString(), path.toUri().toString());
	            docKeys.put(normalizePath(path), new DocumentDataHolder(docGen.newId()));
	        }
	    }
	}

	private String normalizePath(Path path) {
		return path.toAbsolutePath().normalize().toString();
	}
	
    private XDMDocument newDocumentFromPath(Path path) {
    	if (Files.exists(path)) {
    		String uri = normalizePath(path);
    		DocumentDataHolder data = docKeys.get(uri);
    		if (data == null) {
	    		logger.info("newDocumentFromPath; got unknown path: {}", path);
    			data = new DocumentDataHolder(docGen.newId());
    			// @todo: how will we get docType now ?
    			docKeys.put(uri, data);
    		}
    		try {
				return new XDMDocument(data.docId, uri, data.docType, defVersion, 
					new Date(Files.getLastModifiedTime(path).toMillis()), Files.getOwner(path).getName(), 
					defEncoding);
			} catch (IOException ex) {
				logger.error("newDocumentFromPath.error; path: " + path, ex);
			}
    	}
    	return null;
    }
    
	@Override
	public XDMDocument load(String key) {
		logger.trace("load.enter; key: {}", key);
		Path path = Paths.get(key);
    	XDMDocument result = newDocumentFromPath(path);
		logger.trace("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Map<String, XDMDocument> loadAll(Collection<String> keys) {
		logger.trace("loadAll.enter; keys: {}; Cluster size: {}", keys, hzInstance.getCluster().getMembers().size());
		Map<String, XDMDocument> result = new HashMap<String, XDMDocument>(keys.size());
	    for (String key: keys) {
			Path path = Paths.get(key);
	    	XDMDocument doc = newDocumentFromPath(path);
	    	if (doc != null) {
	    		result.put(key, doc);
	    	}
	    }
		logger.trace("loadAll.exit; returning: {}", result);
		return result;
	}

	@Override
	public Set<String> loadAllKeys() {
		if (!ready) {
			logger.trace("loadAllKeys.enter; store is not ready yet, skipping population");
			return null;
		}
		
		logger.trace("loadAllKeys.enter;");
	    Set<String> docUris = null;
		Path path = Paths.get(getDataPath());
		try {
			docKeys.clear();
			processPathFiles(path);
			docUris = new HashSet(docKeys.keySet());
		} catch (IOException ex) {
			logger.error("loadAllKeys.error;", ex);
		}
		logger.trace("loadAllKeys.exit; returning: {}", docUris);
		return docUris;
	}
	
	@Override
	public void store(String key, XDMDocument value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		
	}

	@Override
	public void storeAll(Map<String, XDMDocument> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
	}

	@Override
	public void delete(String key) {
		logger.trace("delete.enter; key: {}", key);
    	Path path = Paths.get(key);
    	boolean result = false;
		try {
			result = Files.deleteIfExists(path);
		} catch (IOException ex) {
			logger.error("delete.error; path: " + path, ex);
		}
    	logger.trace("delete.exit; deleted: {}", result);
	}

	@Override
	public void deleteAll(Collection<String> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
		int deleted = 0;
		for (String key: keys) {
	    	Path path = Paths.get(key);
			try {
				if (Files.deleteIfExists(path)) {
					deleted++;
				}
			} catch (IOException ex) {
				logger.error("deleteAll.error; path: " + path, ex);
			}
		}
		logger.trace("deleteAll.exit; deleted: {}", deleted);
	}
	
}
