package com.bagri.xdm.cache.api;

import java.util.Collection;

import com.bagri.xdm.common.KeyFactory;
import com.bagri.xdm.system.Library;
import com.bagri.xdm.system.Module;
import com.bagri.xdm.system.Schema;

/**
 * XDM Repository server-side extension; Adds methods to get additional management artifacts on the server side.
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface SchemaRepository extends com.bagri.xdm.api.SchemaRepository {
	
	static String bean_id = "xdmRepo";
	
	/**
	 * 
	 * @return current XDMSchema instance
	 */
	Schema getSchema();
	
	/**
	 * 
	 * @return client management interface
	 */
	ClientManagement getClientManagement();
	
	/**
	 * 
	 * @return index management interface
	 */
	IndexManagement getIndexManagement();
	
	/**
	 * 
	 * @return meta-data management interface
	 */
	ModelManagement getModelManagement();
	
	/**
	 * 
	 * @return population management interface
	 */
	PopulationManagement getPopulationManagement();

	/**
	 * 
	 * @return trigger management interface
	 */
	TriggerManagement getTriggerManagement();

	/**
	 * 
	 * @return libraries registered in this XDM cluster
	 */
	Collection<Library> getLibraries();

	/**
	 * 
	 * @return modules registered in this XDM cluster
	 */
	Collection<Module> getModules();
	
	/**
	 * 
	 * @return key factory to generate various cache keys
	 */
	KeyFactory getFactory();
	
	/**
	 * 
	 * @param dataFormat the name of dataFormat to search for
	 * @return XDMParser instance associated with the dataFormat name 
	 */
	ContentParser getParser(String dataFormat);
	
	/**
	 * 
	 * @param dataFormat the name of dataFormat to search for
	 * @return XDMBuilder instance associated with the dataFormat name
	 */
	ContentBuilder getBuilder(String dataFormat);
	
}
