package com.bagri.tools.vvm.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.type.ApplicationType;
import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;
import com.sun.tools.visualvm.application.type.MainClassApplicationTypeFactory;
import com.sun.tools.visualvm.core.datasupport.DataRemovedListener;

public class BagriApplicationTypeProvider extends MainClassApplicationTypeFactory implements DataRemovedListener<Application> {

	private static final Logger LOGGER = Logger.getLogger(BagriApplicationTypeProvider.class.getName());
	private static BagriApplicationTypeProvider instance = new BagriApplicationTypeProvider();

	private Set<Application> admins = new HashSet<>();
	
    @Override
    public ApplicationType createApplicationTypeFor(Application app, Jvm jvm, String mainClass) {
        LOGGER.info("create app: " + app + "; jvm: " + jvm + "; main: " + mainClass);
        if ("com.bagri.server.hazelcast.BagriCacheServer".equals(mainClass)) {
        	String role = jvm.getSystemProperties().getProperty("bdb.cluster.node.role", "");
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
        LOGGER.info("check app: " + app);
        // JmxApplication [id: service:jmx:rmi:///jndi/rmi://172.20.3.10:3430/jmxrmi]
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

