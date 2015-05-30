package com.bagri.xdm.cache.hazelcast.management;

import java.util.ArrayList;
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
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.common.XDMEntity;
import com.bagri.xdm.system.XDMTriggerDef;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xdm.system.XDMTriggerDef.Scope;
import com.hazelcast.core.Member;

@ManagedResource(description="Schema Triggers Management MBean")
public class TriggerManagement extends SchemaFeatureManagement {
	
	public TriggerManagement(String schemaName) {
		super(schemaName);
	}

	protected String getFeatureKind() {
		return "TriggerManagement";
	}
	
	@Override
	protected Collection getSchemaFeatures(XDMSchema schema) {
		return schema.getTriggers();
	}
	//protected Collection<XDMEntity> getSchemaFeatures(XDMSchema schema) {
	//	Collection<XDMEntity> result = new ArrayList<>(schema.getTriggers().size());
	//	result.addAll(schema.getTriggers());
	//	return result;
	//}

	@ManagedAttribute(description="Return triggers defined on Schema")
	public TabularData getTriggers() {
		return getTabularFeatures("trigger", "Trigger definition", "className");
    }
	
	@ManagedAttribute(description="Return aggregated trigger usage statistics, per trigger")
	public TabularData getTriggerStatistics() {
		//return super.getInvocationStatistics(new StatisticSeriesCollector(schemaName, "indexStats"));
		
		//StatisticSeriesCollector statsTask = new StatisticSeriesCollector(schemaName, "indexStats");
		//int cnt = 0;
		TabularData result = null;
		//Map<Member, Future<TabularData>> futures = execService.submitToAllMembers(statsTask);
		//for (Map.Entry<Member, Future<TabularData>> entry: futures.entrySet()) {
		//	try {
		//		TabularData stats = entry.getValue().get();
		//		result = stats; //JMXUtils.aggregateStats(stats, result);
		//		cnt++;
		//	} catch (InterruptedException | ExecutionException ex) {
		//		logger.error("getInvocationStatistics.error: " + ex.getMessage(), ex);
		//	}
		//}
		//logger.trace("getInvocationStatistics.exit; got stats from {} nodes", cnt);
		return result;
	}

	@ManagedOperation(description="Creates a new Trigger")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "library", description = "Library with Trigger implementation"),
		@ManagedOperationParameter(name = "className", description = "Trigger class name"),
		@ManagedOperationParameter(name = "scope", description = "Trigger scope")})
	public void addTrigger(String library, String className, String scope) {

		logger.trace("addTrigger.enter;");
		long stamp = System.currentTimeMillis();
		XDMTriggerDef trigger = schemaManager.addTrigger(library, className, Scope.valueOf(scope));
		if (trigger == null) {
			throw new IllegalStateException("Trigger '" + className + "' in schema '" + schemaName + "' already registered");
		}
		
		//IndexCreator task = new IndexCreator(index);
		//Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		int cnt = 0;
		//for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
		//	try {
		//		if (entry.getValue().get()) {
		//			cnt++;
		//		}
		//	} catch (InterruptedException | ExecutionException ex) {
		//		logger.error("addIndex.error; ", ex);
		//	}
		//}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("addTrigger.exit; trigger created on {} members; timeTaken: {}", cnt, stamp);
	}
	
	@ManagedOperation(description="Removes an existing Trigger")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "className", description = "Trigger className to delete")})
	public void dropTrigger(String className) {
		
		logger.trace("dropTrigger.enter;");
		long stamp = System.currentTimeMillis();
		if (!schemaManager.deleteTrigger(className)) {
			throw new IllegalStateException("Trigger '" + className + "' in schema '" + schemaName + "' does not exist");
		}

		//IndexRemover task = new IndexRemover(name);
		//Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		int cnt = 0;
		//for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
		//	try {
		//		if (entry.getValue().get()) {
		//			cnt++;
		//		}
		//	} catch (InterruptedException | ExecutionException ex) {
		//		logger.error("dropIndex.error; ", ex);
		//	}
		//}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("dropTrigger.exit; trigger deleted on {} members; timeTaken: {}", cnt, stamp);
	}

	@ManagedOperation(description="Enables/Disables an existing Trigger")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "className", description = "Trigger className to enable/disable"),
		@ManagedOperationParameter(name = "enable", description = "enable/disable trigger")})
	public void enableTrigger(String className, boolean enable) {
		
		if (!schemaManager.enableTrigger(className, enable)) {
			throw new IllegalStateException("Trigger '" + className + "' in schema '" + schemaName + 
					"' does not exist or already " + (enable ? "enabled" : "disabled"));
		}
	}
	

}
