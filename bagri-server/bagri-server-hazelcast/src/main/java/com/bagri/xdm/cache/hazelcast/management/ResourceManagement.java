package com.bagri.xdm.cache.hazelcast.management;

import java.util.Collection;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.rest.BagriRestServer;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.system.Resource;
import com.bagri.xdm.system.Schema;

@ManagedResource(description="Schema Resources Management MBean")
public class ResourceManagement extends SchemaFeatureManagement {
	
	public ResourceManagement(String schemaName) {
		super(schemaName);
	}

	protected String getFeatureKind() {
		return "ResourceManagement";
	}

	@Override
	protected Collection getSchemaFeatures(Schema schema) {
		return schema.getResources();
	}

	@ManagedAttribute(description="Return resources defined on Schema")
	public TabularData getResources() {
		return getTabularFeatures("resource", "Resource definition", "name");
    }
	
	@ManagedAttribute(description="Return aggregated resource usage statistics, per resource")
	public TabularData getResourceStatistics() {
		return null; //super.getUsageStatistics(new StatisticSeriesCollector(schemaName, "resourceStats"), aggregator);
	}

	@ManagedOperation(description="Creates a new REST Resource")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Resource name"),
		@ManagedOperationParameter(name = "path", description = "Resource base path"),
		@ManagedOperationParameter(name = "module", description = "XQuery Module with Resource functions"),
		@ManagedOperationParameter(name = "description", description = "Resource description")})
	public void addResource(String name, String path, String module, String description) {

		logger.trace("addResource.enter;");
		long stamp = System.currentTimeMillis();
		Resource resource = schemaManager.addResource(name, path, module, description);
		if (resource == null) {
			throw new IllegalArgumentException("Resource '" + name + "' in schema '" + schemaName + "' already registered");
		}
		
		//ResourceCreator task = new ResourceCreator(resource);
		//Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		int cnt = 0;
		//for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
		//	try {
		//		if (entry.getValue().get()) {
		//			cnt++;
		//		}
		//	} catch (InterruptedException | ExecutionException ex) {
		//		logger.error("addTrigger.error; ", ex);
		//	}
		//}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("addResource.exit; resource created on {} members; timeTaken: {}", cnt, stamp);
	}
	
	@ManagedOperation(description="Removes an existing Resource")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Resource name to delete")})
	public void dropResource(String name) {
		
		logger.trace("dropResource.enter;");
		long stamp = System.currentTimeMillis();
		if (!schemaManager.deleteResource(name)) {
			throw new IllegalStateException("Resource '" + name + "' in schema '" + schemaName + "' does not exist");
		}

		//ResourceRemover task = new ResourceRemover(name);
		//Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		int cnt = 0;
		//for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
		//	try {
		//		if (entry.getValue().get()) {
		//			cnt++;
		//		}
		//	} catch (InterruptedException | ExecutionException ex) {
		//		logger.error("dropResource.error; ", ex);
		//	}
		//}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("dropResource.exit; resource deleted on {} members; timeTaken: {}", cnt, stamp);
	}

	@ManagedOperation(description="Enables/Disables an existing Resource")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Resource name to enable/disable"),
		@ManagedOperationParameter(name = "enable", description = "enable/disable trigger")})
	public void enableResource(String name, boolean enable) {
		
		if (!schemaManager.enableResource(name, enable)) {
			throw new IllegalStateException("Resource '" + name + "' in schema '" + schemaName + 
					"' does not exist or already " + (enable ? "enabled" : "disabled"));
		}
		
		// switch it on/off here!!
	}
	
	@ManagedOperation(description="Reload Schema Resources")
	public void reloadResources() {
		BagriRestServer rest = schemaManager.getParent().getRestService();
		if (rest != null) {
			rest.reload(schemaName, true);
		}
	}
	
	
}
