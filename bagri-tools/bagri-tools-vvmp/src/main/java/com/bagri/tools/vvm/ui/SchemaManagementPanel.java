package com.bagri.tools.vvm.ui;

import static com.bagri.core.Constants.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.event.EventBus;
import com.bagri.tools.vvm.event.EventHandler;
import com.bagri.tools.vvm.model.ClusterManagement;
import com.bagri.tools.vvm.model.ColumnConfig;
import com.bagri.tools.vvm.model.DefaultGridRow;
import com.bagri.tools.vvm.model.GridDataLoader;
import com.bagri.tools.vvm.model.GridRow;
import com.bagri.tools.vvm.model.Schema;
import com.bagri.tools.vvm.model.SchemaManagement;
import com.bagri.tools.vvm.service.SchemaManagementService;
import com.bagri.tools.vvm.service.ServiceException;

public class SchemaManagementPanel extends JPanel {
	
    private static final Logger LOGGER = Logger.getLogger(SchemaManagementPanel.class.getName());

    private String schemaName;
    private final EventBus eventBus;
    private final SchemaManagementService schemaService;

    public SchemaManagementPanel(String schemaName, SchemaManagementService schemaService, EventBus eventBus) {
        super(new BorderLayout());
        this.eventBus = eventBus;
        this.schemaService = schemaService;
       	this.schemaName = schemaName;

        JPanel left = createClusterPanel();
        JPanel right = createPropertiesPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);        
    }
    
    private JPanel createClusterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel clPanel = new JPanel(new BorderLayout());
        clPanel.setBorder(BorderFactory.createTitledBorder("Cluster state "));
        JPanel stPanel = new JPanel(new BorderLayout());
        stPanel.setPreferredSize(new Dimension(400, 25));
        JLabel clState = new JLabel(" INACTIVE ");
        stPanel.add(clState, BorderLayout.WEST);
        JButton clStartStop = new JButton("Start/stop cluster");
        clStartStop.setToolTipText("start/stop schema cluster");
        clStartStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startStopCluster();
            }
        });
        stPanel.add(clStartStop, BorderLayout.EAST);
        clPanel.add(stPanel, BorderLayout.NORTH);
        
        ArrayList<ColumnConfig> clConfigs = new ArrayList<ColumnConfig>();
        ColumnConfig c = new ColumnConfig();
        c.setHeader("Address");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        clConfigs.add(c);
        c = new ColumnConfig();
        c.setHeader("Port");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        clConfigs.add(c);
        c = new ColumnConfig();
        c.setHeader("Size");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        clConfigs.add(c);
        c = new ColumnConfig();
        c.setHeader("State");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        clConfigs.add(c);
        XTable nodeGrid = new XTable(clConfigs, new GridDataLoader() {
            @Override
            public List<GridRow> loadData() {
            	return getClusterRows();
            }
        });
        nodeGrid.reload();
        JScrollPane nodeScroller = new JScrollPane(nodeGrid);
        clPanel.add(nodeScroller, BorderLayout.CENTER);

        JPanel btPanel = new JPanel(new BorderLayout());
        btPanel.setPreferredSize(new Dimension(400, 50));

        JButton clAddNode = new JButton("Add nodes");
        clAddNode.setToolTipText("add nodes to schema cluster");
        clAddNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNodes();
            }
        });
        btPanel.add(clAddNode, BorderLayout.WEST);
        JButton clDelNode = new JButton("Remove nodes");
        clDelNode.setToolTipText("remove nodes from schema cluster");
        clDelNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeNodes();
            }
        });
        btPanel.add(clDelNode, BorderLayout.EAST);
        
        clPanel.add(btPanel, BorderLayout.SOUTH);
        clPanel.setPreferredSize(new Dimension(400, 400));

        JPanel ppPanel = new JPanel(new BorderLayout());
        ppPanel.setBorder(BorderFactory.createTitledBorder("Population state "));
        JPanel ptPanel = new JPanel(new BorderLayout());
        ptPanel.setPreferredSize(new Dimension(400, 25));
        JLabel ppState = new JLabel(" NOT POPULATED ");
        ptPanel.add(ppState, BorderLayout.WEST);
        JButton clPopulate = new JButton("Populate cluster");
        clPopulate.setToolTipText("populate schema cluster");
        clPopulate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateCluster();
            }
        });
        ptPanel.add(clPopulate, BorderLayout.EAST);
        ppPanel.add(ptPanel, BorderLayout.NORTH);

        ArrayList<ColumnConfig> dsConfigs = new ArrayList<ColumnConfig>();
        c = new ColumnConfig();
        c.setHeader("Property");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        dsConfigs.add(c);
        c = new ColumnConfig();
        c.setHeader("Value");
        c.setColumnClass(Integer.class);
        c.setWidth(40);
        c.setResizable(true);
        dsConfigs.add(c);
        XTable dsGrid = new XTable(dsConfigs, new GridDataLoader() {
            @Override
            public List<GridRow> loadData() {
            	return getDataStoreRows();
            }
        });
        dsGrid.reload();
        JScrollPane dsScroller = new JScrollPane(dsGrid);
        ppPanel.add(dsScroller, BorderLayout.CENTER);

        JPanel dsPanel = new JPanel(new BorderLayout());
        dsPanel.setPreferredSize(new Dimension(400, 50));
        ppPanel.add(dsPanel, BorderLayout.SOUTH);
        ppPanel.setPreferredSize(new Dimension(400, 400));
        
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, clPanel, ppPanel);
		splitter.setResizeWeight(0.5);
		panel.add(splitter, BorderLayout.CENTER);        
        
        JPanel hlPanel = new JPanel(new BorderLayout());
        hlPanel.setBorder(BorderFactory.createTitledBorder("Health state "));
        hlPanel.setPreferredSize(new Dimension(400, 100));
        panel.add(hlPanel, BorderLayout.SOUTH);

        eventBus.addEventHandler(new EventHandler<ApplicationEvent>() {
            @Override
            public void handleEvent(ApplicationEvent e) {
                if (SchemaManagement.SCHEMA_HEALTH_CHANGED.equals(e.getCommand())) {
                    hlPanel.invalidate();
                }
            }
        });
        
        return panel;
    }
    
    private JPanel createPropertiesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Properties "));
        
        // Column configs
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig c = new ColumnConfig();
        c.setHeader("Property");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Value");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        XTable propsGrid = new XTable(configs, new GridDataLoader() {
            @Override
            public List<GridRow> loadData() {
            	Schema schema = getSchema();
            	if (schema == null) {
            		return null;
            	}
            	
                List<String> propNames = new ArrayList<>(schema.getProperties().stringPropertyNames());
                Collections.sort(propNames);
                List<GridRow> result = new ArrayList<GridRow>();
                for (String prop: propNames) {
                    result.add(new DefaultGridRow(prop, new Object[] {prop, schema.getProperty(prop)}));
                }
                return result;
            }
        });
        //propsGrid.addMouseListener(new MouseAdapter() {
        //    @Override
        //    public void mouseClicked(MouseEvent e) {
        //        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()>=2) {
        //            int selectedIndex = optionsGrid.getSelectionModel().getLeadSelectionIndex();
        //            if (selectedIndex >= 0 && selectedIndex < optionsGrid.getModel().getRowCount()) {
        //                GridRow row = ((GridTableModel) optionsGrid.getModel()).getRow(selectedIndex);
        //                final EditNodeOptionDialog dlg = new EditNodeOptionDialog(options.get(row.getId().toString()), owner);
        //                dlg.setSuccessListener(new ActionListener() {
        //                    @Override
        //                    public void actionPerformed(ActionEvent e) {
        //                        NodeOption op = dlg.getOption();
        //                        if (null != op.getOptionName()){
        //                            options.put(op.getOptionName(), op);
        //                            optionsGrid.reload();
        //                        }
        //                    }
        //                });
        //                dlg.setVisible(true);
        //            }
        //        }
        //    }
        //});
        propsGrid.reload();
        JScrollPane propsScroller = new JScrollPane(propsGrid);
        panel.add(propsScroller, BorderLayout.CENTER); //NORTH);
        //JSeparator propSep = new JSeparator(SwingConstants.HORIZONTAL);
        //propSep.setPreferredSize(new Dimension(200, 5));
        //panel.add(propSep, BorderLayout.CENTER);

        JPanel propMgmt = new JPanel(new GridLayout(1, 3));
        propMgmt.add(new JLabel("Set property: "));
        JTextField propVal = new JTextField();
        propMgmt.add(propVal);
        JButton propSet = new JButton("Set value");
        propSet.setToolTipText("Update property");
        propSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSchemaProperty();
            }
        });
        propMgmt.add(propSet); //CENTER);
        propMgmt.setPreferredSize(new Dimension(200, 22));
        panel.add(propMgmt, BorderLayout.SOUTH);

        eventBus.addEventHandler(new EventHandler<ApplicationEvent>() {
            @Override
            public void handleEvent(ApplicationEvent e) {
            	LOGGER.info("SchemaManagementPanel; got event: " + e);
                if (schemaName.equals(e.getSource()) && 
                	(SchemaManagement.SCHEMA_PROPERTY_CHANGED.equals(e.getCommand()) || 
                		SchemaManagement.SCHEMA_PROPERTIES_CHANGED.equals(e.getCommand()))) {
                	LOGGER.info("SchemaManagementPanel; reloading schema: " + schemaName);
                    propsGrid.reload();
                }
            }
        });
        
        return panel;
    }
    
    private Schema getSchema() {
    	try {
    		return schemaService.getSchema(schemaName);
    	} catch (ServiceException ex) {
    		LOGGER.throwing(SchemaManagementPanel.class.getName(), "getSchema", ex);
    	}
		return null;
    }
    
    private List<GridRow> getClusterRows() {
    	Schema schema = getSchema();
    	if (schema == null) {
    		return null;
    	}
    	
        List<GridRow> result = new ArrayList<GridRow>();
        String members = schema.getProperty(pn_schema_members);
        if (members != null) {
            String fPort = schema.getProperty(pn_schema_ports_first);
            String lPort = schema.getProperty(pn_schema_ports_last);
        	String[] mms = members.split(",");
        	String port;
        	for (String member: mms) {
            	String[] parts = member.split(":");
            	if (parts.length > 1) {
            		port = parts[1];
            	} else {
            		port = fPort + ".." + lPort;
            	}
                result.add(new DefaultGridRow(member, new Object[] {parts[0], port, "4G", ""}));
        	}
        }
        return result;
    }
    
    private List<GridRow> getDataStoreRows() {
    	Schema schema = getSchema();
    	if (schema == null) {
    		return null;
    	}
    	
        List<GridRow> result = new ArrayList<GridRow>();
        String stype = schema.getProperty(pn_schema_store_type);
        Properties props = schemaService.getDataStoreProperties(stype);
        if (props != null) {
	        List<String> pNames = new ArrayList<>(props.stringPropertyNames());
	        Collections.sort(pNames);
	        for (String pName: pNames) {
	        	String pVal = schema.getProperty(pName);
	        	if (pVal == null) {
	        		pVal = props.getProperty(pName);
	        	}
	            result.add(new DefaultGridRow(pName, new Object[] {pName, pVal}));
	        }
        }
        return result;
    }
    
    private void startStopCluster() {
    	//
    }
    
    private void populateCluster() {
    	//
    }
    
    private void addNodes() {
    	//
    }

    private void removeNodes() {
    	//
    }
    
    private void setSchemaProperty() {
    	//
    }
}