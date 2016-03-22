package com.bagri.visualvm.manager;

import com.sun.tools.visualvm.application.type.ApplicationType;
import java.awt.Image;
import org.openide.util.Utilities;

public class BagriApplicationType extends ApplicationType {

    protected final int appPID;

    public BagriApplicationType(int pid) {
        appPID = pid;
    }

    @Override
    public String getName() {
        return "Bagri Manager";
    }

    @Override
    public String getVersion() {
        return "0.6.1";
    }

    @Override
    public String getDescription() {
        return "Application type for Bagri Management Tool";
    }

    @Override
    public Image getIcon() {
    	// TODO: add Bagri Icon here
        return Utilities.loadImage("com/sun/tools/visualvm/core/ui/resources/snapshot.png", true);
    }

}
