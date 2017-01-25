package com.bagri.server.hazelcast.store;

import static com.bagri.core.Constants.ctx_repo;
import static com.bagri.core.Constants.ctx_popService;
import static com.bagri.core.Constants.pn_schema_format_default;
import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.core.Constants.pn_schema_store_data_path;
import static com.bagri.core.api.TransactionManagement.TX_INIT;
import static com.bagri.core.api.TransactionManagement.TX_NO;
import static com.bagri.core.model.Document.dvFirst;

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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.DocumentManagement;
import com.bagri.core.system.DataFormat;
import com.bagri.server.hazelcast.impl.DocumentManagementImpl;
import com.bagri.server.hazelcast.impl.PopulationManagementImpl;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.support.util.FileUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class FileDocumentCacheStore implements MapStore<DocumentKey, Document>, MapLoaderLifecycleSupport {

    private static final Logger logger = LoggerFactory.getLogger(FileDocumentCacheStore.class);
    
	private String dataPath;
    private String schemaName;
    private String dataFormat;
    private HazelcastInstance hzi;
    private SchemaRepositoryImpl xdmRepo;
    private PopulationManagementImpl popManager;
    
	@Override
	public void init(HazelcastInstance hzInstance, Properties properties, String mapName) {
		logger.info("init.enter; properties: {}", properties);
		this.hzi = hzInstance;
		popManager = (PopulationManagementImpl) hzInstance.getUserContext().get(ctx_popService);
		if (popManager == null) {
			logger.warn("init; PopulationManager not set, please check Spring configuration files..."); 
		}
		dataPath = properties.getProperty(pn_schema_store_data_path);
		if (dataPath == null) {
			logger.warn("init; dataPath not set, please check schema properties in config.xml"); 
		}
		schemaName = properties.getProperty(pn_schema_name);
		if (schemaName == null) {
			logger.warn("init; schemaName not set, please check node profile properties"); 
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}
	
	private synchronized void ensureRepository() {
		if (xdmRepo == null) {
			xdmRepo = (SchemaRepositoryImpl) hzi.getUserContext().get(ctx_repo);
			if (xdmRepo != null) {
				dataFormat = xdmRepo.getSchema().getProperty(pn_schema_format_default);
				while (!xdmRepo.isInitialized()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						// ??
						logger.warn("ensureRepository; interrupted while waiting for Repo initialization"); 
					}
				}
			}
			logger.info("ensureRepository; repo: {}", xdmRepo);
		}
	}
    
	private void processPathFiles(Path root, List<Path> files) throws IOException {
		DataFormat df = xdmRepo.getDataFormat(dataFormat);
		StringBuffer ext = new StringBuffer();
		if (df != null) {
			int cnt = 0;
			for (String e: df.getExtensions()) {
				if (cnt > 0) {
					ext.append(",");
				}
				ext.append("*.").append(e);
				cnt++;
			}
		} else {
			ext.append("*.").append(dataFormat.toLowerCase());
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, ext.toString())) {
		    for (Path path: stream) {
		        if (Files.isDirectory(path)) {
		            processPathFiles(path, files);
		        } else {
		    		//logger.trace("processPathFiles; path: {}; uri: {}", path.toString(), path.toUri().toString());
		            files.add(path.getFileName());
		        }
		    }
		}
	}
	
	private String getFullUri(String fileName) {
		return dataPath + "/" + fileName;
	}

	@Override
	public Set<DocumentKey> loadAllKeys() {
		//if (true) {
		//	return Collections.emptySet();
		//}
		
		ensureRepository();
		if (xdmRepo == null) {
			logger.trace("loadAllKeys.enter; store is not ready yet, skipping population");
			return null;
		}
		
		logger.trace("loadAllKeys.enter;");
		Set<DocumentKey> docIds = popManager.getDocumentKeys();
		if (docIds != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("loadAllKeys.exit; returning from PopulationManager: {}", docIds);
			} else {
				logger.info("loadAllKeys.exit; returning keys from PopulationManager: {}", docIds.size());
			}
			return docIds;
		}
	    
	    Path root = Paths.get(dataPath);
	    Map<DocumentKey, String> uris = new HashMap<>();
		try {
			List<Path> files = new ArrayList<>();
			processPathFiles(root, files);
			DocumentKey docKey; 
			for (Path path: files) {
				String uri = path.getFileName().toString();
				int revision = 0;
				do {
					docKey = xdmRepo.getFactory().newDocumentKey(uri, revision, dvFirst);
					revision++;
				} while (uris.get(docKey) != null);				
				uris.put(docKey, uri);
			}
			docIds = new HashSet<>(uris.keySet());
		} catch (IOException ex) {
			logger.error("loadAllKeys.error;", ex);
		}
   		popManager.setKeyMappings(uris);
		if (logger.isTraceEnabled()) {
			logger.trace("loadAllKeys.exit; got mappings: {}", uris);
		} else {
			logger.info("loadAllKeys.exit; returning keys: {}", docIds.size());
		}
		return docIds;
	}
	
	private Document loadDocument(DocumentKey docKey) {
    	String docUri = null;
    	Document doc = popManager.getDocument(docKey.getKey());
    	if (doc != null) {
    		if (doc.getTxFinish() > TX_NO) {
    			// no need to load content for inactive docs
    			return doc;
    		}
        	docUri = doc.getUri();
    	} else {
    		docUri = popManager.getKeyMapping(docKey);
    	}
		//logger.info("loadDocument; got uri: {} for key: {}; uris: {}", docUri, docKey, uris.size());

    	if (docUri != null) {
    		String fullUri = getFullUri(docUri);
			Path path = Paths.get(fullUri);
	    	if (Files.exists(path)) {
        		try {
        			String content = FileUtils.readTextFile(fullUri);
        			Document newDoc;
        			DocumentManagementImpl docManager = (DocumentManagementImpl) xdmRepo.getDocumentManagement(); 
        			if (doc == null) {
        				newDoc = docManager.createDocument(docKey, docUri, content, dataFormat, new Date(Files.getLastModifiedTime(path).toMillis()), 
        						Files.getOwner(path).getName(), TX_INIT, null, true);
        			} else {
        				newDoc = docManager.createDocument(docKey, docUri, content, dataFormat, doc.getCreatedAt(), doc.getCreatedBy(), doc.getTxStart(), 
        						doc.getCollections(), true);
        			}
       				return newDoc;
				} catch (IOException | BagriException ex) {
					logger.error("loadDocument.error", ex);
					// TODO: notify popManager about this?!
				}
	    	}
		}
    	return null;
    }
    
	@Override
	public Document load(DocumentKey key) {
		logger.trace("load.enter; key: {}", key);
		ensureRepository();
    	Document result = loadDocument(key);
		logger.trace("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Map<DocumentKey, Document> loadAll(Collection<DocumentKey> keys) {
		logger.debug("loadAll.enter; keys: {}; ", keys.size());
		ensureRepository();
		Map<DocumentKey, Document> result = new HashMap<>(keys.size());
	    for (DocumentKey key: keys) {
	    	Document doc = loadDocument(key);
	    	if (doc != null) {
	    		result.put(key, doc);
	    	}
	    }
		logger.debug("loadAll.exit; returning: {} documents for keys: {}", result.size(), keys.size());
		return result;
	}

	private Exception storeDocument(DocumentManagement docManager, DocumentKey key, Document value) {
		String docUri = popManager.getKeyMapping(key);
		if (docUri == null) {
			// create a new document
			//logger.trace("store; got path: {}; uri: {}", path, uri);
			docUri = value.getUri();
			popManager.setKeyMapping(key, docUri);
		} else {
			// update existing document - put a new version
		}
		
		String fullUri = getFullUri(docUri);
		try {
			String xml = docManager.getDocumentAsString(key, null);
			FileUtils.writeTextFile(fullUri, xml);
			logger.trace("storeDocument.exit; stored as: {}; length: {}", fullUri, xml.length());
			return null;
		} catch (IOException | BagriException ex) {
			return ex;
		}
	}
	
	@Override
	public void store(DocumentKey key, Document value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		ensureRepository();
		DocumentManagement docManager = (DocumentManagement) xdmRepo.getDocumentManagement();
		Exception ex = storeDocument(docManager, key, value);
		if (ex != null) {
			logger.error("store.error; exception on store document: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		} else {
			logger.trace("store.exit");
		}
	}

	@Override
	public void storeAll(Map<DocumentKey, Document> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
		ensureRepository();
		int cnt = 0;
		int err = 0;
		Exception ex = null;
		DocumentManagement docManager = (DocumentManagement) xdmRepo.getDocumentManagement();
		for (Map.Entry<DocumentKey, Document> entry: entries.entrySet()) {
			Exception e = storeDocument(docManager, entry.getKey(), entry.getValue());  
			if (e == null) {
				cnt++;
			} else {
				err++;
				ex = e;
			}
		}
		if (err == 0) {
			logger.trace("storeAll.exit; stored: {}", cnt);
		} else {
			logger.info("storeAll.exit; stored: {}; errors: {}", cnt, err);
			throw new RuntimeException(ex);
		}
	}
	
	private boolean deleteDocument(DocumentKey key) {
    	boolean result = false;
		String docUri = popManager.deleteKeyMapping(key);
		if (docUri != null) {
			docUri = getFullUri(docUri);
	    	Path path = Paths.get(docUri);
			try {
				result = Files.deleteIfExists(path);
			} catch (IOException ex) {
				logger.error("deleteDocument.error; path: " + path, ex);
			}
		}
		return result;
	}

	@Override
	public void delete(DocumentKey key) {
		logger.trace("delete.enter; key: {}", key);
    	boolean result = deleteDocument(key);
    	logger.trace("delete.exit; deleted: {}", result);
	}

	@Override
	public void deleteAll(Collection<DocumentKey> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
		int deleted = 0;
		for (DocumentKey key: keys) {
			if (deleteDocument(key)) {
				deleted++;
			}
		}
		logger.trace("deleteAll.exit; deleted: {}", deleted);
	}
	
}
