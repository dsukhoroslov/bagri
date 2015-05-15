package com.bagri.xdm.cache.hazelcast.management;

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
import com.bagri.xdm.cache.hazelcast.task.user.UserCreator;
import com.bagri.xdm.cache.hazelcast.task.user.UserRemover;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMRole;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xdm.system.XDMUser;
import com.bagri.xquery.api.XQCompiler;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Denis Sukhoroslov email: dsukhoroslov@gmail.com
 *
 */
@ManagedResource(objectName="com.bagri.xdm:type=Management,name=ModuleManagement", 
	description="XQuery Module Management MBean")
public class ModuleManagement extends EntityManagement<String, XDMModule> {

	private XQCompiler xqComp;
	
    public ModuleManagement(HazelcastInstance hzInstance) {
    	super(hzInstance);
    }

	public void setXQCompiler(XQCompiler xqComp) {
		this.xqComp = xqComp;
	}
	
	@Override
	protected EntityManager<XDMModule> createEntityManager(String moduleName) {
		ModuleManager mgr = new ModuleManager(moduleName);
		mgr.setEntityCache(entityCache);
		mgr.setXQCompiler(xqComp);
		return mgr;
	}
    
	@ManagedAttribute(description="Return registered Module names")
	public String[] getModuleNames() {
		return entityCache.keySet().toArray(new String[0]);
	}

	@ManagedAttribute(description="Return registered Modules")
	public TabularData getModules() {
		Collection<XDMModule> modules = entityCache.values();
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
/*	
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


/*


	@ManagedOperation(description="Create new User")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "login", description = "User login"),
		@ManagedOperationParameter(name = "password", description = "User password")})
	public boolean addUser(String login, String password) {

		if (!entityCache.containsKey(login)) {
	    	Object result = entityCache.executeOnKey(login, new UserCreator(JMXUtils.getCurrentUser(), password));
	    	logger.debug("addUser; execution result: {}", result);
			return true;
		}
		return false;
	}

	@ManagedOperation(description="Delete User")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "login", description = "User login")})
	public boolean deleteUser(String login) {
		//return userCache.remove(login) != null;
		XDMUser user = entityCache.get(login);
		if (user != null) {
	    	Object result = entityCache.executeOnKey(login, new UserRemover(user.getVersion(), JMXUtils.getCurrentUser()));
	    	logger.debug("deleteUser; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}


*/