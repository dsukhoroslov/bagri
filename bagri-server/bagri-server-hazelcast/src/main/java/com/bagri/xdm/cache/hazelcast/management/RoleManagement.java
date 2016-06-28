package com.bagri.xdm.cache.hazelcast.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.management.openmbean.TabularData;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.util.JMXUtils;
import com.bagri.xdm.cache.hazelcast.task.role.RoleCreator;
import com.bagri.xdm.cache.hazelcast.task.role.RoleRemover;
import com.bagri.xdm.cache.hazelcast.task.user.UserCreator;
import com.bagri.xdm.cache.hazelcast.task.user.UserRemover;
import com.bagri.xdm.system.Role;
import com.bagri.xdm.system.User;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Denis Sukhoroslov email: dsukhoroslov@gmail.com
 *
 */
@ManagedResource(objectName="com.bagri.xdm:type=Management,name=RoleManagement", 
	description="Role Management MBean")
public class RoleManagement extends EntityManagement<Role> {

	public RoleManagement(HazelcastInstance hzInstance) {
		//
		super(hzInstance);
	}

	@ManagedAttribute(description="Registered Roles")
	public String[] getRoleNames() {
		return getEntityNames();
	}
	
	@ManagedAttribute(description="Return registered Roles")
	public TabularData getRoles() {
		return getEntities("role", "Role definition");
    }
	
	@ManagedOperation(description="Create new Role")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "User name"),
		@ManagedOperationParameter(name = "description", description = "Role description")})
	public boolean addRole(String name, String description) {
		if (!entityCache.containsKey(name)) {
	    	Object result = entityCache.executeOnKey(name, new RoleCreator(getCurrentUser(), description));
	    	logger.debug("addRole; execution result: {}", result);
			return true;
		}
		return false;
	}

	@ManagedOperation(description="Delete Role")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Role name")})
	public boolean deleteRole(String name) {
		//return userCache.remove(login) != null;
		Role role = entityCache.get(name);
		if (role != null) {
	    	Object result = entityCache.executeOnKey(name, new RoleRemover(role.getVersion(), getCurrentUser()));
	    	logger.debug("deleteRole; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@Override
	protected EntityManager<Role> createEntityManager(String roleName) {
		RoleManager mgr = new RoleManager(hzInstance, roleName);
		mgr.setEntityCache(entityCache);
		return mgr;
	}

}
