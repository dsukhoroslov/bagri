package com.bagri.tools.vvm.ui;

import javax.management.ObjectName;
import javax.swing.*;

import com.bagri.tools.vvm.event.*;
import com.bagri.tools.vvm.model.*;
import com.bagri.tools.vvm.service.ClusterManagementService;
import com.bagri.tools.vvm.service.ServiceException;
import com.bagri.tools.vvm.util.ErrorUtil;

import static com.bagri.tools.vvm.util.Icons.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.logging.Logger;

public class ClusterManagementPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(ClusterManagementPanel.class.getName());
    private final ClusterManagementService clusterService;
    private final EventBus<ApplicationEvent> eventBus;
    private JTabbedPane tabbedPane;
    private JToolBar toolBar;
    private XTable grid;

    public ClusterManagementPanel(ClusterManagementService clusterService, EventBus<ApplicationEvent> eventBus) {
        super(new GridLayout(1, 1));
        this.clusterService = clusterService;
        this.eventBus = eventBus;
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(ClusterManagement.CLUSTER_MANAGEMENT, createMainPanel());
        add(tabbedPane);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel createMainPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        toolBar = new JToolBar();
        // "Add Node" button
        JButton addNode = new JButton("Add");
        addNode.setToolTipText("Adds new node");
        addNode.setIcon(ADD_ICON);
        addNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAdd();
            }
        });
        toolBar.add(addNode);
        toolBar.addSeparator();
        // "Edit Node" button
        JButton editNode = new JButton("Edit");
        editNode.setToolTipText("Edit selected node");
        editNode.setIcon(EDIT_ICON);
        editNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onEdit();
            }
        });
        toolBar.add(editNode);
        toolBar.addSeparator();
        // "Delete Node" button
        JButton deleteNode = new JButton("Delete");
        deleteNode.setToolTipText("Delete selected node");
        deleteNode.setIcon(DELETE_ICON);
        deleteNode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDelete();
            }
        });
        toolBar.add(deleteNode);
        toolBar.addSeparator();
        toolBar.setFloatable(false);
        panel.add(toolBar, BorderLayout.PAGE_START);
        // Column configs
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig c = new ColumnConfig();
        c.setHeader("Name");
        c.setColumnClass(String.class);
        c.setWidth(45);
        c.setResizable(true);
        configs.add(c);
        grid = new XTable(configs, new GridDataLoader() {
            @Override
            public java.util.List<GridRow> loadData() {
                java.util.List<Node> nodes;
                try {
                    nodes = clusterService.getNodes();
                } catch (ServiceException e) {
                    ErrorUtil.showError(ClusterManagementPanel.this, e);
                    return null;
                }
                java.util.List<GridRow> rows = new ArrayList<GridRow>();
                for (Node n : nodes) {
                    rows.add(new DefaultGridRow(n.getObjectName(), new Object[]{n.getName()}));
                }
                return rows;
            }
        });
        grid.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >=2 ){
                    onEdit();
                }
            }
        });
        eventBus.addEventHandler(new EventHandler<ApplicationEvent>() {
            @Override
            public void handleEvent(ApplicationEvent e) {
                if (ClusterManagement.CLUSTER_STATE_CHANGED.equals(e.getCommand())) {
                    grid.reload();
                    invalidate();
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
            for (Component c : toolBar.getComponents()) {
                c.setEnabled(false);
            }
            toolBar.setEnabled(false);
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

    // --- Event Handlers --- //
    private void onAdd() {
        final EditNodeDialog dlg = new EditNodeDialog(null, ClusterManagementPanel.this);
        dlg.setSuccessListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            clusterService.addNode(dlg.getNode());
                        } catch (ServiceException e1) {
                            LOGGER.throwing(ClusterManagementPanel.class.getName(), "onAddNode", e1);
                            ErrorUtil.showError(ClusterManagementPanel.this, e1);
                        }
                        eventBus.fireEvent(new ApplicationEvent(dlg, ClusterManagement.CLUSTER_STATE_CHANGED));
                    }
                });
            }
        });
        dlg.setVisible(true);
    }

    private void onEdit() {
        int selectedIndex = grid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < grid.getModel().getRowCount()) {
            GridRow row = ((GridTableModel) grid.getModel()).getRow(selectedIndex);
            Node node;
            try {
                node = clusterService.getNode((ObjectName) row.getId());
            } catch (ServiceException e1) {
                LOGGER.throwing(ClusterManagementPanel.class.getName(), "onEditNode", e1);
                ErrorUtil.showError(ClusterManagementPanel.this, e1);
                return;
            }
            final EditNodeDialog dlg = new EditNodeDialog(node, ClusterManagementPanel.this);
            dlg.setSuccessListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                clusterService.saveNode(dlg.getNode());
                            } catch (ServiceException e1) {
                                LOGGER.throwing(ClusterManagementPanel.class.getName(), "onEditNode", e1);
                                ErrorUtil.showError(ClusterManagementPanel.this, e1);
                                return;
                            }
                            eventBus.fireEvent(new ApplicationEvent(dlg, ClusterManagement.CLUSTER_STATE_CHANGED));
                        }
                    });
                }
            });
            dlg.setVisible(true);
        }
    }

    private void onDelete() {
        int selectedIndex = grid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < grid.getModel().getRowCount()) {
            final GridRow row = ((GridTableModel) grid.getModel()).getRow(selectedIndex);
            int n = JOptionPane.showConfirmDialog(
                    ClusterManagementPanel.this,
                    "Are you sure you want to delete selected node \"" + row.getValueAt(0) + "\"?",
                    "Confirm deletion",
                    JOptionPane.YES_NO_OPTION);
            if (JOptionPane.YES_OPTION == n) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Node node = clusterService.getNode((ObjectName) row.getId());
                            clusterService.deleteNode(node);
                        } catch (ServiceException e1) {
                            LOGGER.throwing(ClusterManagementPanel.class.getName(), "onDeleteNode", e1);
                            ErrorUtil.showError(ClusterManagementPanel.this, e1);
                        }
                        eventBus.fireEvent(new ApplicationEvent(ClusterManagementPanel.this, ClusterManagement.CLUSTER_STATE_CHANGED));
                    }
                });
            }
        }
    }
}
