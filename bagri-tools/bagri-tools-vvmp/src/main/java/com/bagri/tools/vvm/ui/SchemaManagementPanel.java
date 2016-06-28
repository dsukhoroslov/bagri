package com.bagri.tools.vvm.ui;

import javax.management.ObjectName;
import javax.swing.*;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.event.EventBus;
import com.bagri.tools.vvm.event.EventHandler;
import com.bagri.tools.vvm.model.*;
import com.bagri.tools.vvm.service.SchemaManagementService;
import com.bagri.tools.vvm.service.ServiceException;
import com.bagri.tools.vvm.util.ErrorUtil;

import static com.bagri.tools.vvm.util.Icons.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

public class SchemaManagementPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(SchemaManagementPanel.class.getName());
    private final SchemaManagementService schemaService;
    private final EventBus<ApplicationEvent> eventBus;
    private JTabbedPane tabbedPane;
    private JToolBar schemasToolBar;
    private XTable schemasGrid;
    private JToolBar propsToolBar;
    private XTable propsGrid;

    public SchemaManagementPanel(SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus) {
        super(new GridLayout(1, 1));
        this.schemaService= schemaService;
        this.eventBus = eventBus;
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(SchemaManagement.SCHEMA_MANAGEMENT, createSchemaManagementPanel());
        tabbedPane.addTab(SchemaManagement.PROPERTIES_MANAGEMENT, createPropertiesManagementPanel());
        add(tabbedPane);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel createSchemaManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        schemasToolBar = new JToolBar();
        // "Add schema" button
        JButton addNode = new JButton("Add");
        addNode.setToolTipText("Adds new schema");
        addNode.setIcon(ADD_ICON);
        addNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddSchema();
            }
        });
        schemasToolBar.add(addNode);
        schemasToolBar.addSeparator();
        // "Edit Schema" button
        JButton editNode = new JButton("Edit");
        editNode.setToolTipText("Edit selected schema");
        editNode.setIcon(EDIT_ICON);
        editNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onEditSchema();
            }
        });
        schemasToolBar.add(editNode);
        schemasToolBar.addSeparator();
        // "Delete Schema" button
        JButton deleteNode = new JButton("Delete");
        deleteNode.setToolTipText("Delete selected schema");
        deleteNode.setIcon(DELETE_ICON);
        deleteNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDeleteSchema();
            }
        });
        schemasToolBar.add(deleteNode);
        schemasToolBar.addSeparator();
        schemasToolBar.setFloatable(false);
        panel.add(schemasToolBar, BorderLayout.PAGE_START);
        // Column configs
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig c = new ColumnConfig();
        c.setHeader("Schema Name");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Description");
        c.setColumnClass(String.class);
        c.setWidth(140);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Persistent");
        c.setColumnClass(Boolean.class);
        c.setWidth(40);
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
        c.setHeader("Active");
        c.setColumnClass(Boolean.class);
        c.setFixedWidth(45);
        c.setResizable(true);
        configs.add(c);
        schemasGrid = new XTable(configs, new GridDataLoader() {
            @Override
            public java.util.List<GridRow> loadData() {
                java.util.List<Schema> schemas;
                try {
                    schemas = schemaService.getSchemas();
                } catch (ServiceException e) {
                    ErrorUtil.showError(SchemaManagementPanel.this, e);
                    return null;
                }
                java.util.List<GridRow> rows = new ArrayList<GridRow>();
                if (null == schemas) { 
                	return rows; 
                }
                for (Schema schema : schemas) {
                    rows.add(new DefaultGridRow(schema.getObjectName(), new Object[]{
                            schema.getSchemaName()
                            , schema.getDescription()
                            , schema.isPersistent()
                            , schema.getDataFormat()
                            , schema.getState()
                            , schema.isActive()
                    }));
                }
                return rows;
            }
        });
        schemasGrid.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                    onEditSchema();
                }
            }
        });
        eventBus.addEventHandler(new EventHandler<ApplicationEvent>() {
            @Override
            public void handleEvent(ApplicationEvent e) {
                if (SchemaManagement.SCHEMA_STATE_CHANGED.equals(e.getCommand())) {
                    schemasGrid.reload();
                    invalidate();
                }
            }
        });
        panel.add(new JScrollPane(schemasGrid), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPropertiesManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        propsToolBar = new JToolBar();
        // "Add schema" button
        JButton addNode = new JButton("Add");
        addNode.setToolTipText("Add new default property");
        addNode.setIcon(ADD_ICON);
        addNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddProperty();
            }
        });
        propsToolBar.add(addNode);
        propsToolBar.addSeparator();
        // "Edit Schema" button
        JButton editNode = new JButton("Edit");
        editNode.setToolTipText("Edit selected property");
        editNode.setIcon(EDIT_ICON);
        editNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onEditProperty();
            }
        });
        propsToolBar.add(editNode);
        propsToolBar.addSeparator();
        // "Delete Schema" button
        JButton deleteNode = new JButton("Delete");
        deleteNode.setToolTipText("Delete selected property");
        deleteNode.setIcon(DELETE_ICON);
        deleteNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDeleteProperty();
            }
        });
        propsToolBar.add(deleteNode);
        propsToolBar.addSeparator();
        propsToolBar.setFloatable(false);
        panel.add(propsToolBar, BorderLayout.PAGE_START);
        // Column configs
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig c = new ColumnConfig();
        c.setHeader("Property Name");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Property Value");
        c.setColumnClass(String.class);
        c.setWidth(140);
        c.setResizable(true);
        configs.add(c);
        propsGrid = new XTable(configs, new GridDataLoader() {
            @Override
            public java.util.List<GridRow> loadData() {
                Properties properties;
                try {
                    properties = schemaService.getDefaultProperties();
                } catch (ServiceException e) {
                    ErrorUtil.showError(SchemaManagementPanel.this, e);
                    return null;
                }
                java.util.List<GridRow> rows = new ArrayList<GridRow>();
                if (null == properties || properties.isEmpty()) { return rows; }
                for (String propertyName : properties.stringPropertyNames()) {
                    rows.add(new DefaultGridRow(propertyName, new Object[]{
                            propertyName
                            , properties.get(propertyName)
                    }));
                }
                return rows;
            }
        });
        propsGrid.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                    onEditProperty();
                }
            }
        });
        eventBus.addEventHandler(new EventHandler<ApplicationEvent>() {
            @Override
            public void handleEvent(ApplicationEvent e) {
                if (SchemaManagement.SCHEMA_PROPERTIES_CHANGED.equals(e.getCommand())) {
                    propsGrid.reload();
                    invalidate();
                }
            }
        });
        panel.add(new JScrollPane(propsGrid), BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            schemasGrid.clearSelection();
            schemasGrid.getTableHeader().setEnabled(false);
            schemasGrid.setEnabled(false);
            for (Component c : schemasToolBar.getComponents()) {
                c.setEnabled(false);
            }
            schemasToolBar.setEnabled(false);
            for (Component c : tabbedPane.getComponents()) {
                c.setEnabled(false);
            }
            propsGrid.clearSelection();
            propsGrid.getTableHeader().setEnabled(false);
            propsGrid.setEnabled(false);
            for (Component c : propsToolBar.getComponents()) {
                c.setEnabled(false);
            }
            propsToolBar.setEnabled(false);
            for (Component c : tabbedPane.getComponents()) {
                c.setEnabled(false);
            }
            tabbedPane.setEnabled(false);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!schemasGrid.isLoaded()) {
            schemasGrid.reload();
        }
        if (!propsGrid.isLoaded()) {
            propsGrid.reload();
        }
    }

    // --- Event Handlers --- //
    private void onAddSchema() {
        final EditSchemaDialog dlg = new EditSchemaDialog(null, SchemaManagementPanel.this);
        dlg.setSuccessListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Schema schema = dlg.getSchema();
                        try {
                            schemaService.addSchema(schema);
                        } catch (ServiceException e1) {
                            LOGGER.throwing(SchemaManagementPanel.class.getName(), "onAddSchema", e1);
                            ErrorUtil.showError(SchemaManagementPanel.this, e1);
                        }
                        eventBus.fireEvent(new ApplicationEvent(dlg, SchemaManagement.SCHEMA_STATE_CHANGED));
                    }
                });
            }
        });
        dlg.setVisible(true);
    }

    private void onEditSchema() {
        int selectedIndex = schemasGrid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < schemasGrid.getModel().getRowCount()) {
            final GridRow row = ((GridTableModel) schemasGrid.getModel()).getRow(selectedIndex);
            Schema schema = null;
            try {
                schema = schemaService.getSchema((ObjectName) row.getId());
            } catch (ServiceException e) {
                LOGGER.throwing(SchemaManagementPanel.class.getName(), "onEditSchema", e);
                ErrorUtil.showError(SchemaManagementPanel.this, e);
            }
            final EditSchemaDialog dlg = new EditSchemaDialog(schema, SchemaManagementPanel.this);
            dlg.setSuccessListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Schema updated = dlg.getSchema();
                            try {
                                schemaService.saveSchema(updated);
                            } catch (ServiceException e1) {
                                LOGGER.throwing(SchemaManagementPanel.class.getName(), "onEditSchema", e1);
                                ErrorUtil.showError(SchemaManagementPanel.this, e1);
                            }
                            eventBus.fireEvent(new ApplicationEvent(dlg, SchemaManagement.SCHEMA_STATE_CHANGED));
                        }
                    });
                }
            });
            dlg.setVisible(true);
        }
    }

    private void onDeleteSchema() {
        int selectedIndex = schemasGrid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < schemasGrid.getModel().getRowCount()) {
            final GridRow row = ((GridTableModel) schemasGrid.getModel()).getRow(selectedIndex);
            int n = JOptionPane.showConfirmDialog(
                    SchemaManagementPanel.this,
                    "Are you sure you want to delete selected schema \"" + row.getValueAt(0) + "\"?",
                    "Confirm deletion",
                    JOptionPane.YES_NO_OPTION);
            if (JOptionPane.YES_OPTION == n) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Schema schema = schemaService.getSchema((ObjectName)row.getId());
                            schemaService.deleteSchema(schema);
                        } catch (ServiceException e1) {
                            LOGGER.throwing(SchemaManagementPanel.class.getName(), "onDeleteSchema", e1);
                            ErrorUtil.showError(SchemaManagementPanel.this, e1);
                        }
                        eventBus.fireEvent(new ApplicationEvent(SchemaManagementPanel.this, SchemaManagement.SCHEMA_STATE_CHANGED));
                    }
                });
            }
        }
    }

    private void onAddProperty() {
        final EditPropertyDialog dlg = new EditPropertyDialog(null, "Default Property", SchemaManagementPanel.this);
        dlg.setSuccessListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Property prop = dlg.getProperty();
                        if (null != prop.getPropertyName()) {
                            try {
                                schemaService.setDefaultProperty(prop);
                            } catch (ServiceException e1) {
                                LOGGER.throwing(SchemaManagementPanel.class.getName(), "onAddDefaultProperty", e1);
                                ErrorUtil.showError(SchemaManagementPanel.this, e1);
                            }
                            eventBus.fireEvent(new ApplicationEvent(dlg, SchemaManagement.SCHEMA_PROPERTIES_CHANGED));
                        }
                    }
                });
            }
        });
        dlg.setVisible(true);
    }

    private void onEditProperty() {
        int selectedIndex = propsGrid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < propsGrid.getModel().getRowCount()) {
            final GridRow row = ((GridTableModel) propsGrid.getModel()).getRow(selectedIndex);
            final EditPropertyDialog dlg = new EditPropertyDialog(new Property((String) row.getValueAt(0), (String) row.getValueAt(1)), "Default Property", SchemaManagementPanel.this);
            dlg.setSuccessListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Property prop = dlg.getProperty();
                            try {
                                schemaService.setDefaultProperty(prop);
                            } catch (ServiceException e1) {
                                LOGGER.throwing(SchemaManagementPanel.class.getName(), "onEditDefaultProperty", e1);
                                ErrorUtil.showError(SchemaManagementPanel.this, e1);
                            }
                            eventBus.fireEvent(new ApplicationEvent(dlg, SchemaManagement.SCHEMA_PROPERTIES_CHANGED));
                        }
                    });
                }
            });
            dlg.setVisible(true);
        }
    }

    private void onDeleteProperty() {
        int selectedIndex = propsGrid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < propsGrid.getModel().getRowCount()) {
            final GridRow row = ((GridTableModel) propsGrid.getModel()).getRow(selectedIndex);
            int n = JOptionPane.showConfirmDialog(
                    SchemaManagementPanel.this,
                    "Are you sure you want to delete selected property \"" + row.getValueAt(0) + "\"?",
                    "Confirm deletion",
                    JOptionPane.YES_NO_OPTION);
            if (JOptionPane.YES_OPTION == n) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            schemaService.setDefaultProperty(new Property((String) row.getId(), null));
                        } catch (ServiceException e1) {
                            LOGGER.throwing(SchemaManagementPanel.class.getName(), "onDeleteDefaultProperty", e1);
                            ErrorUtil.showError(SchemaManagementPanel.this, e1);
                        }
                        eventBus.fireEvent(new ApplicationEvent(SchemaManagementPanel.this, SchemaManagement.SCHEMA_PROPERTIES_CHANGED));
                    }
                });
            }
        }
    }

}
