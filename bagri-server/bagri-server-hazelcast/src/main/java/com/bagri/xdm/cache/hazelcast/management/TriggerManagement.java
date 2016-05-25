package com.bagri.xdm.cache.hazelcast.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.cache.hazelcast.task.trigger.TriggerCreator;
import com.bagri.xdm.cache.hazelcast.task.trigger.TriggerRemover;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xdm.system.XDMTriggerAction;
import com.bagri.xdm.system.XDMTriggerAction.Scope;
import com.bagri.xdm.system.XDMTriggerDef;
import com.bagri.xdm.system.XDMTriggerAction.Order;
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
		return super.getUsageStatistics(new StatisticSeriesCollector(schemaName, "triggerStats"), aggregator);
	}

	@ManagedOperation(description="Creates a new Java Trigger")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "library", description = "Java Library with Trigger implementation"),
		@ManagedOperationParameter(name = "className", description = "Trigger class name"),
		@ManagedOperationParameter(name = "docType", description = "Document type to fire on"),
		@ManagedOperationParameter(name = "synchronous", description = "Sync/Async trigger behaviour"),
		@ManagedOperationParameter(name = "actions", description = "Triggered actions and scope")})
	public void addJavaTrigger(String library, String className, String docType, boolean synchronous, String actions) {
		addTrigger(true, library, className, docType, synchronous, actions);
	}
	
	@ManagedOperation(description="Creates a new XQuery Trigger")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "module", description = "XQuery Module with Trigger function"),
		@ManagedOperationParameter(name = "function", description = "Trigger function name"),
		@ManagedOperationParameter(name = "docType", description = "Document type to fire on"),
		@ManagedOperationParameter(name = "synchronous", description = "Sync/Async trigger behaviour"),
		@ManagedOperationParameter(name = "actions", description = "Triggered actions and scope")})
	public void addXQueryTrigger(String module, String function, String docType, boolean synchronous, String actions) {
		addTrigger(false, module, function, docType, synchronous, actions);
	}
	
	private void addTrigger(boolean java, String container, String implementation, String docType, 
			boolean synchronous, String actions) {

		logger.trace("addTrigger.enter;");
		long stamp = System.currentTimeMillis();
		String type = (docType == null || docType.trim().length() == 0) ? null : docType;
		
		StringTokenizer st = new StringTokenizer(actions, " ,");
		List<XDMTriggerAction> acts = new ArrayList<>();
		while (st.hasMoreTokens()) {
			String order = st.nextToken();
			String scope = st.nextToken();
			acts.add(new XDMTriggerAction(Order.valueOf(order), Scope.valueOf(scope)));
		}
		
		int index = schemaManager.getEntity().getTriggers().size();
		XDMTriggerDef trigger = schemaManager.addTrigger(java, container, implementation, type, synchronous, acts, index);
		if (trigger == null) {
			throw new IllegalArgumentException("Trigger '" + implementation + "' in schema '" + schemaName + "' already registered");
		}
		
		TriggerCreator task = new TriggerCreator(trigger);
		Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		int cnt = 0;
		for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
			try {
				if (entry.getValue().get()) {
					cnt++;
				}
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("addTrigger.error; ", ex);
			}
		}
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

		TriggerRemover task = new TriggerRemover(className);
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
