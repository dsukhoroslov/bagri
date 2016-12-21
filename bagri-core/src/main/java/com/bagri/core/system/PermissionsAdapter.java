package com.bagri.core.system;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The adapter converting Collection of permissions to Map of permissions
 * 
 * @author Denis Sukhoroslov
 *
 */
public class PermissionsAdapter extends XmlAdapter<Permissions, Map<String, Permission>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Permissions marshal(Map<String, Permission> perms) throws Exception {
	    Permissions xdmPerms = new Permissions();
	    for (Permission xdmPerm : perms.values()) {
	    	xdmPerms.addPermission(xdmPerm);
	    }
	    return xdmPerms;	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Permission> unmarshal(Permissions xdmPerms) throws Exception {
		Map<String, Permission> perms = new HashMap<String, Permission>();
	    for (Permission xdmPerm : xdmPerms.permissions()) {
	    	perms.put(xdmPerm.getResource(), xdmPerm);
	    }
	    return perms;
	}


}
