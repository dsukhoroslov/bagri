package com.bagri.xdm.cache.hazelcast.management;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.StatsAggregator;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticTotalsCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticsReseter;

@ManagedResource(description="Schema Transactions Management MBean")
public class TransactionManagement extends SchemaFeatureManagement {
	
	private StatsAggregator txAggregator;
	
	public TransactionManagement(String schemaName) {
		super(schemaName);
	}

	protected String getFeatureKind() {
		return "TransactionManagement";
	}
	
	@ManagedAttribute(description="Return aggregated transaction statistics")
	public CompositeData getTxStatistics() {
		if (txAggregator == null) {
			txAggregator = new StatsAggregator() {

				@Override
				public Object[] aggregateStats(Object[] source, Object[] target) {
					target[0] = (Long) source[0] + (Long) target[0]; // 
					target[1] = (Long) source[1] + (Long) target[1]; //   
					target[2] = (Long) source[2] + (Long) target[2]; // 
					target[3] = (Long) source[3] + (Long) target[3]; // 
					return target;
				}
				
			};
		}
		
		return super.getTotalsStatistics(new StatisticTotalsCollector(schemaName, "txManager"), txAggregator);
	}
	
	@ManagedAttribute(description="Return in-flight transactions")
	public TabularData getInFlightTransactions() {
		return super.getSeriesStatistics(new StatisticSeriesCollector(schemaName, "txManager"), aggregator);
	}

	@ManagedOperation(description="Reset TransactionManagement statistics")
	public void resetStatistics() {
		super.resetStatistics(new StatisticsReseter(schemaName, "txManager")); 
	}

	
}
