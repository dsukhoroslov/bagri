package com.bagri.xdm.cache.hazelcast.store.xml;

import static com.bagri.common.config.XDMConfigConstants.xdm_schema_store_type;
import static com.bagri.common.config.XDMConfigConstants.xdm_schema_name;
import static com.bagri.common.util.FileUtils.def_encoding;
import static com.bagri.xdm.api.XDMTransactionManagement.TX_INIT;
import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;
import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.getContext;
import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.schema_context;

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
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.cache.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.PopulationManagementImpl;
import com.bagri.xdm.client.common.XDMCacheConstants;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMParser;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMFragmentedDocument;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class DocumentCacheStore extends XmlCacheStore implements MapStore<XDMDocumentKey, XDMDocument>, MapLoaderLifecycleSupport {

    private static final Logger logger = LoggerFactory.getLogger(DocumentCacheStore.class);
    
    private HazelcastInstance hzInstance;
    private Map<XDMDocumentKey, String> uris = new HashMap<>();
    
    private String schemaName;
	//private String dataPath;
    private String dataFormat = XDMParser.df_xml;
    private DocumentManagementImpl docMgr;
    private XDMModelManagement schemaDict;
    private PopulationManagementImpl popManager;
    // TODO: work with xmlCache via docMgr!
    private IMap<XDMDocumentKey, String> xmlCache;
    
	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; properties: {}", properties);
		hzInstance = hazelcastInstance;
		popManager = (PopulationManagementImpl) hzInstance.getUserContext().get("popManager");
		schemaName = (String) properties.get(xdm_schema_name);
		xmlCache = hzInstance.getMap(XDMCacheConstants.CN_XDM_CONTENT);
		String df = properties.getProperty(xdm_schema_store_type);
		if (df != null) {
			dataFormat = df;
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}
	
	private synchronized void ensureDocumentManager() {
		if (docMgr == null) {
			ApplicationContext schemaCtx = (ApplicationContext) getContext(schemaName, schema_context);
			if (schemaCtx != null) {
				docMgr = schemaCtx.getBean(DocumentManagementImpl.class);
				schemaDict = schemaCtx.getBean(XDMModelManagement.class);
			} else {
				logger.warn("ensureDocumentManager; can not get context for schema: {}", schemaName);
			}
			logger.info("ensureDocumentManager; mgr: {}", docMgr);
		}
	}
    
	private void processPathFiles(Path root, List<Path> files) throws IOException {
		String ext = "*." + dataFormat.toLowerCase();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, ext)) {
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
		return getDataPath() + "/" + fileName;
	}

    @SuppressWarnings("unchecked")
	private XDMDocument loadDocument(XDMDocumentKey docKey) {
    	String docUri = null;
    	XDMDocument doc = popManager.getDocument(docKey.getKey());
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
    	    		XDMParser parser = docMgr.getXdmFactory().newXDMParser(dataFormat, schemaDict);
        			//List<XDMData> data = parser.parse(new File(ddh.uri));
        			List<XDMData> data = parser.parse(content);         		

        			Object[] ids = docMgr.loadElements(docKey.getKey(), data);
        			List<Long> fragments = (List<Long>) ids[0];  
        			if (fragments == null) {
        				logger.warn("loadDocument.exit; the document is not valid as it has no root element");
        				//throw new XDMException("invalid document", XDMException.ecDocument);
        			} else {
	        			int docType = fragments.get(0).intValue();
						xmlCache.set(docKey, content);
						// can make a fake population TX with id = 1! 
	        			if (fragments.size() == 1) {
	        				if (doc == null) {
	        					doc = new XDMDocument(docKey.getDocumentId(), docKey.getVersion(), docUri, docType, TX_INIT, TX_NO,
	        							new Date(Files.getLastModifiedTime(path).toMillis()), Files.getOwner(path).getName(), def_encoding,
	        							content.length(), data.size());
	    	        			docMgr.checkDefaultDocumentCollection(doc);
	        				}
	        			} else {
	        				if (doc == null) {
	        					doc = new XDMFragmentedDocument(docKey.getDocumentId(), docKey.getVersion(), docUri, docType, TX_INIT, TX_NO,
	        							new Date(Files.getLastModifiedTime(path).toMillis()), Files.getOwner(path).getName(), def_encoding,
	        							content.length(), data.size());
	    	        			docMgr.checkDefaultDocumentCollection(doc);
	        				} else {
	        					XDMDocument fdoc = new XDMFragmentedDocument(docKey.getDocumentId(), docKey.getVersion(), doc.getUri(), doc.getTypeId(), 
		        						doc.getTxStart(), doc.getTxFinish(), doc.getCreatedAt(), doc.getCreatedBy(), doc.getEncoding(), doc.getBytes(),
		        						doc.getElements());
	        					fdoc.setCollections(doc.getCollections());
	        					doc = fdoc;
	        				}
	        				long[] fa = new long[fragments.size()];
	        				fa[0] = docKey.getKey();
	        				for (int i=1; i < fragments.size(); i++) {
	        					fa[i] = fragments.get(i);
	        				}
	        				((XDMFragmentedDocument) doc).setFragments(fa);
	        			}
	        			//Set<Integer> paths = (Set<Integer>) ids[1];
	        			docMgr.updateDocumentStats(doc, doc.getCollections(), true, data.size());
	        			//int cnt = docMgr.updateDocumentStats(doc, doc.getCollections(), true, paths.size());
	        			return doc;
        			}
				} catch (IOException | XDMException ex) {
					logger.error("loadDocument.error", ex);
					// TODO: notify popManager about this?!
				}
	    	}
		}
    	return null;
    }
    
	@Override
	public XDMDocument load(XDMDocumentKey key) {
		logger.trace("load.enter; key: {}", key);
		ensureDocumentManager();
    	XDMDocument result = loadDocument(key);
		logger.trace("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Map<XDMDocumentKey, XDMDocument> loadAll(Collection<XDMDocumentKey> keys) {
		logger.debug("loadAll.enter; keys: {}; Cluster size: {}", keys.size(), hzInstance.getCluster().getMembers().size());
		ensureDocumentManager();
		Map<XDMDocumentKey, XDMDocument> result = new HashMap<>(keys.size());
	    for (XDMDocumentKey key: keys) {
	    	XDMDocument doc = loadDocument(key);
	    	if (doc != null) {
	    		result.put(key, doc);
	    	}
	    }
		logger.debug("loadAll.exit; returning: {} documents for keys: {}", result.size(), keys.size());
		return result;
	}

	@Override
	public Set<XDMDocumentKey> loadAllKeys() {
		ensureDocumentManager();
		if (docMgr == null) {
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
	    
	    Path root = Paths.get(getDataPath());
		try {
			uris.clear();
			List<Path> files = new ArrayList<>();
			processPathFiles(root, files);
			Collections.sort(files);
			XDMDocumentKey docKey; 
			for (Path path: files) {
				docKey = docMgr.nextDocumentKey();
				uris.put(docKey, path.getFileName().toString());
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
	public void store(XDMDocumentKey key, XDMDocument value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		ensureDocumentManager();
		if (docMgr == null) {
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
		
		docUri = getFullUri(docUri);
		try {
			String xml = docMgr.getDocumentAsString(new XDMDocumentId(key.getKey()));
			FileUtils.writeTextFile(docUri, xml);
			logger.trace("store.exit; stored as: {}; length: {}", docUri, xml.length());
		} catch (IOException | XDMException ex) {
			logger.error("store.error; exception on store document: " + ex.getMessage(), ex);
			// rethrow it ?
		}
	}

	@Override
	public void storeAll(Map<XDMDocumentKey, XDMDocument> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
		for (Map.Entry<XDMDocumentKey, XDMDocument> entry: entries.entrySet()) {
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
