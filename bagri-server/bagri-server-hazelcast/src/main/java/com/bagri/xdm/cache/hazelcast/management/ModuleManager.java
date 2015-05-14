package com.bagri.xdm.cache.hazelcast.management;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.system.XDMModule;
import com.hazelcast.core.HazelcastInstance;

@ManagedResource(description="XQuery Module Manager MBean")
public class ModuleManager extends EntityManager<XDMModule> { 

    private HazelcastInstance hzInstance;
	//private IExecutorService execService;

	public ModuleManager() {
		// default constructor
		super();
	}
    
	public ModuleManager(HazelcastInstance hzInstance, String moduleName) {
		super(moduleName);
		this.hzInstance = hzInstance;
		//execService = hzInstance.getExecutorService(PN_XDM_SYSTEM_POOL);
		//IMap<String, XDMNode> nodes = hzInstance.getMap("nodes"); 
		//setEntityCache(nodes);
	}
	
	@Override
	protected String getEntityType() {
		return "Module";
	}
	
	@ManagedAttribute(description="Returns registered Module name")
	public String getName() {
		// this is not an entityName!
		return getEntity().getName();
	}

}
