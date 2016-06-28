package com.bagri.xdm.cache.hazelcast.store;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_INIT;
import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;
import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.getContext;
import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.schema_context;
import static com.bagri.xdm.common.XDMConstants.xdm_schema_format_default;
import static com.bagri.xdm.common.XDMConstants.xdm_schema_name;
import static com.bagri.xdm.common.XDMConstants.xdm_schema_store_data_path;
import static com.bagri.xdm.domain.Document.dvFirst;

import java.io.IOException;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.bagri.common.util.FileUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.PopulationManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.system.DataFormat;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class FileDocumentCacheStore implements MapStore<XDMDocumentKey, Document>, MapLoaderLifecycleSupport {

    private static final Logger logger = LoggerFactory.getLogger(FileDocumentCacheStore.class);
    
    private Map<XDMDocumentKey, String> uris = new HashMap<>();
    
	private String dataPath;
    private String schemaName;
    private String dataFormat;
    private RepositoryImpl xdmRepo;
    private PopulationManagementImpl popManager;
    
	@Override
	public void init(HazelcastInstance hzInstance, Properties properties, String mapName) {
		logger.info("init.enter; properties: {}", properties);
		popManager = (PopulationManagementImpl) hzInstance.getUserContext().get("popManager");
		dataPath = properties.getProperty(xdm_schema_store_data_path);
		schemaName = (String) properties.get(xdm_schema_name);
	}

	@Override
	public void destroy() {
		// do nothing
	}
	
	private synchronized void ensureRepository() {
		if (xdmRepo == null) {
			ApplicationContext schemaCtx = (ApplicationContext) getContext(schemaName, schema_context);
			if (schemaCtx != null) {
				xdmRepo = schemaCtx.getBean(RepositoryImpl.class);
				dataFormat = xdmRepo.getSchema().getProperty(xdm_schema_format_default);
				
				while (!xdmRepo.isInitialized()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						// ??
						logger.warn("ensureRepository; interrupted while waiting for Repo initialization"); 
					}
				}
			} else {
				logger.warn("ensureRepository; can not get context for schema: {}", schemaName);
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

	private Document loadDocument(XDMDocumentKey docKey) {
    	String docUri = null;
    	Document doc = popManager.getDocument(docKey.getKey());
    	if (doc != null) {
    		if (doc.getTxFinish() > TX_NO) {
    			// no need to load content for inactive docs
    			return doc;
    		}
        	docUri = doc.getUri();
    	} else {
    		docUri = uris.get(docKey);
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
				} catch (IOException | XDMException ex) {
					logger.error("loadDocument.error", ex);
					// TODO: notify popManager about this?!
				}
	    	}
		}
    	return null;
    }
    
	@Override
	public Document load(XDMDocumentKey key) {
		logger.trace("load.enter; key: {}", key);
		ensureRepository();
    	Document result = loadDocument(key);
		logger.trace("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Map<XDMDocumentKey, Document> loadAll(Collection<XDMDocumentKey> keys) {
		logger.debug("loadAll.enter; keys: {}; ", keys.size());
		ensureRepository();
		Map<XDMDocumentKey, Document> result = new HashMap<>(keys.size());
	    for (XDMDocumentKey key: keys) {
	    	Document doc = loadDocument(key);
	    	if (doc != null) {
	    		result.put(key, doc);
	    	}
	    }
		logger.debug("loadAll.exit; returning: {} documents for keys: {}", result.size(), keys.size());
		return result;
	}

	@Override
	public Set<XDMDocumentKey> loadAllKeys() {
		//if (true) {
		//	return Collections.emptySet();
		//}
		
		ensureRepository();
		if (xdmRepo == null) {
			logger.trace("loadAllKeys.enter; store is not ready yet, skipping population");
			return null;
		}
		
		logger.trace("loadAllKeys.enter;");
		Set<XDMDocumentKey> docIds = popManager.getDocumentKeys();
		if (docIds != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("loadAllKeys.exit; returning from PopulationManager: {}", docIds);
			} else {
				logger.info("loadAllKeys.exit; returning keys from PopulationManager: {}", docIds.size());
			}
			return docIds;
		}
	    
	    Path root = Paths.get(dataPath);
		try {
			uris.clear();
			List<Path> files = new ArrayList<>();
			processPathFiles(root, files);
			Collections.sort(files);
			XDMDocumentKey docKey; 
			for (Path path: files) {
				String uri = path.getFileName().toString();
				int revision = 0;
				do {
					docKey = xdmRepo.getFactory().newXDMDocumentKey(uri, revision, dvFirst);
					revision++;
				} while (uris.get(docKey) != null);				
				uris.put(docKey, uri);
			}
			docIds = new HashSet<XDMDocumentKey>(uris.keySet());
		} catch (IOException ex) {
			logger.error("loadAllKeys.error;", ex);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("loadAllKeys.exit; returning: {}; docKeys: {}", docIds, uris);
		} else {
			logger.info("loadAllKeys.exit; returning keys: {}", docIds.size());
		}
		return docIds;
	}
	
	@Override
	public void store(XDMDocumentKey key, Document value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		ensureRepository();
		if (xdmRepo == null) {
			logger.trace("store; not ready yet, skipping store");
			return;
		}
		
		String docUri = uris.get(key);
		if (docUri == null) {
			// create a new document
			//logger.trace("store; got path: {}; uri: {}", path, uri);
			docUri = value.getUri();
			uris.put(key, docUri);
		} else {
			// update existing document - put a new version
		}
		
		String fullUri = getFullUri(docUri);
		try {
			DocumentManagementImpl docManager = (DocumentManagementImpl) xdmRepo.getDocumentManagement(); 
			String xml = docManager.getDocumentAsString(key);
			FileUtils.writeTextFile(fullUri, xml);
			logger.trace("store.exit; stored as: {}; length: {}", fullUri, xml.length());
		} catch (IOException | XDMException ex) {
			logger.error("store.error; exception on store document: " + ex.getMessage(), ex);
			// rethrow it ?
		}
	}

	@Override
	public void storeAll(Map<XDMDocumentKey, Document> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
		for (Map.Entry<XDMDocumentKey, Document> entry: entries.entrySet()) {
			store(entry.getKey(), entry.getValue());
		}
		logger.trace("storeAll.exit; stored: {}", entries.size());
	}

	@Override
	public void delete(XDMDocumentKey key) {
		logger.trace("delete.enter; key: {}", key);
    	boolean result = false;
		String docUri = uris.get(key);
		if (docUri != null) {
			docUri = getFullUri(docUri);
	    	Path path = Paths.get(docUri);
			try {
				result = Files.deleteIfExists(path);
			} catch (IOException ex) {
				logger.error("delete.error; path: " + path, ex);
			}
		}
    	logger.trace("delete.exit; deleted: {}", result);
	}

	@Override
	public void deleteAll(Collection<XDMDocumentKey> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
		int deleted = 0;
		for (XDMDocumentKey key: keys) {
			String docUri = uris.get(key);
			if (docUri != null) {
				docUri = getFullUri(docUri);
		    	Path path = Paths.get(docUri);
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
