package com.bagri.visualvm.manager;

import static com.bagri.visualvm.manager.BagriApplicationType.BAGRI_MANAGER;

import java.awt.Component;
import java.awt.Container;
import java.util.logging.Logger;

import com.bagri.visualvm.manager.service.BagriServiceProvider;
import com.bagri.visualvm.manager.service.DefaultServiceProvider;
import com.bagri.visualvm.manager.ui.BagriMainPanel;
import com.bagri.visualvm.manager.util.Icons;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.datasupport.DataRemovedListener;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;
import org.openide.util.WeakListeners;

import javax.management.MBeanServerConnection;
import javax.swing.*;

public class BagriManagerView extends DataSourceView implements DataRemovedListener<Application> {
	
	private static final Logger LOGGER = Logger.getLogger(BagriManagerView.class.getName());
    private static final String NOT_CONNECTED = "Not Connected";

    private Application application;
    private BagriMainPanel mainPanel;

    public BagriManagerView(Application application) {
        super(application, BAGRI_MANAGER, Icons.MAIN_ICON.getImage(), 60, false);
        this.application = application;
        application.notifyWhenRemoved(this);
    }

    @Override
    protected void removed() {
        if (mainPanel != null) {
            mainPanel.dispose();
        }
    }

    @Override
    protected DataViewComponent createComponent() {
        DataViewComponent dvc;
        JmxModel jmx = JmxModelFactory.getJmxModelFor(application);
        if (jmx == null || jmx.getConnectionState() != JmxModel.ConnectionState.CONNECTED) {
            JTextArea textArea = new JTextArea();
            textArea.setBorder(BorderFactory.createEmptyBorder(25, 9, 9, 9));
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setText(NOT_CONNECTED);
            dvc = new DataViewComponent(
                    new DataViewComponent.MasterView(BAGRI_MANAGER, null, textArea), // NOI18N
                    new DataViewComponent.MasterViewConfiguration(true));
        } else {
            DataViewComponent.MasterView monitoringMasterView = new DataViewComponent.MasterView(BAGRI_MANAGER, null, new JLabel(" ")); // NOI18N
            DataViewComponent.MasterViewConfiguration monitoringMasterConfiguration = new DataViewComponent.MasterViewConfiguration(false);
            dvc = new DataViewComponent(monitoringMasterView, monitoringMasterConfiguration);
            dvc.configureDetailsView(new DataViewComponent.DetailsViewConfiguration(0.33, 0, -1, -1, -1, -1));
            dvc.configureDetailsArea(new DataViewComponent.DetailsAreaConfiguration(BAGRI_MANAGER, false), DataViewComponent.TOP_LEFT); // NOI18N
            MBeanServerConnection mbsc = jmx.getMBeanServerConnection();
            LOGGER.info("got connection: " + mbsc + "; className: " + mbsc.getClass().getName());
        	BagriServiceProvider bsp = DefaultServiceProvider.getInstance(mbsc);
            mainPanel = new BagriMainPanel(bsp);
            jmx.addPropertyChangeListener(WeakListeners.propertyChange(mainPanel, jmx));
            dvc.addDetailsView(new DataViewComponent.DetailsView("", null, 10, mainPanel, null), DataViewComponent.TOP_LEFT); // NOI18N
        }
        return dvc;
    }
    
    public void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container)component, enable);
            }
        }
        container.setEnabled(enable);
    }    

	@Override
	public void dataRemoved(Application source) {
		LOGGER.info("dataRemoved; got removed notification from source: " + source);
		application = null;
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                enableComponents(mainPanel, false);
            }
        });
	}
}
