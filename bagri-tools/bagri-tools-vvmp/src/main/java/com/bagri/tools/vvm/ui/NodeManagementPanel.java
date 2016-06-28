package com.bagri.tools.vvm.ui;

import javax.swing.*;

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

public class NodeManagementPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(NodeManagementPanel.class.getName());
    private final ClusterManagementService clusterService;
    private XTable optionsGrid;
    private Map<String, NodeOption> options = new HashMap<String, NodeOption>();
    private Node node;
    private JToolBar toolBar;
    private JTabbedPane tabbedPane;

    public NodeManagementPanel(ClusterManagementService clusterService, Node node) {
        super(new GridLayout(1, 1));
        this.clusterService = clusterService;
        this.node = node;
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(node.getName(), createMainPanel());
        add(tabbedPane);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel createMainPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        toolBar = new JToolBar();
        // "Add Option" button
        JButton addOption = new JButton("Add");
        addOption.setToolTipText("Adds new option");
        addOption.setIcon(ADD_ICON);
        addOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAdd();
            }
        });
        toolBar.add(addOption);
        toolBar.addSeparator();
        // "Edit Option" button
        JButton editOption = new JButton("Edit");
        editOption.setToolTipText("Edit option");
        editOption.setIcon(EDIT_ICON);
        editOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onEdit();
            }
        });
        toolBar.add(editOption);
        toolBar.addSeparator();
        // "Delete Option" button
        JButton deleteOption = new JButton("Delete");
        deleteOption.setToolTipText("Delete option");
        deleteOption.setIcon(DELETE_ICON);
        deleteOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDelete();
            }
        });
        toolBar.add(deleteOption);
        toolBar.addSeparator();
        toolBar.setFloatable(false);
        panel.add(toolBar, BorderLayout.PAGE_START);

        // Column configs
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig c = new ColumnConfig();
        c.setHeader("Option name");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Option value");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        optionsGrid = new XTable(configs, new GridDataLoader() {
            @Override
            public java.util.List<GridRow> loadData() {
                java.util.List<GridRow> result = new ArrayList<GridRow>();
                try {
                    Node loadedNode = clusterService.getNode(node.getObjectName());
                    options.clear();
                    for (NodeOption option : loadedNode.getNodeOptions()) {
                        options.put(option.getOptionName(), option);
                    }
                } catch (ServiceException e) {
                    LOGGER.throwing(NodeManagementPanel.class.getName(), "loadData", e);
                    ErrorUtil.showError(NodeManagementPanel.this, e);
                    return result;
                }
                for (NodeOption option : options.values()) {
                    GridRow row = new DefaultGridRow(option.getOptionName(), new Object[] {option.getOptionName(), option.getOptionValue()});
                    result.add(row);
                }
                return result;
            }
        });
        optionsGrid.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()>=2) {
                    onEdit();
                }
            }
        });
        panel.add(new JScrollPane(optionsGrid), BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            optionsGrid.clearSelection();
            optionsGrid.getTableHeader().setEnabled(false);
            optionsGrid.setEnabled(false);
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
        if (!optionsGrid.isLoaded()) {
            optionsGrid.reload();
        }
    }

    // --- Event Handlers --- //
    private void onAdd() {
        final EditNodeOptionDialog dlg = new EditNodeOptionDialog(null, NodeManagementPanel.this);
        dlg.setSuccessListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        NodeOption op = dlg.getOption();
                        if (null != op.getOptionName()) {
                            options.put(op.getOptionName(), op);
                            node.setNodeOptions(new ArrayList<NodeOption>(options.values()));
                            try {
                                clusterService.saveNode(node);
                            } catch (ServiceException e1) {
                                LOGGER.throwing(NodeManagementPanel.class.getName(), "onAddOption", e1);
                                ErrorUtil.showError(NodeManagementPanel.this, e1);
                            }
                            optionsGrid.reload();
                        }
                    }
                });
            }
        });
        dlg.setVisible(true);
    }

    private void onEdit() {
        int selectedIndex = optionsGrid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < optionsGrid.getModel().getRowCount()) {
            GridRow row = ((GridTableModel) optionsGrid.getModel()).getRow(selectedIndex);
            final EditNodeOptionDialog dlg = new EditNodeOptionDialog(options.get(row.getId().toString()), NodeManagementPanel.this);
            dlg.setSuccessListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            NodeOption op = dlg.getOption();
                            if (null != op.getOptionName()) {
                                options.put(op.getOptionName(), op);
                                node.setNodeOptions(new ArrayList<NodeOption>(options.values()));
                                try {
                                    clusterService.saveNode(node);
                                } catch (ServiceException e1) {
                                    LOGGER.throwing(NodeManagementPanel.class.getName(), "onEditOption", e1);
                                    ErrorUtil.showError(NodeManagementPanel.this, e1);
                                }
                                optionsGrid.reload();
                            }
                        }
                    });
                }
            });
            dlg.setVisible(true);
        }
    }

    private void onDelete() {
        int selectedIndex = optionsGrid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < optionsGrid.getModel().getRowCount()) {
            final GridRow row = ((GridTableModel) optionsGrid.getModel()).getRow(selectedIndex);
            int n = JOptionPane.showConfirmDialog(
                    NodeManagementPanel.this,
                    "Are you sure you want to delete selected option \"" + row.getId().toString() + "\"?",
                    "Confirm deletion",
                    JOptionPane.YES_NO_OPTION);
            if (JOptionPane.YES_OPTION == n) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        options.remove(row.getId().toString());
                        node.setNodeOptions(new ArrayList<NodeOption>(options.values()));
                        try {
                            clusterService.saveNode(node);
                        } catch (ServiceException e1) {
                            LOGGER.throwing(NodeManagementPanel.class.getName(), "onDeleteOption", e1);
                            ErrorUtil.showError(NodeManagementPanel.this, e1);
                        }
                        optionsGrid.reload();
                    }
                });
            }
        }
    }
}
