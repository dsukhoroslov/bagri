package com.bagri.tools.vvm.manager;

import com.sun.tools.visualvm.application.type.ApplicationType;
import java.awt.Image;
import org.openide.util.Utilities;

import static com.bagri.core.Constants.bg_version;

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
        return bg_version;
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
    	return Utilities.loadImage("com/bagri/tools/vvm/manager/bagri16x32.png", true);
    	//return Utilities.icon2Image(Icons.BAGRI_MANAGER_ICON);
    	//return new ImageIcon("com/bagri/visualvm/manager/bagri16.ico");
    }

}
