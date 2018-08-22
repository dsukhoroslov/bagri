package com.bagri.server.hazelcast.store;

import static com.bagri.core.Constants.ctx_repo;
import static com.bagri.core.Constants.ctx_popService;
import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.Constants.pn_schema_format_default;
import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.core.Constants.pn_schema_store_data_path;
import static com.bagri.core.Constants.pn_schema_store_load_count;
import static com.bagri.core.Constants.pn_schema_store_load_percent;
import static com.bagri.core.api.TransactionManagement.TX_INIT;
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
import com.bagri.core.api.DocumentAccessor;
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
    private int popCount;
    private int popPercent;
    private Properties props;
    
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
		popPercent = Integer.parseInt(properties.getProperty(pn_schema_store_load_percent, "100"));
		popCount = Integer.parseInt(properties.getProperty(pn_schema_store_load_count, "0"));
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
				props = new Properties();
				props.setProperty(pn_document_data_format, dataFormat);
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
	
	private void processPathFiles(Path root, Path current, final List<String> exts, List<Path> files) throws IOException {
		
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException {
				if (Files.isDirectory(entry)) {
					return true;
				}
				
				for (String ext: exts) {
					if (entry.toString().endsWith(ext)) {
						return true;
					}
				}
				return false;
			}
		};
		
		int count = 0;
		int found = 0;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(current, filter)) {
		    for (Path path: stream) {
				count++;
		        if (Files.isDirectory(path)) {
		            processPathFiles(root, path, exts, files);
		        } else {
		            files.add(root.relativize(path));
		            found++;
		        }
		    }
		}
		logger.debug("processPathFiles.exit; current: {}; files processed: {}; files accepted: {}", current.toString(), count, found);
	}
    
	private List<Path> getPathFiles(Path root) throws IOException {
		DataFormat df = xdmRepo.getDataFormat(dataFormat);
		final List<String> exts;
		if (df != null) {
			exts = new ArrayList<>(df.getExtensions().size());
			for (String ext: df.getExtensions()) {
				exts.add("." + ext);
			}
		} else {
			exts = new ArrayList<>(1);
			exts.add("." + dataFormat.toLowerCase());
		}
		
		List<Path> files = new ArrayList<>();
		processPathFiles(root, root, exts, files);
		return files;
	}
	
	private String getFullUri(String fileName) {
		return dataPath + "/" + fileName;
	}

	@Override
	public Set<DocumentKey> loadAllKeys() {
		ensureRepository();
		if (xdmRepo == null) {
			logger.debug("loadAllKeys.enter; store is not ready yet, skipping population");
			return null;
		}
		
		logger.trace("loadAllKeys.enter;");
		// what about partial load here!?
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
			List<Path> files = getPathFiles(root);
			DocumentKey docKey; 
			int keyCount = files.size();
			if (popCount > 0) {
				keyCount = popCount;
			} else {
				if (popPercent < 100) {
					keyCount = keyCount*popPercent/100;
				}
			}
			if (keyCount > files.size()) {
				keyCount = files.size();
			}
			logger.info("loadAllKeys; going to load {} keys out of {} files", keyCount, files.size());
			for (Path path: files) {
				String uri = path.toString();
				int revision = 0;
				do {
					docKey = xdmRepo.getFactory().newDocumentKey(uri, revision, dvFirst);
					revision++;
				} while (uris.get(docKey) != null);				
				uris.put(docKey, uri);
				// for partial load..
				if (uris.size() >= keyCount) {
					break;
				}
			}
			docIds = uris.keySet(); 
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
	
	//private String loadDocumentContent(String uri) throws IOException {
	//	String fullUri = getFullUri(uri);
	//	Path path = Paths.get(fullUri);
    //	if (Files.exists(path)) {
   	//		return FileUtils.readTextFile(fullUri);
    //	}
    //	return null;
	//}
	
	private String getSourceFormat(String uri) {
		int pos = uri.lastIndexOf(".");
		if (pos > 0) {
			return uri.substring(pos + 1).toUpperCase();
		}
		return dataFormat;
	}
	
	private Document loadDocument(DocumentKey docKey) {
    	String docUri = null;
    	Document doc = null;
   		try {
	    	doc = popManager.getDocument(docKey.getKey());
		} catch (Exception ex) {
			logger.error("loadDocument.error; error getting document from PM; docKey {}", docKey, ex);
		}

/*
 after cluster rebalancing got errors for lost documents: 
 
2018-07-05 21:39:47.944 [hz.Inventory-0.cached.thread-23] INFO  com.bagri.server.hazelcast.store.DocumentMemoryStore - getString; too long: 65536
2018-07-05 21:39:47.944 [hz.Inventory-0.cached.thread-23] INFO  com.bagri.server.hazelcast.store.DocumentMemoryStore - getString; too long: 942749486
2018-07-05 21:39:48.161 [hz.Inventory-0.cached.thread-23] ERROR com.bagri.server.hazelcast.impl.QueryManagementImpl - runQuery.error: 
java.nio.BufferUnderflowException: null
	at java.nio.DirectByteBuffer.get(DirectByteBuffer.java:271)
	at java.nio.ByteBuffer.get(ByteBuffer.java:715)
	at com.bagri.server.hazelcast.store.MemoryMappedStore.getString(MemoryMappedStore.java:304)
	at com.bagri.server.hazelcast.store.DocumentMemoryStore.readEntry(DocumentMemoryStore.java:135)
	at com.bagri.server.hazelcast.store.DocumentMemoryStore.readEntry(DocumentMemoryStore.java:23)
	at com.bagri.server.hazelcast.store.MemoryMappedStore.getEntry(MemoryMappedStore.java:110)
	at com.bagri.server.hazelcast.impl.PopulationManagementImpl.getDocument(PopulationManagementImpl.java:188)
	at com.bagri.server.hazelcast.store.FileDocumentCacheStore.loadDocument(FileDocumentCacheStore.java:187)
	at com.bagri.server.hazelcast.store.FileDocumentCacheStore.load(FileDocumentCacheStore.java:236)
*/
   		
   		if (doc != null) {
	    	if (!doc.isActive()) {
	    		// no need to load content for inactive docs
	    		return doc;
	    	}
	       	docUri = doc.getUri();
	    } else {
	    	docUri = popManager.getKeyMapping(docKey);
	    }
	
    	if (docUri != null) {
       		try {
	    		String fullUri = getFullUri(docUri);
				Path path = Paths.get(fullUri);
		    	if (Files.exists(path)) {
        			String content = FileUtils.readTextFile(fullUri);
        			String srcFormat = getSourceFormat(fullUri);
        			Document newDoc;
        			DocumentManagementImpl docManager = (DocumentManagementImpl) xdmRepo.getDocumentManagement(); 
        			if (doc == null) {
        				newDoc = docManager.createDocument(docKey, docUri, content, srcFormat, new Date(Files.getLastModifiedTime(path).toMillis()), 
        						Files.getOwner(path).getName(), TX_INIT, null);
        			} else {
        				newDoc = docManager.createDocument(docKey, docUri, content, srcFormat, doc.getCreatedAt(), 
        						doc.getCreatedBy(), doc.getTxStart(), doc.getCollections());
        			}
       				return newDoc;
		    	}
    		} catch (Exception ex) {
    			logger.error("loadDocument.error; error loading document {} with uri {} for key {}", doc, docUri, docKey, ex);
    			// TODO: notify popManager about this?!
    			// implement some retry policy here? skip/raise/retry
    		}
		}
    	return null;
    }
    
	@Override
	public Document load(DocumentKey key) {
		try {
			throw new RuntimeException();
		} catch (Exception ex) {
			logger.error("load", ex);
		}
		
		logger.debug("load.enter; key: {}", key);
		Document result = null;
		if (popManager.isPopulationAllowed()) {
			ensureRepository();
	    	result = loadDocument(key);
		}
		logger.debug("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Map<DocumentKey, Document> loadAll(Collection<DocumentKey> keys) {
		logger.debug("loadAll.enter; keys: {}; ", keys.size());
		Map<DocumentKey, Document> result;
		if (popManager.isPopulationAllowed()) {
			ensureRepository();
			popManager.addLoadingCounts(keys.size());
			int skipped = 0;
			result = new HashMap<>(keys.size());
		    for (DocumentKey key: keys) {
		    	if (popManager.isPopulationAllowed()) {
			    	Document doc = loadDocument(key);
			    	if (doc != null) {
			    		result.put(key, doc);
			    	}
		    	} else {
		    		skipped++;
		    	}
		    }
			popManager.addLoadedCounts(keys.size() - result.size() - skipped, result.size());
		} else {
			result = new HashMap<>(1);
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
			// must return doc in source format (XML/JSON)
			Properties props = new Properties();
			String srcFormat = getSourceFormat(fullUri);
			props.setProperty(pn_document_data_format, srcFormat);
			DocumentAccessor doc = 	docManager.getDocument(key, props);
			String content = doc.getContent();
			if (content == null) {
				logger.info("storeDocument.exit; got null content for key: {}; uri: {}", key, fullUri);
			} else {
				FileUtils.writeTextFile(fullUri, content);
				logger.trace("storeDocument.exit; stored as: {}; length: {}", fullUri, content.length());
			}
			return null;
		} catch (Exception ex) {
			logger.error("storeDocument.error; error storing document: {}", fullUri, ex);
			return ex;
		}
	}
	
	@Override
	public void store(DocumentKey key, Document value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		ensureRepository();
		// got document after merge
		DocumentManagement docManager = (DocumentManagement) xdmRepo.getDocumentManagement();
		Exception ex = storeDocument(docManager, key, value);
		if (ex != null) {
			//logger.error("store.error; exception on store document: " + ex.getMessage(), ex);
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
