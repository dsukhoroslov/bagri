package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaCleaner;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaStatsAggregator;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticsReseter;
import com.bagri.xdm.client.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.domain.XDMDocument;
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
	
	@ManagedOperation(description="Returns Document XML")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docId", description = "Internal Document identifier")})
	public String getDocumentXML(long docId) {
		//
		return docManager.getDocumentAsString(docId);
	}

	@ManagedOperation(description="delete all Schema documents")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "evictOnly", description = "Delete from Cache or from Cache and CacheStore too")})
	public boolean clear(boolean evictOnly) {
		
		SchemaCleaner task = new SchemaCleaner(schemaName, evictOnly);
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
	public int registerDocument(String docFile) {
		
		String uri = "file:///" + docFile;
		try {
			String xml = FileUtils.readTextFile(docFile);
			XDMDocument doc = docManager.storeDocumentFromString(0, uri, xml);
			return 1;
		} catch (IOException ex) {
			logger.error("registerDocument.error: " + ex.getMessage(), ex);
		}
		return 0;
	}
	
	@ManagedOperation(description="Updates already registerd Document")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docId", description = "Registered Document identifier"),
		@ManagedOperationParameter(name = "uri", description = "A new uri for the file or null"),
		@ManagedOperationParameter(name = "docFile", description = "A full path to XML file to register")})
	public void updateDocument(int docId, String uri, String docFile) {
		// TODO: implement it!
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
		return super.getInvocationStatistics(new StatisticSeriesCollector(schemaName, "docStats"));
	}
	
	@ManagedOperation(description="Reset DocumentManagement invocation statistics")
	public void resetStatistics() {
		super.resetStatistics(new StatisticsReseter(schemaName, "docStats")); 
	}

	@Override
	protected String getFeatureKind() {
		return "DocumentManagement";
	}
}
