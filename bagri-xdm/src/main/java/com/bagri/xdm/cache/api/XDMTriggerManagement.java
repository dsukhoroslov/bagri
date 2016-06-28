package com.bagri.xdm.cache.api;

import com.bagri.xdm.system.TriggerDefinition;

/**
 * XDM Trigger Management interface; Adds/Deletes XDM Schema triggers at runtime
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface XDMTriggerManagement {
	
	/**
	 * registers a new trigger
	 *
	 * @param trigger the {@link TriggerDefinition} definition to register in the current schema
	 * @return true if trigger registered, false otherwise
	 * 
	 */
	boolean createTrigger(TriggerDefinition trigger);
	
	/**
	 * removes an existing trigger
	 * 
	 * @param trigger the {@link TriggerDefinition} definition to unregister from the current schema
	 * @return true if trigger unregistered, false otherwise
	 */
	boolean deleteTrigger(TriggerDefinition trigger);
	

}
