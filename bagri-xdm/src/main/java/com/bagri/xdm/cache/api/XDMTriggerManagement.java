package com.bagri.xdm.cache.api;

import com.bagri.xdm.system.XDMTriggerDef;

public interface XDMTriggerManagement {
	
	/**
	 * registers a new trigger
	 *
	 * @param trigger the {@link XDMTriggerDef} definition to register in the current schema
	 * @return true if trigger registered, false otherwise
	 * 
	 */
	boolean createTrigger(XDMTriggerDef trigger);
	
	/**
	 * removes an existing trigger
	 * 
	 * @param trigger the {@link XDMTriggerDef} definition to unregister from the current schema
	 * @return true if trigger unregistered, false otherwise
	 */
	boolean deleteTrigger(XDMTriggerDef trigger);
	

}
