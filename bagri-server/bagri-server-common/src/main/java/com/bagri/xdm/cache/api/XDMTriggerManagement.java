package com.bagri.xdm.cache.api;

import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMTriggerDef;
import com.bagri.xdm.system.XDMTriggerDef.Scope;

public interface XDMTriggerManagement {
	
	/**
	 * @param typeId
	 * check if any triggers registered for the type and scope or not
	 * 
	 */
	boolean isTriggerRigistered(int typeId, Scope scope); 
	
	/**
	 * @param typeId
	 * registers a new trigger
	 * 
	 */
	void createTrigger(XDMTriggerDef trigger, XDMDocumentType docType);
	
	/**
	 * @param typeId
	 * remove an existing trigger
	 * 
	 */
	void deleteTrigger(XDMTriggerDef trigger, XDMDocumentType docType);
	

}
