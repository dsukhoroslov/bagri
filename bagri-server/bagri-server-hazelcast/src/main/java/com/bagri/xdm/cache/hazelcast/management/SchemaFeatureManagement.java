package com.bagri.xdm.cache.hazelcast.management;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.xml.xquery.XQConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.manage.StatsAggregator;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticsReseter;
import com.bagri.xdm.common.XDMEntity;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xdm.system.XDMTriggerDef;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public abstract class SchemaFeatureManagement implements SelfNaming {
	
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String schemaName;
	protected IExecutorService execService;
	protected XDMModelManagement modelMgr;
	protected SchemaManager schemaManager;
	protected StatsAggregator aggregator;

    public SchemaFeatureManagement(String schemaName) {
    	this.schemaName = schemaName;
    }

	@ManagedAttribute(description="Returns corresponding Schema name")
	public String getSchema() {
		return schemaName;
	}
	
	public void setExecService(IExecutorService execService) {
		this.execService = execService;
	}
	
	public void setModelManager(XDMModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}

	public void setSchemaManager(SchemaManager schemaManager) {
		this.schemaManager = schemaManager;
	}

    public void setStatsAggregator(StatsAggregator aggregator) {
    	this.aggregator = aggregator;
    }
	
	protected abstract String getFeatureKind();

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return JMXUtils.getObjectName("type=Schema,name=" + schemaName + ",kind=" + getFeatureKind());
	}

	public TabularData getUsageStatistics(Callable<TabularData> statsTask, StatsAggregator aggregator) {
		logger.trace("getUsageStatistics.enter; going to collect stats for schema: {}", schemaName);

		int cnt = 0;
		TabularData result = null;
		Map<Member, Future<TabularData>> futures = execService.submitToAllMembers(statsTask);
		for (Map.Entry<Member, Future<TabularData>> entry: futures.entrySet()) {
			try {
				TabularData stats = entry.getValue().get();
				logger.trace("getUsageStatistics; got stats: {}, from member {}", stats, entry.getKey());
				result = JMXUtils.aggregateStats(stats, result, aggregator);
				logger.trace("getUsageStatistics; got aggregated result: {}", result);
				cnt++;
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getUsageStatistics.error: " + ex.getMessage(), ex);
			}
		}
		logger.trace("getUsageStatistics.exit; got stats from {} nodes", cnt);
		return result;
	}

	protected TabularData getSeriesStatistics(Callable<TabularData> statsTask, StatsAggregator aggregator) {
		logger.trace("getSeriesStatistics.enter; going to collect stats for schema: {}", schemaName);

		int cnt = 0;
		TabularData result = null;
		Map<Member, Future<TabularData>> futures = execService.submitToAllMembers(statsTask);
		for (Map.Entry<Member, Future<TabularData>> entry: futures.entrySet()) {
			try {
				TabularData stats = entry.getValue().get();
				logger.trace("getSeriesStatistics; got stats: {}, from member {}", stats, entry.getKey());
				result = JMXUtils.aggregateStats(stats, result, aggregator);
				logger.trace("getSeriesStatistics; got aggregated result: {}", result);
				cnt++;
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getSeriesStatistics.error: " + ex.getMessage(), ex);
			}
		}
		logger.trace("getSeriesStatistics.exit; got stats from {} nodes", cnt);
		return result;
	}
	
	protected CompositeData getTotalsStatistics(Callable<CompositeData> statsTask, StatsAggregator aggregator) {
		logger.trace("getTotalsStatistics.enter; going to collect stats for schema: {}", schemaName);

		int cnt = 0;
		CompositeData result = null;
		Map<Member, Future<CompositeData>> futures = execService.submitToAllMembers(statsTask);
		for (Map.Entry<Member, Future<CompositeData>> entry: futures.entrySet()) {
			try {
				CompositeData stats = entry.getValue().get();
				logger.trace("getTotalsStatistics; got stats: {}, from member {}", stats, entry.getKey());
				result = JMXUtils.aggregateStats(stats, result, aggregator);
				logger.trace("getTotalsStatistics; got aggregated result: {}", result);
				cnt++;
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getTotalsStatistics.error: " + ex.getMessage(), ex);
			}
		}
		logger.trace("getTotalsStatistics.exit; got stats from {} nodes", cnt);
		return result;
	}
	
	protected void resetStatistics(Callable<Boolean> statsTask) {
		logger.trace("resetStatistics.enter; going to reset stats for schema: {}", schemaName);

		int cnt = 0;
		Map<Member, Future<Boolean>> futures = execService.submitToAllMembers(statsTask);
		for (Map.Entry<Member, Future<Boolean>> entry: futures.entrySet()) {
			try {
				if (entry.getValue().get()) {
					cnt++;
				}
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("resetStatistics.error: " + ex.getMessage() + " on member " + entry.getKey(), ex);
			}
		}
		logger.trace("resetStatistics.exit; reset stats on {} nodes", cnt);
	}
	
	protected Collection<XDMEntity> getSchemaFeatures(XDMSchema schema) {
		return null;
	}

	protected TabularData getTabularFeatures(String name, String desc, String key) {

		XDMSchema schema = schemaManager.getEntity();
		Collection<XDMEntity> features = getSchemaFeatures(schema); 
		if (features == null || features.size() == 0) {
			return null;
		}
		
        TabularData result = null;
        for (XDMEntity feature: features) {
            try {
                Map<String, Object> def = feature.toMap();
                CompositeData data = JMXUtils.mapToComposite(name, desc, def);
                result = JMXUtils.compositeToTabular(name, desc, key, result, data);
            } catch (Exception ex) {
                logger.error("getTabularFeatures; error", ex);
            }
        }
        return result;
    }
	
	
}
