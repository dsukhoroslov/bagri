package com.bagri.core.server.api;

import java.util.Collection;

import com.bagri.core.KeyFactory;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;

/**
 * XDM Repository server-side extension; Adds methods to get additional management artifacts on the server side.
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface SchemaRepository extends com.bagri.core.api.SchemaRepository {
	
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
	 * @return ContentParser instance associated with the dataFormat name 
	 */
	ContentParser getParser(String dataFormat);
	
	/**
	 * 
	 * @param dataFormat the name of dataFormat to search for
	 * @return ContentBuilder instance associated with the dataFormat name
	 */
	ContentBuilder getBuilder(String dataFormat);
	
	/**
	 * 
	 * @param dataFormat the name of dataFormat to search for
	 * @return ContentModeler instance associated with the dataFormat name
	 */
	ContentModeler getModeler(String dataFormat);
	
	/**
	 * 
	 * @param dataFormat the name of dataFormat to search for
	 * @param source the class of source data to convert from
	 * @return the converter to convert source to data format
	 */
	ContentConverter getConverter(String dataFormat, Class source);
}
