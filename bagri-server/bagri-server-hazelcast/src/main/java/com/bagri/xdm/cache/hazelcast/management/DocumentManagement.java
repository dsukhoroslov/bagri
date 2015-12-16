package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.manage.StatsAggregator;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentStructureProvider;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaDocCleaner;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaStatsAggregator;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticsReseter;
import com.bagri.xdm.client.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.common.XDMEntity;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.system.XDMCollection;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.Member;

@ManagedResource(description="Schema Documents Management MBean")
public class DocumentManagement extends SchemaFeatureManagement {

	private DocumentManagementImpl docManager;
    
    public DocumentManagement(String schemaName) {
    	super(schemaName);
    }

	public void setDocumentManager(DocumentManagementImpl docManager) {
		this.docManager = docManager;
	}
	
	@ManagedAttribute(description="Returns Schema size in documents")
	public Integer getDocumentCount() {
		return docManager.getXddSize(); 
	}
    
	@ManagedAttribute(description="Returns Schema size in documents, per document type")
	public CompositeData getTypedDocumentCount() {
		Map<Integer, Integer> counts = null; //((HazelcastDocumentServer) docManager).getTypeDocuments();
		return null;
	}
    
	@ManagedAttribute(description="Returns Schema size in elements")
	public Integer getElementCount() {
		return docManager.getXdmSize(); 
	}
    
	@ManagedAttribute(description="Returns Schema size in elements, per document type")
	public CompositeData getTypedElementCount() {
		Map<Integer, Integer> counts = null; //((HazelcastDocumentServer) docManager).getTypeElements();
		return null;
	}
    
