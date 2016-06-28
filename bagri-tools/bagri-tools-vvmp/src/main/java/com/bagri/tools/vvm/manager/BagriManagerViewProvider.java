package com.bagri.tools.vvm.manager;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.DataSourceViewProvider;
import com.sun.tools.visualvm.core.ui.DataSourceViewsManager;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class BagriManagerViewProvider extends DataSourceViewProvider<Application> {

	private static BagriManagerViewProvider instance = new BagriManagerViewProvider();

    @Override
    protected boolean supportsViewFor(Application application) {
    	return BagriApplicationTypeProvider.isBargiAdminApp(application);
    }

    @Override
    protected DataSourceView createView(Application application) {
        return new BagriManagerView(application);
    }

    static void initialize() {
        DataSourceViewsManager.sharedInstance().addViewProvider(instance, Application.class);
    }

    static void uninitialize() {
        DataSourceViewsManager.sharedInstance().removeViewProvider(instance);
    }
}
