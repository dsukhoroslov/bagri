package com.bagri.xdm.cache.hazelcast.store.xml;

import java.io.File;
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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.MapStore;

public class DocumentCacheStore extends XmlCacheStore implements MapStore<String, XDMDocument> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentCacheStore.class);
    
    private IdGenerator docGen;
    private Map<String, Long> docKeys;
    
    public void setDocumentIdGenerator(IdGenerator docGen) {
    	this.docGen = docGen;
    }
    
    private XDMDocument newDocumentFromFile(File file) {
    	if (file.exists()) {
    		long docId = docGen.newId();
    		docKeys.put(file.getPath(), docId);
    		// @todo: get docType from dictionary
    		int docType = 1;
    		return new XDMDocument(docId, file.getPath(), docType, 1, 
    			new Date(file.lastModified()), "system", "UTF-8");
    	}
    	return null;
    }
    
	@Override
	public XDMDocument load(String key) {
		logger.trace("load.enter; key: {}", key);
    	File file = new File(key);
    	XDMDocument result = newDocumentFromFile(file);
		logger.trace("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Set<String> loadAllKeys() {
		logger.trace("loadAllKeys.enter;");
	    Set<String> docUris = null;
		Path path = Paths.get(getDataPath());
		try {
			docUris = new HashSet<String>(processPathFiles(path));
			if (docKeys == null) {
				docKeys = new HashMap<String, Long>(docUris.size());
			}
		} catch (IOException ex) {
			logger.error("loadAllKeys.error;", ex);
		}
		logger.trace("loadAllKeys.exit; returning: {}", docUris);
		return docUris;
	}
	
	Map<String, Long> getDocumentKeys() {
		return docKeys;
	}
	
	private List<String> processPathFiles(Path path) throws IOException {
		List<String> result = new ArrayList<String>(); 
		DirectoryStream<Path> ds = Files.newDirectoryStream(path, "*.xml");
	    for (Iterator<Path> itr = ds.iterator(); itr.hasNext();) {
	    	path = itr.next();
	        if (Files.isDirectory(path)) {
	            result.addAll(processPathFiles(path));
	        } else {
	            result.add(path.toString());
	        }
	    }
	    return result;
	}

	@Override
	public Map<String, XDMDocument> loadAll(Collection<String> keys) {
		logger.trace("loadAll.enter; keys: {}", keys);
		Map<String, XDMDocument> result = new HashMap<String, XDMDocument>(keys.size());
	    for (String path: keys) {
	    	File file = new File(path);
	    	XDMDocument doc = newDocumentFromFile(file);
	    	if (doc != null) {
	    		result.put(path, doc);
	    	}
	    }
		logger.trace("loadAll.exit; returning: {}", result);
		return result;
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
    	File file = new File(key);
    	boolean result = false;
    	if (file.exists()) {
    		result = file.delete();
    	}
    	logger.trace("delete.exit; deleted: {}", result);
	}

	@Override
	public void deleteAll(Collection<String> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
		int deleted = 0;
		for (String path: keys) {
	    	File file = new File(path);
	    	if (file.exists() && file.delete()) {
	    		deleted++;
	    	}
		}
		logger.trace("deleteAll.exit; deleted: {}", deleted);
	}

}
