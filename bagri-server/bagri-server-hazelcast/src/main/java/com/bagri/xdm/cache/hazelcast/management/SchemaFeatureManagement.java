package com.bagri.xdm.cache.hazelcast.management;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;
import javax.xml.xquery.XQConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.cache.hazelcast.task.stats.InvocationStatsCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.InvocationStatsReseter;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public abstract class SchemaFeatureManagement implements SelfNaming {
	
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String schemaName;
	protected IExecutorService execService;
	protected XDMModelManagement modelMgr;
	
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
	
	protected abstract String getFeatureKind();

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return JMXUtils.getObjectName("type=Schema,name=" + schemaName + ",kind=" + getFeatureKind());
	}

	protected TabularData getInvocationStatistics(Callable<TabularData> statsTask) {
		logger.trace("getInvocationStatistics.enter; going to collect stats for schema: {}", schemaName);

		int cnt = 0;
		TabularData result = null;
		Map<Member, Future<TabularData>> futures = execService.submitToAllMembers(statsTask);
		for (Map.Entry<Member, Future<TabularData>> entry: futures.entrySet()) {
			try {
				TabularData stats = entry.getValue().get();
				logger.trace("getInvocationStatistics; got stats: {}, from member {}", stats, entry.getKey());
				result = JMXUtils.aggregateStats(stats, result);
				logger.trace("getInvocationStatistics; got aggregated result: {}", result);
				cnt++;
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getInvocationStatistics.error: " + ex.getMessage(), ex);
			}
		}
		logger.trace("getInvocationStatistics.exit; got stats from {} nodes", cnt);
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

	
	
}
