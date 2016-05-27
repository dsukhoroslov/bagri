package com.bagri.xdm.cache.hazelcast.management;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.system.XDMDataFormat;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Denis Sukhoroslov email: dsukhoroslov@gmail.com
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
		@ManagedOperationParameter(name = "library", description = "Library name containing DataFormat implementation"),
		@ManagedOperationParameter(name = "description", description = "DataFormat description")})
	public void addDataFormat(String name, String library, String description) {

		logger.trace("addDataFormat.enter; name: {}", name);
		XDMDataFormat format = null;
		if (!entityCache.containsKey(name)) {
	    	//Object result = entityCache.executeOnKey(name, new DataFormatCreator(getCurrentUser(), library, description));
			//return true;
	    	//format = (XDMDataFormat) result;
		}
		//return false;
		logger.trace("addLibrary.exit; dataFormat created: {}", format);
	}
	
	@ManagedOperation(description="Removes an existing Extension Library")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Library name to delete")})
	public void deleteDataFormat(String name) {
		
		logger.trace("deleteDataFormat.enter; name: {}", name);
		XDMDataFormat format = entityCache.get(name);
		if (format != null) {
	    	//Object result = entityCache.executeOnKey(name, new DataFormatRemover(format.getVersion(), getCurrentUser()));
	    	//return result != null;
		}
		//return false;
		logger.trace("deleteDataFormat.exit; dataFormat deleted");
	}



}
