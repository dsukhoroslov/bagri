package com.bagri.xdm.api;

import com.bagri.xdm.system.XDMPermission.Permission;

public interface XDMAccessManagement {
	
	boolean authenticate(String username, String password);

	boolean hasPermission(String username, Permission permission);
}
