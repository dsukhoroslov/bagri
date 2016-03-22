package com.bagri.visualvm.manager;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.type.ApplicationType;
import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;
import com.sun.tools.visualvm.application.type.MainClassApplicationTypeFactory;
import com.sun.tools.visualvm.application.views.ApplicationViewsSupport;

public class BagriApplicationTypeProvider extends MainClassApplicationTypeFactory {

	private static BagriApplicationTypeProvider INSTANCE = new BagriApplicationTypeProvider();

    @Override
    public ApplicationType createApplicationTypeFor(Application app, Jvm jvm, String mainClass) {

        //TODO: Do this for admin node only !?:
        if ("com.bagri.xdm.cache.hazelcast.XDMCacheServer".equals(mainClass)) {
            return new BagriApplicationType(app.getPid());
        }
        return null;
    }

    static void initialize() {
        ApplicationTypeFactory.getDefault().registerProvider(INSTANCE);
    }

    static void uninitialize() {
        ApplicationTypeFactory.getDefault().unregisterProvider(INSTANCE);
    }
    
}

