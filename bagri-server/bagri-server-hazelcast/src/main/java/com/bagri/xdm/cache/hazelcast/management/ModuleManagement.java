package com.bagri.xdm.cache.hazelcast.management;

import static com.bagri.common.config.XDMConfigConstants.xdm_cluster_login;

import java.io.IOException;
import java.util.Collection;
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
import com.bagri.xdm.cache.hazelcast.task.module.ModuleCreator;
import com.bagri.xdm.cache.hazelcast.task.module.ModuleRemover;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMUser;
import com.bagri.xquery.api.XQCompiler;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

/**
 * @author Denis Sukhoroslov email: dsukhoroslov@gmail.com
 *
 */
@ManagedResource(objectName="com.bagri.xdm:type=Management,name=ModuleManagement", 
	description="XQuery Module Management MBean")
public class ModuleManagement extends EntityManagement<XDMModule> {

	private XQCompiler xqComp;
	
    public ModuleManagement(HazelcastInstance hzInstance) {
    	super(hzInstance);
    }

	public void setXQCompiler(XQCompiler xqComp) {
		this.xqComp = xqComp;
	}
	
	@Override
	protected EntityManager<XDMModule> createEntityManager(String moduleName) {
		ModuleManager mgr = new ModuleManager(hzInstance, moduleName);
		mgr.setEntityCache(entityCache);
		mgr.setXQCompiler(xqComp);
		return mgr;
	}
    
	@ManagedAttribute(description="Return registered Module names")
	public String[] getModuleNames() {
		return getEntityNames();
	}

	@ManagedAttribute(description="Return registered Modules")
	public TabularData getModules() {
		return getEntities("module", "Module definition");
    }
	
	@ManagedOperation(description="Creates a new Module")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Module name to create"),
		@ManagedOperationParameter(name = "fileName", description = "File for module"),
		@ManagedOperationParameter(name = "description", description = "Module description"),
		@ManagedOperationParameter(name = "namespace", description = "Module namespace")})
	public boolean addModule(String name, String fileName, String description, String namespace) {
		logger.trace("addModule.enter;");
		XDMModule module = null;
		if (!entityCache.containsKey(name)) {
	    	Object result = entityCache.executeOnKey(name, new ModuleCreator(getCurrentUser(), fileName, namespace, description));
	    	module = (XDMModule) result;
		}
		logger.trace("addModule.exit; module created: {}", module);
		return module != null;
	}
	
	@ManagedOperation(description="Removes an existing Module")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Module name to delete")})
	public boolean deleteModule(String name) {
		logger.trace("deleteModule.enter; name: {}", name);
		XDMModule module = entityCache.get(name);
		if (module != null) {
	    	Object result = entityCache.executeOnKey(name, new ModuleRemover(module.getVersion(), getCurrentUser()));
	    	module = (XDMModule) result;
		}
		logger.trace("deleteModule.exit; module deleted: {}", module);
		return module != null;
	}
/*
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
*/		
	
}
