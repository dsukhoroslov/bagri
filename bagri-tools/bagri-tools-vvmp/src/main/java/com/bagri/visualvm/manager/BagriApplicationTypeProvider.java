package com.bagri.visualvm.manager;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.type.ApplicationType;
import com.sun.tools.visualvm.application.type.MainClassApplicationTypeFactory;

public class BagriApplicationTypeProvider extends MainClassApplicationTypeFactory {

    @Override
    public ApplicationType createApplicationTypeFor(Application app, Jvm jvm, String mainClass) {

        //TODO: Specify the name of the application's main class here:
        if ("com.bagri.xdm.cache.hazelcast.XDMCacheServer".equals(mainClass)) {
            return new BagriApplicationType(app.getPid());
        }
        return null;
    }

}

