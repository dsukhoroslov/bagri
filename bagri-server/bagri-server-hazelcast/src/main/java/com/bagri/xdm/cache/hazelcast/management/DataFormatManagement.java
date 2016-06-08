package com.bagri.xdm.cache.hazelcast.management;

import static com.bagri.common.util.PropUtils.propsFromString;

import java.io.IOException;
import java.util.Arrays;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.cache.hazelcast.task.format.DataFormatCreator;
import com.bagri.xdm.cache.hazelcast.task.format.DataFormatRemover;
import com.bagri.xdm.system.XDMDataFormat;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Denis Sukhoroslov 
 *
 */
@ManagedResource(objectName="com.bagri.xdm:type=Management,name=DataFormatManagement", 
	description="Data Format Management MBean")
public class DataFormatManagement extends EntityManagement<XDMDataFormat> {

    public DataFormatManagement(HazelcastInstance hzInstance) {
    	super(hzInstance);
    }

	@Override
	protected EntityManager<XDMDataFormat> createEntityManager(String formatName) {
		DataFormatManager mgr = new DataFormatManager(hzInstance, formatName);
		mgr.setEntityCache(entityCache);
		return mgr;
	}
    
	@ManagedAttribute(description="Return registered Data format names")
	public String[] getDataFormatNames() {
		return getEntityNames();
	}

	@ManagedAttribute(description="Return registered Data Formats")
	public TabularData getDataFormats() {
		return getEntities("dataFormat", "Data Format definition");
    }
	
	@ManagedOperation(description="Creates a new Data Format")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "DataFormat name to create"),
		@ManagedOperationParameter(name = "parserClass", description = "Parser implementation class name"),
		@ManagedOperationParameter(name = "builderClass", description = "Builder implementation class name"),
		@ManagedOperationParameter(name = "description", description = "DataFormat description"),
		@ManagedOperationParameter(name = "extensions", description = "Comma-separated format extensions"),
		@ManagedOperationParameter(name = "properties", description = "DataFormat properties with their default values")})
	public boolean addDataFormat(String name, String parser, String builder, String description, String extensions, String properties) {
		logger.trace("addDataFormat.enter; name: {}", name);
		XDMDataFormat format = null;
		if (!entityCache.containsKey(name)) {
			try {
				Object result = entityCache.executeOnKey(name, new DataFormatCreator(getCurrentUser(), parser, builder, description,
						Arrays.asList(extensions.split(", ")), propsFromString(properties)));
		    	format = (XDMDataFormat) result;
			} catch (IOException ex) {
				logger.error("", ex);
			}
		}
		logger.trace("addLibrary.exit; dataFormat created: {}", format);
		return format != null;
	}
	
	@ManagedOperation(description="Removes an existing Extension Library")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Data Format name to delete")})
	public boolean deleteDataFormat(String name) {
		logger.trace("deleteDataFormat.enter; name: {}", name);
		XDMDataFormat format = entityCache.get(name);
		if (format != null) {
	    	Object result = entityCache.executeOnKey(name, new DataFormatRemover(format.getVersion(), getCurrentUser()));
	    	format = (XDMDataFormat) result;
		}
		logger.trace("deleteDataFormat.exit; dataFormat deleted: {}", format);
		return format != null;
	}

}
