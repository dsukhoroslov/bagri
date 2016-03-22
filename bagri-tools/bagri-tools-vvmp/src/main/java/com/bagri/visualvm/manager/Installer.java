package com.bagri.visualvm.manager;

import org.openide.modules.ModuleInstall;

import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        BagriApplicationTypeProvider.initialize();
        BagriManagerViewProvider.initialize();
        BagriOverviewPluginProvider.initialize();
    }

    @Override
    public void uninstalled() {
        BagriApplicationTypeProvider.uninitialize();
        BagriManagerViewProvider.uninitialize();
        BagriOverviewPluginProvider.uninitialize();
    }
}
