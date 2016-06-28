package com.bagri.tools.vvm.ui;

import java.awt.GridLayout;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.event.EventBus;
import com.bagri.tools.vvm.model.Schema;
import com.bagri.tools.vvm.model.SchemaManagement;
import com.bagri.tools.vvm.service.SchemaManagementService;

public class SchemaPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(SchemaManagementPanel.class.getName());
    private final SchemaManagementService schemaService;
    private final EventBus<ApplicationEvent> eventBus;
    private final Schema schema;
    private JTabbedPane tabbedPane;
    //private XTable grid;

    public SchemaPanel(SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus, Schema schema) {
        super(new GridLayout(1, 1));
        this.schema = schema;
        this.schemaService = schemaService;
        this.eventBus = eventBus;
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(schema.getSchemaName() + SchemaManagement.SCHEMA_DETAILS, createSchemaInfoPanel());
        tabbedPane.addTab(SchemaManagement.DOCUMENT_MANAGEMENT, createSchemaDocumentsPanel());
        tabbedPane.addTab(SchemaManagement.QUERY_MANAGEMENT, createSchemaQueryPanel());
        tabbedPane.addTab(SchemaManagement.SCHEMA_MONITORING, createSchemaMonitoringPanel());
        add(tabbedPane);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel createSchemaInfoPanel() {
        JPanel panel = new JPanel();
        return panel;
    }

    private JPanel createSchemaDocumentsPanel() {
        JPanel panel = new SchemaDocumentPanel(schema, schemaService, eventBus);
        return panel;
    }

    private JPanel createSchemaMonitoringPanel() {
    	JPanel panel = new SchemaMonitoringPanel(schema.getSchemaName(), schemaService, eventBus);
        return panel;
    }

    private JPanel createSchemaQueryPanel() {
        JPanel panel = new SchemaQueryPanel(schema, schemaService, eventBus);
        return panel;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
//        if (!enabled) {
//            grid.clearSelection();
//            grid.getTableHeader().setEnabled(false);
//            grid.setEnabled(false);
//            for (Component c : toolBar.getComponents()) {
//                c.setEnabled(false);
//            }
//            toolBar.setEnabled(false);
//            for (Component c : tabbedPane.getComponents()) {
//                c.setEnabled(false);
//            }
//            tabbedPane.setEnabled(false);
//        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
//        if (!grid.isLoaded()) {
//            grid.reload();
//        }
    }

}
