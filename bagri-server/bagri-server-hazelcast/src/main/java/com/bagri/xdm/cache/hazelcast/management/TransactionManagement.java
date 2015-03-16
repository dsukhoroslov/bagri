package com.bagri.xdm.cache.hazelcast.management;

import javax.management.openmbean.CompositeData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.cache.hazelcast.task.stats.StatisticTotalsCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticsReseter;

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
	public CompositeData getTxStatistics() {
		return super.getTotalsStatistics(new StatisticTotalsCollector(schemaName, "txManager"));
	}
	
	@ManagedOperation(description="Reset TransactionManagement statistics")
	public void resetStatistics() {
		super.resetStatistics(new StatisticsReseter(schemaName, "txManager")); 
	}

	
}
