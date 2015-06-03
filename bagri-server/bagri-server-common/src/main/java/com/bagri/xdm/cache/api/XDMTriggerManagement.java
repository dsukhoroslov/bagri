package com.bagri.xdm.cache.api;

import com.bagri.xdm.system.XDMTriggerDef;

public interface XDMTriggerManagement {
	
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
