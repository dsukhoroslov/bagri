package com.bagri.xdm.cache.hazelcast.management;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.system.XDMModule;
import com.bagri.xquery.api.XQCompiler;

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
	
	@ManagedOperation(description="Compiles registered Module")
	public void compileModule() {

		XDMModule module = getEntity();
		xqComp.compileModule("http://helloworld", module.getName(), module.getText());
	}

	@Override
	protected String getEntityType() {
		return "Module";
	}

	@ManagedOperation(description="Returns Module body")
	public String getBody() {
		return getEntity().getText();
	}
	
	@ManagedAttribute(description="Returns Module description")
	public String getDescription() {
		return getEntity().getDescription();
	}

	@ManagedAttribute(description="Returns Module file name")
	public String getFileName() {
		return getEntity().getFileName();
	}

	@ManagedAttribute(description="Returns registered Module name")
	public String getName() {
		return entityName;
	}

	@ManagedAttribute(description="Returns Module compilation state")
	public String getState() {
		return "invalid";
	}

	@ManagedAttribute(description="Returns Module version")
	public int getVersion() {
		return super.getVersion();
	}
	
	@ManagedOperation(description="Updates Module body")
	public void setBody(String body) {
		// TODO: do this via EntryProcessor with locks etc.. 
		XDMModule module = getEntity();
		module.setText(body);
		flushEntity(module);
		// TODO: now write it on disk..
	}
}
