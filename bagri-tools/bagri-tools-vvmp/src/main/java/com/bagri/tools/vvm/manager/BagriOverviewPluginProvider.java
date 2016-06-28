package com.bagri.tools.vvm.manager;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;
import com.sun.tools.visualvm.application.views.ApplicationViewsSupport;
import com.sun.tools.visualvm.core.ui.DataSourceViewPlugin;
import com.sun.tools.visualvm.core.ui.DataSourceViewPluginProvider;
import com.sun.tools.visualvm.core.ui.DataSourceViewProvider;

public class BagriOverviewPluginProvider extends DataSourceViewPluginProvider<Application> {

	private static BagriOverviewPluginProvider instance = new BagriOverviewPluginProvider();
	
	@Override
    protected DataSourceViewPlugin createPlugin(Application application) {
        return new BagriOverview(application);
    }

	@Override
    protected boolean supportsPluginFor(Application application) {
    	return BagriApplicationTypeProvider.isBargiAdminApp(application);
    }

    static void initialize() {
        ApplicationViewsSupport.sharedInstance().getOverviewView().registerPluginProvider(instance);
    }

    static void uninitialize() {
        ApplicationViewsSupport.sharedInstance().getOverviewView().unregisterPluginProvider(instance);
    }
   
}
