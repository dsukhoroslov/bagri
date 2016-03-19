package com.bagri.visualvm.manager;

import org.openide.modules.ModuleInstall;

import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;

public class Installer extends ModuleInstall {

	private static BagriApplicationTypeProvider INSTANCE = new BagriApplicationTypeProvider();

    @Override
    public void restored() {
        BagriManagerViewProvider.initialize();
        ApplicationTypeFactory.getDefault().registerProvider(INSTANCE);
        BagriOverviewPluginProvider.initialize();
    }

    @Override
    public void uninstalled() {
        BagriManagerViewProvider.unregister();
        ApplicationTypeFactory.getDefault().unregisterProvider(INSTANCE);
        BagriOverviewPluginProvider.uninitialize();
    }
}
