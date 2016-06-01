package com.bagri.xdm.cache.api;

import com.bagri.xdm.system.XDMTriggerDef;

public interface XDMTriggerManagement {
	
	/**
	 * @param trigger
	 * registers a new trigger
	 * 
	 */
	boolean createTrigger(XDMTriggerDef trigger);
	
	/**
	 * @param trigger
	 * removes an existing trigger
	 * 
	 */
	boolean deleteTrigger(XDMTriggerDef trigger);
	

}
