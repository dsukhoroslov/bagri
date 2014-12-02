package com.bagri.visualvm.manager;

import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        BagriManagerViewProvider.initialize();
    }

    @Override
    public void uninstalled() {
        BagriManagerViewProvider.unregister();
    }
}
