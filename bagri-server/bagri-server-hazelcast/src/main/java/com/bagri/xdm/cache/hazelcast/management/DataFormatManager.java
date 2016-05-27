package com.bagri.xdm.cache.hazelcast.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.openmbean.CompositeData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.cache.hazelcast.task.EntityProcessor.Action;
import com.bagri.xdm.system.XDMDataFormat;
import com.hazelcast.core.HazelcastInstance;

@ManagedResource(description="Data Format Manager MBean")
public class DataFormatManager extends EntityManager<XDMDataFormat> { 

	public DataFormatManager() {
		super();
	}
    
	public DataFormatManager(HazelcastInstance hzInstance, String formatName) {
		super(hzInstance, formatName);
	}

	@ManagedAttribute(description="Returns Data Format extensions")
	public String[] getExtensions() {
		XDMDataFormat format = getEntity();
		List<String> result = new ArrayList<>(format.getExtensions());
		Collections.sort(result);
		return result.toArray(new String[result.size()]);
	}
	
	@ManagedAttribute(description="Returns Data Store properties")
	public CompositeData getProperties() {
		XDMDataFormat format = getEntity();
		return JMXUtils.propsToComposite(entityName, "properties", format.getProperties());
	}
	
	@Override
	protected String getEntityType() {
		return "DataFormat";
	}

	@ManagedAttribute(description="Returns Data Format description")
	public String getDescription() {
		return getEntity().getDescription();
	}

	@ManagedAttribute(description="Returns Data Format library name")
	public String getLibrary() {
		return getEntity().getLibrary();
	}

	@ManagedAttribute(description="Returns registered Data Format name")
	public String getName() {
		return entityName;
	}

	@ManagedAttribute(description="Returns Data Format version")
	public int getVersion() {
		return super.getVersion();
	}
	
	@ManagedAttribute(description="Returns Parser class for this Data Format")
	public String getParserClass() {
		return getEntity().getParserClass();
	}
	
	@ManagedAttribute(description="Returns Builder class for this Data Format")
	public String getBuilderClass() {
		return getEntity().getBuilderClass();
	}
	
	@ManagedOperation(description="Adds a new file extension")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "extension", description = "File extension")})
	public void addExtension(String ext) {

		logger.trace("addExtension.enter; ext: {}", ext);
		//XDMFunction function = (XDMFunction) entityCache.executeOnKey(entityName,  
	    //			new LibFunctionUpdater(getVersion(), getCurrentUser(), className, prefix, description, signature, Action.add));
		// notify existing sessions about library/function change ?!
		//logger.trace("addExtension.exit; function created: {}", function);
	}
	
	@ManagedOperation(description="Removes an existing file extension")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "extension", description = "File extension")})
	public void deleteExtension(String ext) {
		
		logger.trace("deleteExtension.enter; ext: {}", ext);
		//XDMFunction function = (XDMFunction) entityCache.executeOnKey(entityName,  
    	//		new LibFunctionUpdater(getVersion(), getCurrentUser(), className, "test", null, signature, Action.remove));
		// notify existing sessions about library/function change ?!
		//logger.trace("deleteExtension.exit; function deleted: {}", function);
	}



}
