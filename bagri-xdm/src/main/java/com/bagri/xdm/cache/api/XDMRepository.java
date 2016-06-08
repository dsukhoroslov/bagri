package com.bagri.xdm.cache.api;

import java.util.Collection;

import com.bagri.xdm.common.XDMBuilder;
import com.bagri.xdm.common.XDMKeyFactory;
import com.bagri.xdm.common.XDMParser;
import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMSchema;

/**
 * XDM Repository server-side extension; Adds methods to get additional management artifacts on the server side.
 * 
 * @author Denis Suukhoroslov
 *
 */
public interface XDMRepository extends com.bagri.xdm.api.XDMRepository {
	
	static String bean_id = "xdmRepo";
	
	/**
	 * 
	 * @return current XDMSchema instance
	 */
	XDMSchema getSchema();
	
	/**
	 * 
	 * @return client management interface
	 */
	XDMClientManagement getClientManagement();
	
	/**
	 * 
	 * @return index management interface
	 */
	XDMIndexManagement getIndexManagement();
	
	/**
	 * 
	 * @return trigger management interface
	 */
	XDMTriggerManagement getTriggerManagement();

	/**
	 * 
	 * @return libraries registered in this XDM cluster
	 */
	Collection<XDMLibrary> getLibraries();

	/**
	 * 
	 * @return modules registered in this XDM cluster
	 */
	Collection<XDMModule> getModules();
	
	/**
	 * 
	 * @return key factory to generate various cache keys
	 */
	XDMKeyFactory getFactory();
	
	/**
	 * 
	 * @param dataFormat the name of dataFormat to search for
	 * @return XDMParser instance associated with the dataFormat name 
	 */
	XDMParser getParser(String dataFormat);
	
	/**
	 * 
	 * @param dataFormat the name of dataFormat to search for
	 * @return XDMBuilder instance associated with the dataFormat name
	 */
	XDMBuilder getBuilder(String dataFormat);
	
}
