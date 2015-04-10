package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMSchema;

@ManagedResource(description="Schema Documents Management MBean")
public class ModuleManagement extends SchemaFeatureManagement {

	private SchemaManager schemaManager;

    public ModuleManagement(String schemaName) {
    	super(schemaName);
    }

	@Override
	protected String getFeatureKind() {
		return "ModuleManagement";
	}
	
	public void setSchemaManager(SchemaManager schemaManager) {
		this.schemaManager = schemaManager;
	}

	@ManagedAttribute(description="Return modules defined on Schema")
	public TabularData getModules() {
		XDMSchema schema = schemaManager.getEntity();
		Set<XDMModule> modules = schema.getModules();
        logger.trace("getModules; modules: {}", modules);
		if (modules.size() == 0) {
			return null;
		}
		
        TabularData result = null;
        for (XDMModule module: modules) {
            try {
                Map<String, Object> def = module.toMap();
                CompositeData data = JMXUtils.mapToComposite("module", "Module definition", def);
                result = JMXUtils.compositeToTabular("module", "Module definition", "name", result, data);
            } catch (Exception ex) {
                logger.error("getModules; error", ex);
            }
        }
        return result;
    }
	
	@ManagedOperation(description="Creates a new Module")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Module name to create"),
		@ManagedOperationParameter(name = "fileName", description = "File for module"),
		@ManagedOperationParameter(name = "description", description = "Module description"),
		@ManagedOperationParameter(name = "text", description = "Module body")})
	public void addModule(String name, String fileName, String description, String text) {

		logger.trace("addModule.enter;");
		XDMModule module = schemaManager.addModule(name, fileName, description, text);
		if (module == null) {
			throw new IllegalStateException("Module '" + name + "' in schema '" + schemaName + "' already exists");
		}
		
		//IndexCreator task = new IndexCreator(index);
		//Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		//int cnt = 0;
		//for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
		//	try {
		//		if (entry.getValue().get()) {
		//			cnt++;
		//		}
		//	} catch (InterruptedException | ExecutionException ex) {
		//		logger.error("addIndex.error; ", ex);
		//	}
		//}
		logger.trace("addModule.exit; module created: {}", module);
	}
	
	@ManagedOperation(description="Removes an existing Module")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Module name to delete")})
	public void dropModule(String name) {
		
		logger.trace("dropModule.enter;");
		if (!schemaManager.deleteModule(name)) {
			throw new IllegalStateException("Module '" + name + "' in schema '" + schemaName + "' does not exist");
		}

		//IndexRemover task = new IndexRemover(name);
		//Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		//int cnt = 0;
		//for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
		//	try {
		//		if (entry.getValue().get()) {
		//			cnt++;
		//		}
		//	} catch (InterruptedException | ExecutionException ex) {
		//		logger.error("dropIndex.error; ", ex);
		//	}
		//}
		logger.trace("dropModule.exit; module deleted on members");
	}

	@ManagedOperation(description="Enables/Disables an existing Module")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Module name to enable/disable"),
		@ManagedOperationParameter(name = "enable", description = "enable/disable module")})
	public void enableModule(String name, boolean enable) {
		
		if (!schemaManager.enableModule(name, enable)) {
			throw new IllegalStateException("Module '" + name + "' in schema '" + schemaName + 
					"' does not exist or already " + (enable ? "enabled" : "disabled"));
		}
	}
	
	@ManagedOperation(description="Creates a new Module")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Module name to create"),
		@ManagedOperationParameter(name = "fileName", description = "File for module"),
		@ManagedOperationParameter(name = "description", description = "Module description")})
	public void registerModule(String name, String fileName, String description) throws IOException {

		logger.trace("registerModule.enter;");
		// is it local (to admin) fileName or external (on schema) ?
		String text = FileUtils.readTextFile(fileName);
		XDMModule module = schemaManager.addModule(name, fileName, description, text);
		if (module == null) {
			throw new IllegalStateException("Module '" + name + "' in schema '" + schemaName + "' already exists");
		}

		logger.trace("registerModule.exit; module registered: {}", module);
	}
}
