package com.bagri.xdm.cache.api;

import java.util.Collection;

import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMSchema;

public interface XDMRepository extends com.bagri.xdm.api.XDMRepository {
	
	XDMSchema getSchema();
	
	XDMIndexManagement getIndexManagement();
	
	XDMTriggerManagement getTriggerManagement();

	Collection<XDMLibrary> getLibraries();

	Collection<XDMModule> getModules();
}
