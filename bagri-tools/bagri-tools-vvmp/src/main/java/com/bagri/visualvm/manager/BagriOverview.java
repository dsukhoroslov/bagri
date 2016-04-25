package com.bagri.visualvm.manager;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.bagri.visualvm.manager.model.ColumnConfig;
import com.bagri.visualvm.manager.model.DefaultGridRow;
import com.bagri.visualvm.manager.model.GridDataLoader;
import com.bagri.visualvm.manager.model.GridRow;
import com.bagri.visualvm.manager.model.Schema;
import com.bagri.visualvm.manager.service.BagriServiceProvider;
import com.bagri.visualvm.manager.service.DefaultServiceProvider;
import com.bagri.visualvm.manager.service.SchemaManagementService;
import com.bagri.visualvm.manager.service.ServiceException;
import com.bagri.visualvm.manager.ui.XTable;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.ui.DataSourceViewPlugin;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;

public class BagriOverview extends DataSourceViewPlugin {

	private static final Logger LOGGER = Logger.getLogger(BagriOverview.class.getName());
	
    private XTable schemasGrid;
    private SchemaManagementService shService;
	
    BagriOverview(Application application) {
        super(application);
        JmxModel jmx = JmxModelFactory.getJmxModelFor(application);
    	BagriServiceProvider bsp = DefaultServiceProvider.getInstance(jmx.getMBeanServerConnection());
    	shService = bsp.getSchemaManagement();
    }

    public DataViewComponent.DetailsView createView(int location) {
        switch (location) {
            case DataViewComponent.TOP_RIGHT:
                JPanel panel = createOverviewPanel();
                schemasGrid.reload();
                return new DataViewComponent.DetailsView("Bagri Cluster Overview", "All known Schemas and Nodes in one place", 0, panel, null); 
                        //new ScrollableContainer(panel), null);
            default:
                return null;
        }
    }
    
    private JPanel createOverviewPanel() {

    	JPanel panel = new JPanel(new BorderLayout());
        // Column configs
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig c = new ColumnConfig();
        c.setHeader("Active");
        c.setColumnClass(String.class);
        c.setFixedWidth(50);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Schema Name");
        c.setColumnClass(String.class);
        c.setWidth(50);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Description");
        c.setColumnClass(String.class);
        c.setWidth(200);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Persistent");
        c.setColumnClass(String.class);
        c.setWidth(50);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Data format");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("State");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Members");
        c.setColumnClass(String.class);
        c.setWidth(80);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Uptime");
        c.setColumnClass(String.class);
        c.setWidth(50);
        c.setResizable(true);
        configs.add(c);

        schemasGrid = new XTable(configs, new GridDataLoader() {
            @Override
            public List<GridRow> loadData() {
                List<Schema> schemas;
                try {
                    schemas = shService.getSchemas();
                } catch (ServiceException e) {
                    //ErrorUtil.showError(SchemaManagementPanel.this, e);
                    LOGGER.severe("loadData; got exception loading schemas: " + e.getMessage()); 
                    return null;
                }
                
                //LOGGER.info("loadData; loaded schemas: " + schemas); 
                java.util.List<GridRow> rows = new ArrayList<>();
                if (schemas != null) { 
	                for (Schema schema : schemas) {
	                    rows.add(new DefaultGridRow(schema.getObjectName(), new Object[] {
	                    		schema.isActive() ? "Yes" : "No"
	                            , schema.getSchemaName()
	                            , schema.getDescription()
	                            , schema.isPersistent() ? "Yes" : "No"
	                            , schema.getDataFormat()
	                            , schema.getState()
	                            , schema.getProperties().getProperty("xdm.schema.members")
	                            , "N/A"}));
	                    if ("working".equals(schema.getState())) {
	                    	try {
	                    		List<String> hosts = shService.getWorkingHosts(schema.getSchemaName());
	                    		for (String host: hosts) {
	        	                    rows.add(new DefaultGridRow(schema.getObjectName(), new Object[] {
	        	                    		""
	        	                            , host
	        	                            , ""
	        	                            , ""
	        	                            , ""
	        	                            , ""
	        	                            , "N/A"}));
	                    		}
	                        } catch (ServiceException e) {
	                            //ErrorUtil.showError(SchemaManagementPanel.this, e);
	                            LOGGER.severe("loadData; ot exception loading hosts: " + e.getMessage()); 
	                            // skip..
	                        }
	                    }
	                }
                }
                //LOGGER.info("loadData; loaded rows: " + rows.size() + " " + rows); 
                return rows;
            }
        });
        //schemasGrid.addMouseListener(new MouseAdapter() {
        //    @Override
        //    public void mouseClicked(MouseEvent e) {
        //        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
        //            onEditSchema();
        //        }
        //    }
        //});
        //eventBus.addEventHandler(new EventHandler<ApplicationEvent>() {
        //    @Override
        //    public void handleEvent(ApplicationEvent e) {
        //        if (SchemaManagement.SCHEMA_STATE_CHANGED.equals(e.getCommand())) {
        //            schemasGrid.reload();
        //            invalidate();
        //        }
        //    }
        //});
        panel.add(new JScrollPane(schemasGrid), BorderLayout.CENTER);
        //panel.add(schemasGrid, BorderLayout.CENTER);
        return panel;
    }

}
