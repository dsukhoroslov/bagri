package com.bagri.xdm.cache.hazelcast.management;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
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
import com.bagri.xdm.cache.hazelcast.task.index.IndexCreator;
import com.bagri.xdm.cache.hazelcast.task.index.IndexRemover;
import com.bagri.xdm.cache.hazelcast.task.index.IndexStatsCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.domain.XDMIndexedValue;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.Member;

@ManagedResource(description="Schema Indexes Management MBean")
public class IndexManagement extends SchemaFeatureManagement {
	
	private StatsAggregator isAggregator = new IndexStatsAggregator();
	
	public IndexManagement(String schemaName) {
		super(schemaName);
	}

	protected String getFeatureKind() {
		return "IndexManagement";
	}
	
    public void setStatsAggregator(StatsAggregator aggregator) {
    	this.aggregator = aggregator;
    }
	
	@Override
	protected Collection getSchemaFeatures(XDMSchema schema) {
		return schema.getIndexes();
	}

	@ManagedAttribute(description="Return indexes defined on Schema")
	public TabularData getIndexes() {
		return getTabularFeatures("index", "Index definition", "name");
    }

	@ManagedAttribute(description="Return aggregated index statistics, per index")
	public TabularData getIndexStatistics() {
		return super.getUsageStatistics(new IndexStatsCollector(), isAggregator);
	}
	
	@ManagedAttribute(description="Return aggregated index usage statistics, per index")
	public TabularData getUsageStatistics() {
		return super.getUsageStatistics(new StatisticSeriesCollector(schemaName, "indexStats"), aggregator);
	}

	@ManagedOperation(description="Creates a new Index")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Index name to create"),
		@ManagedOperationParameter(name = "docType", description = "Root path for document type"),
		@ManagedOperationParameter(name = "path", description = "XPath to index"),
		@ManagedOperationParameter(name = "dataType", description = "Indexed value data type"),
		@ManagedOperationParameter(name = "caseSensitive", description = "Is index case-sensitive"),
		@ManagedOperationParameter(name = "range", description = "Is index supports range search"),
		@ManagedOperationParameter(name = "unique", description = "Is index unique"),
		@ManagedOperationParameter(name = "description", description = "Index description")})
	public void addIndex(String name, String docType, String path, String dataType, boolean caseSensitive,
			boolean range, boolean unique, String description) {

		logger.trace("addIndex.enter;");
		long stamp = System.currentTimeMillis();
		XDMIndex index = schemaManager.addIndex(name, docType, path, dataType, caseSensitive, range, unique, description);
		if (index == null) {
			throw new IllegalStateException("Index '" + name + "' in schema '" + schemaName + "' already exists");
		}
		
		IndexCreator task = new IndexCreator(index);
		Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		int cnt = 0;
		for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
			try {
				if (entry.getValue().get()) {
					cnt++;
				}
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("addIndex.error; ", ex);
			}
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("addIndex.exit; index created on {} members; timeTaken: {}", cnt, stamp);
	}
	
	@ManagedOperation(description="Removes an existing Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Index name to delete")})
	public void dropIndex(String name) {
		
		logger.trace("dropIndex.enter;");
		long stamp = System.currentTimeMillis();
		if (!schemaManager.deleteIndex(name)) {
			throw new IllegalStateException("Index '" + name + "' in schema '" + schemaName + "' does not exist");
		}

		IndexRemover task = new IndexRemover(name);
		Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		int cnt = 0;
		for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
			try {
				if (entry.getValue().get()) {
					cnt++;
				}
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("dropIndex.error; ", ex);
			}
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("dropIndex.exit; index deleted on {} members; timeTaken: {}", cnt, stamp);
	}

	@ManagedOperation(description="Enables/Disables an existing Index")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Index name to enable/disable"),
		@ManagedOperationParameter(name = "enable", description = "enable/disable index")})
	public void enableIndex(String name, boolean enable) {
		
		if (!schemaManager.enableIndex(name, enable)) {
			throw new IllegalStateException("Index '" + name + "' in schema '" + schemaName + 
					"' does not exist or already " + (enable ? "enabled" : "disabled"));
		}
	}
	
	@ManagedOperation(description="Rebuilds an existing Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Index to rebuild")})
	public void rebuildIndex(String name) {
		// not implemented yet
	}
	
	private static class IndexStatsAggregator implements StatsAggregator {

		@Override
		public Object[] aggregateStats(Object[] source, Object[] target) {
			target[0] = (Long) source[0] + (Long) target[0]; // "consumed size"
			target[1] = (Integer) source[1] + (Integer) target[1]; // "distinct values"
			target[2] = source[2]; // "index"
			target[3] = (Integer) source[3] + (Integer) target[3]; // "indexed documents"
			target[4] = source[4]; // "path"
			return target;
		}
		
	}

}
