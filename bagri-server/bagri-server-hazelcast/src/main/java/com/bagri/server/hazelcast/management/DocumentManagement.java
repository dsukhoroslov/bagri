package com.bagri.server.hazelcast.management;

import static com.bagri.core.Constants.pn_document_headers;
import static com.bagri.support.util.PropUtils.propsFromString;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Schema;
import com.bagri.server.hazelcast.task.doc.DocumentStructureProvider;
import com.bagri.server.hazelcast.task.schema.SchemaDocCleaner;
import com.bagri.server.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.server.hazelcast.task.stats.StatisticTotalsCollector;
import com.bagri.server.hazelcast.task.stats.StatisticsReseter;
import com.bagri.support.stats.StatsAggregator;
import com.bagri.support.util.FileUtils;
import com.bagri.support.util.JMXUtils;
import com.hazelcast.core.Member;
import com.hazelcast.core.Partition;

@ManagedResource(description="Schema Documents Management MBean")
public class DocumentManagement extends SchemaFeatureManagement {

	private CompositeData docTotals = null;
	private com.bagri.core.api.DocumentManagement docManager;
    
    public DocumentManagement(String schemaName) {
    	super(schemaName);
    }

	@Override
	protected String getFeatureKind() {
		return "DocumentManagement";
	}

    @Override
	public void setSchemaManager(SchemaManager schemaManager) {
    	super.setSchemaManager(schemaManager);
		this.docManager = schemaManager.getRepository().getDocumentManagement();
    }

