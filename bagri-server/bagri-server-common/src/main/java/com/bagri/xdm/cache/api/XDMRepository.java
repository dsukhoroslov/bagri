package com.bagri.xdm.cache.api;

import com.bagri.xdm.system.XDMSchema;

public interface XDMRepository extends com.bagri.xdm.api.XDMRepository {
	
	XDMSchema getSchema();
	
	XDMIndexManagement getIndexManagement();

}
