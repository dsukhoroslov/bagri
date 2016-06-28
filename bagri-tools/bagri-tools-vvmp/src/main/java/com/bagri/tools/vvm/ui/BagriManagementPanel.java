package com.bagri.tools.vvm.ui;

import javax.swing.*;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.event.EventBus;
import com.bagri.tools.vvm.model.*;
import com.bagri.tools.vvm.service.ServiceException;
import com.bagri.tools.vvm.util.ErrorUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Logger;

public class BagriManagementPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(BagriManagementPanel.class.getName());
    private static final ManagedSection[] SECTIONS = new ManagedSection[] {
            new ManagedSection(ClusterManagement.CLUSTER_MANAGEMENT, ClusterManagement.CLUSTER_MANAGEMENT, "Manages cluster state"),
            new ManagedSection(SchemaManagement.SCHEMA_MANAGEMENT, SchemaManagement.SCHEMA_MANAGEMENT, "Manages schemas and schema properties"),
            new ManagedSection(UserManagement.USER_MANAGEMENT, UserManagement.USER_MANAGEMENT, "Allows to manage users and user-roles")
    };
    private final EventBus<ApplicationEvent> eventBus;
    private JTabbedPane tabbedPane;
    private XTable grid;

    public BagriManagementPanel(EventBus<ApplicationEvent> eventBus) {
        super(new GridLayout(1, 1));
        this.eventBus = eventBus;

        setBorder(BorderFactory.createEmptyBorder());
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(BagriManager.BAGRI_MANAGER, createMainPanel());
        add(tabbedPane);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel createMainPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        // Column configs
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig c = new ColumnConfig();
        c.setHeader("Management Section");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Description");
        c.setColumnClass(String.class);
        c.setWidth(45);
        c.setResizable(true);
        configs.add(c);
        grid = new XTable(configs, new GridDataLoader() {
            @Override
            public java.util.List<GridRow> loadData() {
                java.util.List<GridRow> rows = new ArrayList<GridRow>();
                for (ManagedSection section : SECTIONS) {
                    rows.add(new DefaultGridRow(section.getSectionKey(), new Object[]{section.getSectionName(), section.getSectionDescription()}));
                }
                return rows;
            }
        });
        grid.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >=2 ){
                    onDblClick();
                }
            }
        });
        panel.add(new JScrollPane(grid), BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            grid.clearSelection();
            grid.getTableHeader().setEnabled(false);
            grid.setEnabled(false);
            for (Component c : tabbedPane.getComponents()) {
                c.setEnabled(false);
            }
            tabbedPane.setEnabled(false);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!grid.isLoaded()) {
            grid.reload();
        }
    }

    private void onDblClick() {
        int selectedIndex = grid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < grid.getModel().getRowCount()) {
            GridRow row = ((GridTableModel) grid.getModel()).getRow(selectedIndex);
            eventBus.fireEvent(new ApplicationEvent(row.getId(), BagriManager.MANAGEMENT_SECTION_SELECTED));
        }
    }

    private static class ManagedSection {
        private String sectionKey;
        private String sectionName;
        private String sectionDescription;

        private ManagedSection(String sectionKey, String sectionName, String sectionDescription) {
            this.sectionKey = sectionKey;
            this.sectionName = sectionName;
            this.sectionDescription = sectionDescription;
        }

        public String getSectionKey() {
            return sectionKey;
        }

        public void setSectionKey(String sectionKey) {
            this.sectionKey = sectionKey;
        }

        public String getSectionName() {
            return sectionName;
        }

        public void setSectionName(String sectionName) {
            this.sectionName = sectionName;
        }

        public String getSectionDescription() {
            return sectionDescription;
        }

        public void setSectionDescription(String sectionDescription) {
            this.sectionDescription = sectionDescription;
        }
    }

}
