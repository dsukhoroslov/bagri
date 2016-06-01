package com.bagri.xdm.cache.api;

import java.util.Collection;

import com.bagri.xdm.common.XDMBuilder;
import com.bagri.xdm.common.XDMKeyFactory;
import com.bagri.xdm.common.XDMParser;
import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMSchema;

public interface XDMRepository extends com.bagri.xdm.api.XDMRepository {
	
	XDMSchema getSchema();
	
	XDMClientManagement getClientManagement();
	
	XDMIndexManagement getIndexManagement();
	
	XDMTriggerManagement getTriggerManagement();

	Collection<XDMLibrary> getLibraries();

	Collection<XDMModule> getModules();
	
	XDMKeyFactory getFactory();
	
	XDMParser getParser(String dataFormat);
	
	XDMBuilder getBuilder(String dataFormat);
}
