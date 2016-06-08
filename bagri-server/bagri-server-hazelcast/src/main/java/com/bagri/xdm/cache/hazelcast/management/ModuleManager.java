package com.bagri.xdm.cache.hazelcast.management;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xquery.api.XQCompiler;
import com.hazelcast.core.HazelcastInstance;

@ManagedResource(description="XQuery Module Manager MBean")
public class ModuleManager extends EntityManager<XDMModule> { 

	private XQCompiler xqComp;
	//private IExecutorService execService;

	public ModuleManager() {
		super();
	}
    
	public ModuleManager(HazelcastInstance hzInstance, String moduleName) {
		super(hzInstance, moduleName);
	}

	public void setXQCompiler(XQCompiler xqComp) {
		this.xqComp = xqComp;
	}
	
	@ManagedOperation(description="Compiles registered Module")
	public boolean compileModule() {
		XDMModule module = getEntity();
		try {
			xqComp.compileModule(module);
			return true;
		} catch (XDMException ex) {
			return false;
		}
	}

	@ManagedOperation(description="Reloads registered Module from disk")
	public void refreshModule() {
		Set<String> keys = new HashSet<>(1);
		keys.add(entityName);
		entityCache.loadAll(keys, true);
	}

	@ManagedOperation(description="Returns Module functions")
	public String[] getDeclaredFunctions() {
		XDMModule module = getEntity();
		try {
			List<String> list = xqComp.getModuleFunctions(module);
			return list.toArray(new String[list.size()]);
		} catch (XDMException ex) {
			return null;
		}
	}
	
	@Override
	protected String getEntityType() {
		return "Module";
	}

	@ManagedOperation(description="Returns Module body")
	public String getBody() {
		return getEntity().getBody();
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

	@ManagedAttribute(description="Returns registered Module name")
	public String getNamespace() {
		return getEntity().getNamespace();
	}

	@ManagedAttribute(description="Returns Module compilation state")
	public String getState() {
		return xqComp.getModuleState(getEntity()) ? "valid" : "invalid";
	}

	@ManagedAttribute(description="Returns Module version")
	public int getVersion() {
		return super.getVersion();
	}
	
	@ManagedOperation(description="Updates Module body")
	public void setBody(String body) {
		// TODO: do this via EntryProcessor with locks etc.. 
		XDMModule module = getEntity();
		module.setBody(body);
		flushEntity(module);
	}
}
