package com.bagri.xdm.cache.hazelcast.management;

import javax.xml.xquery.XQConnection;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.system.XDMModule;
import com.bagri.xquery.api.XQCompiler;
import com.bagri.xquery.api.XQProcessor;
import com.hazelcast.core.HazelcastInstance;

@ManagedResource(description="XQuery Module Manager MBean")
public class ModuleManager extends EntityManager<XDMModule> { 

	private XQCompiler xqComp;
	//private IExecutorService execService;

	public ModuleManager() {
		// default constructor
		super();
	}
    
	public ModuleManager(String moduleName) {
		super(moduleName);
		//execService = hzInstance.getExecutorService(PN_XDM_SYSTEM_POOL);
		//IMap<String, XDMNode> nodes = hzInstance.getMap("nodes"); 
		//setEntityCache(nodes);
	}

	public void setXQCompiler(XQCompiler xqComp) {
		this.xqComp = xqComp;
	}
	
	@Override
	protected String getEntityType() {
		return "Module";
	}
	
	@ManagedAttribute(description="Returns registered Module name")
	public String getName() {
		return entityName;
	}

	@ManagedOperation(description="Compiles registered Module")
	public void compileModule() {

		// not implemented yet
		xqComp.compileQuery(this.getEntity().getText());
	}
	
}