	@ManagedAttribute(description="Returns Schema size in bytes")
	public Long getSchemaSize() {
		//return docManager.getSchemaSize(); 
	
		long stamp = System.currentTimeMillis();
		logger.trace("getSchemaSize.enter;");
		
		SchemaStatsAggregator task = new SchemaStatsAggregator();
		Map<Member, Future<Long>> results = execService.submitToAllMembers(task);
		long fullSize = 0;
		for (Map.Entry<Member, Future<Long>> entry: results.entrySet()) {
			try {
				Long size = entry.getValue().get();
				fullSize += size;
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getSchemaSize.error; ", ex);
			}
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getSchemaSize.exit; returning: {}; timeTaken: {}", fullSize, stamp);
    	return fullSize;
	}
    
	@ManagedAttribute(description="Returns Schema size in bytes, per document type")
	public CompositeData getTypedSchemaSize() {
		Map<Integer, Long> counts = null; //((HazelcastDocumentServer) docManager).getTypeSchemaSize();
		return null;
	}

	@ManagedOperation(description="Return Document Elements")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docId", description = "Internal Document identifier")})
	public CompositeData getDocumentElements(long docId) {
		//
		//docManager.
		DocumentStructureProvider task = new DocumentStructureProvider(null, docId); //??
		Future<CompositeData> result = execService.submitToKeyOwner(task, docId);
		try {
			return result.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentElements.error; ", ex);
			return null; //new String[] {"Error: " + ex.getMessage()}; 
		}
	}

	@ManagedOperation(description="Return Document Fields")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docId", description = "Internal Document identifier")})
	public CompositeData getDocumentInfo(long docId) {
		try {
			XDMDocument doc = docManager.getDocument(docId);
	        Map<String, Object> docInfo = doc.convert();
	        return JMXUtils.mapToComposite("document", "Document Info", docInfo);
		} catch (XDMException ex) {
			logger.error("getDocumentInfo.error: " + ex.getMessage(), ex);
		}
		return null;
	}
	
	@ManagedOperation(description="Return Document XML")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docId", description = "Internal Document identifier")})
	public String getDocumentXML(long docId) {
		try {
			return docManager.getDocumentAsString(docId);
		} catch (XDMException ex) {
			logger.error("getDocumentXML.error: " + ex.getMessage(), ex);
		}
		return null;
	}

	@Override
	protected Collection getSchemaFeatures(XDMSchema schema) {
		return schema.getCollections();
	}

	@ManagedAttribute(description="Return Collections registered in the Schema")
	public TabularData getCollections() {
		return getTabularFeatures("collection", "Collection definition", "name");
	}
	
	@ManagedOperation(description="Creates a new Collection")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Collection name to create"),
		@ManagedOperationParameter(name = "docType", description = "Root path for document type, if needed"),
		@ManagedOperationParameter(name = "description", description = "Collection description")})
	public void addCollection(String name, String docType, String description) {

		logger.trace("addCollection.enter;");
		long stamp = System.currentTimeMillis();
		XDMCollection collect = schemaManager.addCollection(name, docType, description);
		if (collect == null) {
			throw new IllegalStateException("Collection '" + name + "' in schema '" + schemaName + "' already exists");
		}
		
		int cnt = 0;
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("addCollection.exit; collection added on {} members; timeTaken: {}", cnt, stamp);
	}
	
	@ManagedOperation(description="Removes an existing Collection")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Collection name to remove")})
	public void removeCollection(String name) {
		
		logger.trace("removeCollection.enter;");
		long stamp = System.currentTimeMillis();
		if (!schemaManager.deleteCollection(name)) {
			throw new IllegalStateException("Collection '" + name + "' in schema '" + schemaName + "' does not exist");
		}

		int cnt = 0;
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("removeCollection.exit; collection deleted on {} members; timeTaken: {}", cnt, stamp);
	}

	@ManagedOperation(description="Enables/Disables an existing Collection")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Collection name to enable/disable"),
		@ManagedOperationParameter(name = "enable", description = "enable/disable collection")})
	public void enableCollection(String name, boolean enable) {
		
		if (!schemaManager.enableCollection(name, enable)) {
			throw new IllegalStateException("Collection '" + name + "' in schema '" + schemaName + 
					"' does not exist or already " + (enable ? "enabled" : "disabled"));
		}
	}
	
	@ManagedOperation(description="delete all Schema documents")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "evictOnly", description = "Delete from Cache or from Cache and CacheStore too")})
	public boolean clear(boolean evictOnly) {
		
		SchemaDocCleaner task = new SchemaDocCleaner(schemaName, evictOnly);
		Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		boolean result = true;
		for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
			try {
				if (!entry.getValue().get()) {
					result = false;
				}
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("clear.error; ", ex);
			}
		}
		return result;
	}
	    
	@ManagedOperation(description="Register Document")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docFile", description = "A full path to XML file to register")})
	public long registerDocument(String docFile) {
		
		String uri = "file:///" + docFile;
		try {
			String xml = FileUtils.readTextFile(docFile);
			XDMDocument doc = docManager.storeDocumentFromString(0, uri, xml);
			return doc.getDocumentKey();
		} catch (IOException | XDMException ex) {
			logger.error("registerDocument.error: " + ex.getMessage(), ex);
		}
		return 0;
	}
	
	@ManagedOperation(description="Updates already registerd Document")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docId", description = "Registered Document identifier"),
		@ManagedOperationParameter(name = "uri", description = "A new uri for the file or null"),
		@ManagedOperationParameter(name = "docFile", description = "A full path to XML file to register")})
	public long updateDocument(long docId, String uri, String docFile) {

		try {
			String xml = FileUtils.readTextFile(docFile);
			XDMDocument doc = docManager.storeDocumentFromString(docId, uri, xml);
			return doc.getDocumentKey();
		} catch (IOException | XDMException ex) {
			logger.error("updateDocument.error: " + ex.getMessage(), ex);
		}
		return 0;
	}
	
	@ManagedOperation(description="Remove Document")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docId", description = "Registered Document identifier")})
	public boolean removeDocument(long docId) {
		
		try {
			docManager.removeDocument(docId);
			return true;
		} catch (Exception ex) {
			logger.error("removeDocument.error: " + ex.getMessage(), ex);
		}
		return false;
	}

	private int processFilesInCatalog(Path catalog) {
		int result = 0;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(catalog, "*.xml")) {
		    for (Path path: stream) {
		        if (Files.isDirectory(path)) {
		            result += processFilesInCatalog(path);
		        } else {
		            result += registerDocument(path.toString()); // file.getPath());
		        }
		    }
		} catch (IOException ex) {
			logger.error("processFilesInCatalog.error: " + ex.getMessage(), ex);
		}
	    return result;
	}
	
	@ManagedOperation(description="Register Documents")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "catalog", description = "A full path to the directory containing XML files to register")})
	public int registerDocuments(String path) {
		Path catalog = Paths.get(path);
		return processFilesInCatalog(catalog);	
	}

	@ManagedAttribute(description="Returns aggregated DocumentManagement invocation statistics, per method")
	public TabularData getInvocationStatistics() {
		return super.getSeriesStatistics(new StatisticSeriesCollector(schemaName, "docStats"), aggregator);
	}
	
	@ManagedOperation(description="Reset DocumentManagement invocation statistics")
	public void resetStatistics() {
		super.resetStatistics(new StatisticsReseter(schemaName, "docStats")); 
	}
	
	@ManagedOperation(description="Add Document to Collection")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docId", description = "Document identifier"),
		@ManagedOperationParameter(name = "collectId", description = "Collection identifier")})
	public boolean addDocumentToCollection(long docId, int collectId) {
		docManager.addDocumentsToCollections(new long[] {docId}, new int[] {collectId});
		return true;
	}

	@ManagedOperation(description="Remove Document from Collection")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docId", description = "Document identifier"),
		@ManagedOperationParameter(name = "collectId", description = "Collection identifier")})
	public boolean removeDocumentFromCollection(long docId, int collectId) {
		docManager.removeDocumentsFromCollections(new long[] {docId}, new int[] {collectId});
		return true;
	}

	@Override
	protected String getFeatureKind() {
		return "DocumentManagement";
	}
}
