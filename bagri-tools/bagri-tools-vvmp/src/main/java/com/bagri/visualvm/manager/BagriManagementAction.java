package com.bagri.visualvm.manager;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;
import com.sun.tools.visualvm.core.ui.actions.SingleDataSourceAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JOptionPane;

public class BagriManagementAction extends SingleDataSourceAction<Application> {

    public BagriManagementAction() {
        super(Application.class);
        putValue(Action.NAME, "Discover Bagri clusters");
        putValue(Action.SHORT_DESCRIPTION, "Search for Bagri XDM nodes");
    }

    @Override
    protected void actionPerformed(Application application, ActionEvent arg1) {
        JOptionPane.showMessageDialog(null, application.getPid());
    }

    //Here you can determine whether the menu item is enabled,
    //depending on the data source type that is selected. In this
    //example, the menu item is enabled for all types within
    //the current data source:
    @Override
    protected boolean isEnabled(Application application) {
        if (ApplicationTypeFactory.getApplicationTypeFor(application) instanceof BagriApplicationType) {
            return true;
        }
        return false;
    }

}
