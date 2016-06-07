package com.bagri.xdm.api;

import com.bagri.xdm.system.XDMPermission.Permission;

/**
 * XDM access management interface; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public interface XDMAccessManagement {
	
	/**
	 * 
	 * @param username the user login
	 * @param password the user password
	 * @return true in case of successful authentication, false otherwise 
	 */
	boolean authenticate(String username, String password);

	/**
	 * 
	 * @param username the authenticated user login
	 * @param permission the {@link Permission} to check
	 * @return true in case of successful authorization, false otherwise
	 */
	boolean hasPermission(String username, Permission permission);
	
}
