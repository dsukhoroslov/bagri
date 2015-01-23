package com.bagri.xdm.cache.hazelcast.management;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.process.hazelcast.node.NodeKiller;

@ManagedResource(description="Schema Indexes Management MBean")
public class IndexManagement extends SchemaFeatureManagement {

	public IndexManagement(String schemaName) {
		super(schemaName);
	}

	protected String getFeatureKind() {
		return "IndexManagement";
	}

	@ManagedAttribute(description="Return indexes defined on Schema")
	public TabularData getIndexes() {
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
	}
	
	@ManagedOperation(description="Removes an existing Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "path", description = "XPath to delete from indexes")})
	public void dropIndex(String path) {
	}

	@ManagedOperation(description="Rebuilds an existing Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "path", description = "XPath to rebuild")})
	public void rebuildIndex(String path) {
	}

}
