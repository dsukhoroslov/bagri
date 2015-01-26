package com.bagri.xdm.cache.hazelcast.management;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.IMap;

@ManagedResource(description="Schema Indexes Management MBean")
public class IndexManagement extends SchemaFeatureManagement {
	
	private IMap<String, XDMSchema> schemaCache;

	public IndexManagement(String schemaName) {
		super(schemaName);
	}

	protected String getFeatureKind() {
		return "IndexManagement";
	}
	
	public void setSchemaCache(IMap<String, XDMSchema> schemaCache) {
		this.schemaCache = schemaCache;
	}

	@ManagedAttribute(description="Return indexes defined on Schema")
	public TabularData getIndexes() {
		// get XDMSchema somehow! then get its indexes and build TabularData
		XDMSchema schema = schemaCache.get(schemaName);
		logger.debug("getIndexes; got indexes: {}", schema.getIndexes());
		return null;
	}
	
	@ManagedAttribute(description="Return aggregated index usage statistics, per index")
	public TabularData getIndexStatistics() {
		return null;
	}

	@ManagedAttribute(description="Return candidate index usage statistics, per query path")
	public TabularData getIndexCandidateStatistics() {
		return null;
	}
	
	@ManagedOperation(description="Creates a new Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "path", description = "XPath to index")})
	public void addIndex(String path) {
		// not implemented yet
	}
	
	@ManagedOperation(description="Removes an existing Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "path", description = "XPath to delete from indexes")})
	public void dropIndex(String path) {
		// not implemented yet
	}

	@ManagedOperation(description="Rebuilds an existing Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "path", description = "XPath to rebuild")})
	public void rebuildIndex(String path) {
		// not implemented yet
	}

}