	private void initAggregator() {
		if (aggregator == null) {
			aggregator = new StatsAggregator() {

				@Override
				public Object[] aggregateStats(Object[] source, Object[] target) {
					target[2] = source[2]; // collection
					target[3] = (Long) source[3] + (Long) target[3]; // size
					target[4] = (Integer) source[4] + (Integer) target[4]; // number of docs  
					target[5] = (Integer) source[5] + (Integer) target[5]; // number of elts
					target[6] = (Integer) source[6] + (Integer) target[6]; // number of fragments
					double size = (Long) target[3]; 
					target[0] = size/(Integer) target[4]; // avg in bytes
					size = (Integer) target[5]; 
					target[1] = size/(Integer) target[4]; // avg in elts  
					return target;
				}
				
			};
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
				//throw new RuntimeException(ex.getMessage());
			}
		}
		return result;
	}
	    
	@ManagedAttribute(description="Returns Schema Totals")
	public CompositeData getTotalCounts() {
		collectTotals();
		return docTotals;
	}
	
	private void collectTotals() {
		initAggregator();
		docTotals = super.getTotalsStatistics(new StatisticTotalsCollector(schemaName, "docStats"), aggregator);
	}
	
	@ManagedAttribute(description="Returns Schema size in documents")
	public Integer getDocumentCount() {
		collectTotals();
		return (Integer) docTotals.get("Number of documents"); 
	}
    
	@ManagedAttribute(description="Returns Schema size in elements")
	public Integer getElementCount() {
		if (docTotals == null) {
			collectTotals();
		}
		return (Integer) docTotals.get("Number of elements"); 
	}
    
	@ManagedAttribute(description="Returns Schema size in bytes")
	public Long getSchemaSize() {
		if (docTotals == null) {
			collectTotals();
		}
		return (Long) docTotals.get("Consumed size"); 
	}
    
	@ManagedOperation(description="Return Document Elements")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "uri", description = "Document identifier")})
	public CompositeData getDocumentElements(String uri) {
		//
		//docManager.
		DocumentStructureProvider task = new DocumentStructureProvider(null, uri, schemaManager.getRepository().getDistributionStrategy()); 
		Future<CompositeData> result = execService.submitToKeyOwner(task, uri); 
		try {
			return result.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentElements.error; ", ex);
			throw new RuntimeException(ex.getMessage());
		}
	}

	@ManagedOperation(description="Return Document Fields")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "uri", description = "Document identifier")})
	public CompositeData getDocumentInfo(String uri) {
		try {
			Properties props = new Properties(); 
			props.setProperty("bdb.document.headers", String.valueOf(DocumentAccessor.HDR_DOCUMENT_INFO));
			DocumentAccessor doc = docManager.getDocument(uri, props);
			CompositeData result = null;
			if (doc != null) {
				Map<String, Object> docInfo = new HashMap<>(11); 
				docInfo.put("key", doc.getDocumentKey());
				docInfo.put("version", doc.getVersion());
				docInfo.put("uri", doc.getUri());
				docInfo.put("size", doc.getSizeInBytes());
				docInfo.put("encoding", doc.getEncoding());
				docInfo.put("created at", new Date(doc.getCreateadAt()));
				docInfo.put("created by", doc.getCreatedBy());
				docInfo.put("start tx", doc.getTxStart());
				docInfo.put("elements", doc.getSizeInElements());
				docInfo.put("fragments", doc.getSizeInFragments());
				docInfo.put("collections", doc.getCollections());
				docInfo.put("content type", doc.getContentType());
				result = JMXUtils.mapToComposite("document", "Document Info", docInfo);
			} 
			logger.debug("getDocumentInfo; returning: {}", result);
			return result;
		} catch (BagriException ex) {
			logger.error("getDocumentInfo.error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	@ManagedOperation(description="Return Document Location Info")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "uri", description = "Document identifier")})
	public CompositeData getDocumentLocation(String uri) {
		try {
			Properties props = new Properties(); 
			props.setProperty("bdb.document.headers", String.valueOf(DocumentAccessor.HDR_URI));
			DocumentAccessor doc = docManager.getDocument(uri, props);
			CompositeData result = null;
			if (doc != null) {
				int hash = schemaManager.getRepository().getDistributionStrategy().getDistributionHash(uri);
				Partition part = hzClient.getPartitionService().getPartition(hash);
				Map<String, Object> location = new HashMap<>(2);
				location.put("partition", part.getPartitionId());
				location.put("owner", part.getOwner().toString());
				result = JMXUtils.mapToComposite("document", "Document Location", location);
			}
			logger.debug("getDocumentLocation; returning: {}", result);
			return result;
		} catch (BagriException ex) {
			logger.error("getDocumentLocation.error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	@ManagedOperation(description="Return Document Content")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "uri", description = "Document identifier"),
		@ManagedOperationParameter(name = "properties", description = "A list of properties in key=value form separated by semicolon")})
	public String getDocumentContent(String uri, String properties) {
		try {
			String result = null;
			DocumentAccessor doc = docManager.getDocument(uri, propsFromString(properties));
			if (doc != null) {
				result = doc.getContent();
			}
			logger.debug("getDocumentContent; got accessor {} for uri: {}, props: {}; returning: {}", doc, uri, properties, result);
			return result;
		} catch (IOException | BagriException ex) {
			logger.error("getDocumentContent.error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage());
		} catch (Throwable ex) {
			logger.error("getDocumentContent.error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage());
		}
	}

	@ManagedOperation(description="Register Document")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docFile", description = "A full path to XML file to register"),
		@ManagedOperationParameter(name = "properties", description = "A list of properties in key=value form separated by semicolon")})
	public String registerDocument(String docFile, String properties) {
		String uri = Paths.get(docFile).getFileName().toString(); 
		try {
			String xml = FileUtils.readTextFile(docFile);
			DocumentAccessor doc = docManager.storeDocument(uri, xml, propsFromString(properties));
			return doc.getUri();
		} catch (IOException | BagriException ex) {
			logger.error("registerDocument.error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	@ManagedOperation(description="Updates already registerd Document")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "uri", description = "A new uri for the file or null"),
		@ManagedOperationParameter(name = "docFile", description = "A full path to XML file to register"),
		@ManagedOperationParameter(name = "properties", description = "A list of properties in key=value form separated by semicolon")})
	public String updateDocument(String uri, String docFile, String properties) {
		try {
			String content = FileUtils.readTextFile(docFile);
			DocumentAccessor doc = docManager.storeDocument(uri, content, propsFromString(properties));
			return doc.getUri();
		} catch (IOException | BagriException ex) {
			logger.error("updateDocument.error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	@ManagedOperation(description="Remove Document")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "uri", description = "Document identifier"),
		@ManagedOperationParameter(name = "properties", description = "A list of properties in key=value form separated by semicolon")})
	public boolean removeDocument(String uri, String properties) {
		try {
			docManager.removeDocument(uri, propsFromString(properties));
			return true;
		} catch (Exception ex) {
			logger.error("removeDocument.error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage());
		}
	}

	private int processFilesInCatalog(Path catalog, String properties) {
		int result = 0;
		String docType = schemaManager.getDataFormat();
		logger.debug("processFilesInCatalog; got docType: {}", docType); 
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(catalog, "*." + docType.toLowerCase())) {
		    for (Path path: stream) {
		        if (Files.isDirectory(path)) {
		            result += processFilesInCatalog(path, properties);
		        } else {
		            registerDocument(path.toString(), properties);
		            result++;
		        }
		    }
		    return result;
		} catch (IOException ex) {
			logger.error("processFilesInCatalog.error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	@ManagedOperation(description="Register Documents")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "path", description = "A full path to directory containing XML files to register"),
		@ManagedOperationParameter(name = "properties", description = "A list of properties in key=value form separated by semicolon")})
	public int registerDocuments(String path, String properties) {
		Path catalog = Paths.get(path);
		return processFilesInCatalog(catalog, properties);	
	}

	@Override
	protected java.util.Collection getSchemaFeatures(Schema schema) {
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
		@ManagedOperationParameter(name = "uriPattern", description = "URI Pattern for documents, if needed"),
		@ManagedOperationParameter(name = "description", description = "Collection description")})
	public void addCollection(String name, String docType, String uriPattern, String description) {
		logger.trace("addCollection.enter;");
		long stamp = System.currentTimeMillis();
		Collection collect = schemaManager.addCollection(name, docType, uriPattern, description);
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
	
	@ManagedAttribute(description="Returns aggregated DocumentManagement invocation statistics, per method")
	public TabularData getCollectionStatistics() {
		initAggregator();
		return super.getSeriesStatistics(new StatisticSeriesCollector(schemaName, "docStats"), aggregator);
	}
	
	@ManagedOperation(description="Reset DocumentManagement invocation statistics")
	public void resetStatistics() {
		super.resetStatistics(new StatisticsReseter(schemaName, "docStats")); 
	}
	
	@ManagedOperation(description="Add Document to Collection")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "uri", description = "Document identifier"),
		@ManagedOperationParameter(name = "clnName", description = "Collection name")})
	public int addDocumentToCollection(String uri, String clnName) {
		Collection cln = this.schemaManager.getEntity().getCollection(clnName);
		if (cln != null) {
			return docManager.addDocumentToCollections(uri, new String[] {clnName});
		}
		logger.info("addDocumentToCollection; no collection found for name: {}", clnName);
		return 0;
	}

	@ManagedOperation(description="Remove Document from Collection")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "uri", description = "Document identifier"),
		@ManagedOperationParameter(name = "clnName", description = "Collection name")})
	public int removeDocumentFromCollection(String uri, String clnName) {
		Collection cln = schemaManager.getEntity().getCollection(clnName);
		if (cln != null) {
			return docManager.removeDocumentFromCollections(uri, new String[] {clnName});
		}
		logger.info("removeDocumentFromCollection; no collection found for name: {}", clnName);
		return 0;
	}

	@ManagedOperation(description="Return Documents which belongs to Collection")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "clName", description = "Collection name"),
		@ManagedOperationParameter(name = "properties", description = "A list of properties in key=value form separated by semicolon")})
	public java.util.Collection<String> getCollectionDocuments(String clName, String properties) {
		if (clName != null && !"All Documents".equals(clName)) {
			Collection cln = schemaManager.getEntity().getCollection(clName);
			if (cln == null) {
				logger.info("getCollectionDocuments; got unknown collection: {}", clName);
				return null;
			}
		}

		try {
			return getUris("collections.contains(" + clName + "), txFinish = 0", properties);
		} catch (Exception ex) {
			logger.error("getCollectionDocuments.error", ex);
			return null;
		}
	}

	@ManagedOperation(description="Return Documents matching the pattern provided")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "pattern", description = "A pattern to match documents, like: createdBy = admin, bytes > 100"),
		@ManagedOperationParameter(name = "properties", description = "A list of properties in key=value form separated by semicolon")})
	public java.util.Collection<String> getDocumentUris(String pattern, String properties) {
		try {
			return getUris(pattern, properties);
		} catch (Exception ex) {
			logger.error("getDocumentUris.error", ex);
			return null;
		}
	}
	
	private java.util.Collection<String> getUris(String pattern, String properties) throws Exception {
		Properties props = propsFromString(properties);
		if (props == null) {
			props = new Properties();
		}
		props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_URI));
		Iterable<DocumentAccessor> uris = (Iterable<DocumentAccessor>) docManager.getDocuments(pattern, props);
		List<String> result = new ArrayList<>(); //uris.size()
		for (DocumentAccessor doc: uris) {
			result.add(doc.getUri());
		}
		//uris.close();
		logger.debug("getUris; returning {} uris", result);
		return result;
	}

}
