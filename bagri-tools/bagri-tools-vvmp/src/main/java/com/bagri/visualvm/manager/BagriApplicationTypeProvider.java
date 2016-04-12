package com.bagri.visualvm.manager;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.type.ApplicationType;
import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;
import com.sun.tools.visualvm.application.type.MainClassApplicationTypeFactory;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;

public class BagriApplicationTypeProvider extends MainClassApplicationTypeFactory {

	private static BagriApplicationTypeProvider INSTANCE = new BagriApplicationTypeProvider();

    @Override
    public ApplicationType createApplicationTypeFor(Application app, Jvm jvm, String mainClass) {

        if ("com.bagri.xdm.cache.hazelcast.XDMCacheServer".equals(mainClass)) {
        	boolean isAdmin = isBargiAdminApp(app); 
            return new BagriApplicationType(app.getPid(), isAdmin);
        }
        return null;
    }

    static void initialize() {
        ApplicationTypeFactory.getDefault().registerProvider(INSTANCE);
    }

    static void uninitialize() {
        ApplicationTypeFactory.getDefault().unregisterProvider(INSTANCE);
    }
    
    
    static boolean isBargiAdminApp(Application application) {
        ObjectInstance oi = null;
        try {
            JmxModel jmx = JmxModelFactory.getJmxModelFor(application);
            MBeanServerConnection mbsc = jmx.getMBeanServerConnection();
            oi = mbsc.getObjectInstance(new ObjectName("com.bagri.xdm:type=Management,name=ClusterManagement"));
        } catch (Exception e) {
        }
        return oi != null;
    }

}

