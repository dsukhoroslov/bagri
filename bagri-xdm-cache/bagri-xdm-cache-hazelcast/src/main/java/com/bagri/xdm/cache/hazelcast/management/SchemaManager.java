package com.bagri.xdm.cache.hazelcast.management;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.XDMDocument;
import com.bagri.xdm.XDMDocumentType;
import com.bagri.xdm.XDMSchema;
import com.bagri.xdm.access.api.XDMSchemaDictionaryBase;
import com.bagri.xdm.access.api.XDMSchemaManagerBase;
import com.bagri.xdm.process.hazelcast.HazelcastDocumentServer;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class SchemaManager extends XDMSchemaManagerBase implements InitializingBean, DisposableBean, SchemaManagerMBean {

    private static final transient Logger logger = LoggerFactory.getLogger(SchemaManager.class);
    private static final String type_schema = "Schema";
	
    private HazelcastInstance hzInstance;
    private IMap<String, XDMSchema> schemaCache;
    
	public SchemaManager() {
		super();
	}

	public SchemaManager(HazelcastInstance hzInstance) {
		super();
		this.hzInstance = hzInstance;
	}
	
	public void setSchemaCache(IMap<String, XDMSchema> schemaCache) {
		this.schemaCache = schemaCache;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		JMXUtils.registerMBean(type_schema, schemaName, this);
	}

	@Override
	public void destroy() throws Exception {
		JMXUtils.unregisterMBean(type_schema, schemaName);
	}

	@Override
	public String[] getRegisteredTypes() {
		Collection<XDMDocumentType> types = ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes();
		String[] result = new String[types.size()];
		Iterator<XDMDocumentType> itr = types.iterator();
		for (int i=0; i < types.size(); i++) {
			result[i] = itr.next().getRootPath();
		}
		Arrays.sort(result);
		return result;
	}
	
	@Override
	protected XDMSchema getSchema() {
		XDMSchema schema = schemaCache.get(schemaName);
		logger.trace("getSchema. returning: {}", schema);
		return schema;
	}

	@Override
	protected void flushSchema(XDMSchema schema) {
		schemaCache.put(schemaName, schema);
	}
	
	@Override
	public int registerSchema(String schemaFile) {
		int size = ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes().size(); 
		schemaDictionary.registerSchemaUri(schemaFile);
		return ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes().size() - size;
	}
	
	@Override
	public int registerSchemas(String schemasCatalog) {
		int size = ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes().size(); 
		((XDMSchemaDictionaryBase) schemaDictionary).registerSchemas(schemasCatalog);
		return ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes().size() - size;
	}
	
	@Override
	public int registerDocument(String docFile) {
		
		String uri = "file:///" + docFile;
		//logger.trace("storeDocument; document initialized: {}", docId);
		//DocumentCreator task = new DocumentCreator(docId, uri, xml);

		try {
			String xml = FileUtils.readTextFile(docFile);
			XDMDocument doc = ((HazelcastDocumentServer) docManager).createDocument(uri, xml);
			return 1;
		} catch (IOException ex) {
			logger.error("registerDocument.error: " + ex.getMessage(), ex);
		}
		return 0;
	}
	
	private int processFilesInCatalog(File catalog) {
		int result = 0;
	    for (File file: catalog.listFiles()) {
	        if (file.isDirectory()) {
	            result += processFilesInCatalog(file);
	        } else {
	            result += registerDocument(file.getPath());
	        }
	    }
	    return result;
	}

	@Override
	public int registerDocuments(String docCatalog) {
		File catalog = new File(docCatalog);
		return processFilesInCatalog(catalog);	
	}

}
