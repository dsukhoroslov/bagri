package com.bagri.xdm.system;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The adapter converting Collection of permissions to Map of permissions
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XDMPermissionsAdapter extends XmlAdapter<XDMPermissions, Map<String, XDMPermission>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XDMPermissions marshal(Map<String, XDMPermission> perms) throws Exception {
	    XDMPermissions xdmPerms = new XDMPermissions();
	    for (XDMPermission xdmPerm : perms.values()) {
	    	xdmPerms.addPermission(xdmPerm);
	    }
	    return xdmPerms;	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, XDMPermission> unmarshal(XDMPermissions xdmPerms) throws Exception {
		Map<String, XDMPermission> perms = new HashMap<String, XDMPermission>();
	    for (XDMPermission xdmPerm : xdmPerms.permissions()) {
	    	perms.put(xdmPerm.getResource(), xdmPerm);
	    }
	    return perms;
	}


}
