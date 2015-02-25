package com.bagri.xdm.cache.hazelcast.management;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;

@ManagedResource(description="Schema Transactions Management MBean")
public class TransactionManagement extends SchemaFeatureManagement {
	
	//private SchemaManager schemaManager;

	public TransactionManagement(String schemaName) {
		super(schemaName);
	}

	protected String getFeatureKind() {
		return "TransactionManagement";
	}
	
	//public void setSchemaManager(SchemaManager schemaManager) {
	//	this.schemaManager = schemaManager;
	//}

	@ManagedAttribute(description="Return aggregated transaction statistics")
	public TabularData getTxStatistics() {
		return super.getInvocationStatistics(new StatisticSeriesCollector(schemaName, "txStats"));
	}
	
}
