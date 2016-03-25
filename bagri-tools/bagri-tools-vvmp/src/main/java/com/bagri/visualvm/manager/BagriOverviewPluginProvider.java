package com.bagri.visualvm.manager;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;
import com.sun.tools.visualvm.application.views.ApplicationViewsSupport;
import com.sun.tools.visualvm.core.ui.DataSourceViewPlugin;
import com.sun.tools.visualvm.core.ui.DataSourceViewPluginProvider;

public class BagriOverviewPluginProvider extends DataSourceViewPluginProvider<Application> {

    protected DataSourceViewPlugin createPlugin(Application application) {
        return new BagriOverview(application);
    }

    protected boolean supportsPluginFor(Application application) {
    	return BagriApplicationTypeProvider.isBargiAdminApp(application);
    }

    static void initialize() {
        ApplicationViewsSupport.sharedInstance().getOverviewView().registerPluginProvider(new BagriOverviewPluginProvider());
    }

    static void uninitialize() {
        ApplicationViewsSupport.sharedInstance().getOverviewView().unregisterPluginProvider(new BagriOverviewPluginProvider());
    }
   
}
