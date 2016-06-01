package com.bagri.xdm.cache.api;

import com.bagri.xdm.system.XDMPermission.Permission;

public interface XDMAccessManagement extends com.bagri.xdm.api.XDMAccessManagement {

	boolean hasPermission(String username, Permission perm);
	
}
