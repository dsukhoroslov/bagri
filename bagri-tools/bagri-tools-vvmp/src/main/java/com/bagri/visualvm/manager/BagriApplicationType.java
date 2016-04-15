package com.bagri.visualvm.manager;

import com.sun.tools.visualvm.application.type.ApplicationType;
import java.awt.Image;
import org.openide.util.Utilities;

public class BagriApplicationType extends ApplicationType {

    public static final String BAGRI_MANAGER = "Bagri Manager";
    public static final String BAGRI_SERVER = "Bagri Server";
	
    private final int appPID;
    private final boolean isAdmin;

    public BagriApplicationType(int pid, boolean isAdmin) {
        appPID = pid;
        this.isAdmin = isAdmin;
    }

    @Override
    public String getName() {
    	if (isAdmin) {
    		return BAGRI_MANAGER;
    	}
    	return BAGRI_SERVER;
    }

    @Override
    public String getVersion() {
        return "0.7.1";
    }

    @Override
    public String getDescription() {
    	if (isAdmin) {
    		return "Bagri Management & Monitoring Server";
    	}
    	return "Bagri Cache Server";
    }

    @Override
    public Image getIcon() {
    	// TODO: add Bagri Icon here
        return Utilities.loadImage("com/sun/tools/visualvm/core/ui/resources/snapshot.png", true);
    }

}
