package com.bagri.tools.vvm.manager;

import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.type.ApplicationType;
import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;
import com.sun.tools.visualvm.application.type.MainClassApplicationTypeFactory;
import com.sun.tools.visualvm.core.datasupport.DataRemovedListener;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;

public class BagriApplicationTypeProvider extends MainClassApplicationTypeFactory implements DataRemovedListener<Application> {

	private static BagriApplicationTypeProvider instance = new BagriApplicationTypeProvider();

	private Set<Application> admins = new HashSet<>();
	
    @Override
    public ApplicationType createApplicationTypeFor(Application app, Jvm jvm, String mainClass) {
        if ("com.bagri.xdm.cache.hazelcast.BagriCacheServer".equals(mainClass)) {
        	String role = jvm.getSystemProperties().getProperty("xdm.cluster.node.role", "");
        	boolean isAdmin = "admin".equalsIgnoreCase(role);
        	if (isAdmin) {
        		admins.add(app);
        		app.notifyWhenRemoved(this);
        	}
            return new BagriApplicationType(app.getPid(), isAdmin);
        }
        return null;
    }
    
	@Override
	public void dataRemoved(Application source) {
		admins.remove(source);
	}

    private boolean isAdminApp(Application app) {
    	return admins.contains(app);
    }

    static void initialize() {
        ApplicationTypeFactory.getDefault().registerProvider(instance);
    }

    static void uninitialize() {
        ApplicationTypeFactory.getDefault().unregisterProvider(instance);
    }
    
    
    static boolean isBargiAdminApp(Application application) {
    	return instance.isAdminApp(application);
    }

}

