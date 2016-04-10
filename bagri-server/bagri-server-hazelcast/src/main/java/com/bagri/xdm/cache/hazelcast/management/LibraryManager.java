package com.bagri.xdm.cache.hazelcast.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.cache.hazelcast.task.EntityProcessor.Action;
import com.bagri.xdm.cache.hazelcast.task.library.LibFunctionUpdater;
import com.bagri.xdm.system.XDMFunction;
import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xquery.api.XQCompiler;
import com.hazelcast.core.HazelcastInstance;

@ManagedResource(description="Extension Library Manager MBean")
public class LibraryManager extends EntityManager<XDMLibrary> { 

	private XQCompiler xqComp;
	//private IExecutorService execService;

	public LibraryManager() {
		super();
	}
    
	public LibraryManager(HazelcastInstance hzInstance, String libraryName) {
		super(hzInstance, libraryName);
	}

	public void setXQCompiler(XQCompiler xqComp) {
		this.xqComp = xqComp;
	}
	
	@ManagedAttribute(description="Returns Library functions")
	public String[] getDeclaredFunctions() {
		XDMLibrary library = getEntity();
		List<String> result = new ArrayList<>(library.getFunctions().size());
		for (XDMFunction func: library.getFunctions()) {
			result.add(func.toString());
		}
		Collections.sort(result);
		return result.toArray(new String[result.size()]);
	}
	
	@Override
	protected String getEntityType() {
		return "Library";
	}

	@ManagedAttribute(description="Returns Library description")
	public String getDescription() {
		return getEntity().getDescription();
	}

	@ManagedAttribute(description="Returns Library file name")
	public String getFileName() {
		return getEntity().getFileName();
	}

	@ManagedAttribute(description="Returns registered Library name")
	public String getName() {
		return entityName;
	}

	@ManagedAttribute(description="Returns Library version")
	public int getVersion() {
		return super.getVersion();
	}
	
	@ManagedOperation(description="Adds a new Extension Function")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "className", description = "A full class name"),
		@ManagedOperationParameter(name = "prefix", description = "Namespace prefix"),
		@ManagedOperationParameter(name = "description", description = "Textual function description, can be ommited"),
		@ManagedOperationParameter(name = "signature", description = "Method signature: method-name(params...): result-type")})
	public void addFunction(String className, String prefix, String description, String signature) {

		logger.trace("addFunction.enter; className: {}; signature: {}", className, signature);
		XDMFunction function = (XDMFunction) entityCache.executeOnKey(entityName,  
	    			new LibFunctionUpdater(getVersion(), getCurrentUser(), className, prefix, description, signature, Action.add));
		// notify existing sessions about library/function change ?!
		logger.trace("addFunction.exit; function created: {}", function);
	}
	
	@ManagedOperation(description="Removes an existing Extension Function")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "className", description = "A full class name"),
		@ManagedOperationParameter(name = "signature", description = "Method signature: method-name(params...): result-type")})
	public void deleteFunction(String className, String signature) {
		
		logger.trace("deleteFunction.enter; className: {}; signature: {}", className, signature);
		XDMFunction function = (XDMFunction) entityCache.executeOnKey(entityName,  
    			new LibFunctionUpdater(getVersion(), getCurrentUser(), className, "test", null, signature, Action.remove));
		// notify existing sessions about library/function change ?!
		logger.trace("deleteFunction.exit; function deleted: {}", function);
	}

}
