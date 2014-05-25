package com.bagri.xdm.cache.hazelcast.management;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.system.XDMRole;

@ManagedResource(description="Role Manager MBean")
public class RoleManager extends EntityManager<XDMRole> {

	public RoleManager() {
		super();
	}

	public RoleManager(String roleName) {
		super(roleName);
	}

	@ManagedAttribute(description="Returns Role name")
	public String getName() {
		return entityName;
	}
	
	@ManagedAttribute(description="Returns Role version")
	public int getVersion() {
		return super.getVersion();
	}

	@Override
	protected String getEntityType() {
		return "Role";
	}

	
}
