package com.bagri.visualvm.manager.service;

import java.util.logging.Logger;

import javax.management.MBeanServerConnection;

public class DefaultServiceProvider implements BagriServiceProvider {
	
	private static final Logger LOGGER = Logger.getLogger(DefaultServiceProvider.class.getName());
	
	private AccessServiceProvider accService;
	private ClusterServiceProvider clService;
	
	private static DefaultServiceProvider _INSTANCE = null; 
	
	private DefaultServiceProvider(MBeanServerConnection mbsc) {
		//
		accService = new AccessServiceProvider(mbsc);
		clService = new ClusterServiceProvider(mbsc);
		
		// don't see why would we need this ..
        // Register listener for MBean registration/unregistration
        //try {
        //    mbsc.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this, null, null);
        //} catch (InstanceNotFoundException e) {
            // Should never happen because the MBeanServerDelegate
            // is always present in any standard MBeanServer
            //
        //    LOGGER.throwing(BagriMainPanel.class.getName(), "BagriMainPanel", e);
        //} catch (IOException e) {
        //    LOGGER.throwing(BagriMainPanel.class.getName(), "BagriMainPanel", e);
        //}
	}
	
	public static BagriServiceProvider getInstance(MBeanServerConnection mbsc) {
		if (_INSTANCE == null) {
			_INSTANCE = new DefaultServiceProvider(mbsc); 
		}
		return _INSTANCE;
	}
	
	@Override
    public void close() {
		accService.close();
		clService.close();
		_INSTANCE = null;
    }

	@Override
	public ClusterManagementService getClusterManagement() {
		return clService;
	}
	
	@Override
	public SchemaManagementService getSchemaManagement() {
		return clService;
	}
	
	@Override
	public UserManagementService getUserManagement() {
		return accService;
	}

}
