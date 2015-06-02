package com.bagri.xdm.cache.api;

import com.bagri.xdm.system.XDMTriggerDef;

public interface XDMTriggerManagement {
	
	/**
	 * @param typeId
	 * check if any triggers registered for the type and scope or not
	 * 
	 */
	boolean isTriggerRigistered(int typeId); 
	
	/**
	 * @param typeId
	 * registers a new trigger
	 * 
	 */
	boolean createTrigger(XDMTriggerDef trigger);
	
	/**
	 * @param typeId
	 * remove an existing trigger
	 * 
	 */
	boolean deleteTrigger(XDMTriggerDef trigger);
	

}
