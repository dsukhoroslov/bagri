package com.bagri.tools.vvm.ui;

import javax.management.ObjectName;
import javax.swing.*;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.model.*;
import com.bagri.tools.vvm.util.Icons;
import com.bagri.tools.vvm.util.WindowUtil;

import static com.bagri.tools.vvm.util.Icons.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditNodeDialog extends JDialog {
    private static final KeyStroke ESCAPE_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private JTextField nodeId;
    private ObjectName objectName;
    private Map<String, NodeOption> options = new HashMap<String, NodeOption>();
    private ActionListener successListener;
    private XTable optionsGrid;
    private enum Mode {
        ADD,
        EDIT
    }

    public EditNodeDialog(Node node, final JComponent owner) {
        super(WindowUtil.getFrameForComponent(owner), ((null == node) ? "Add" : "Edit") + " Node", true);
        Mode mode = (null == node) ? Mode.ADD : Mode.EDIT;
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(BorderFactory.createEtchedBorder());
//        fieldsPanel.setMinimumSize(new Dimension(200, 100));
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbNodeId = new JLabel("Name: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        cs.weightx=0;
        fieldsPanel.add(lbNodeId, cs);

        nodeId = new JTextField(20);
//        nodeId.setMinimumSize(new Dimension(200, -1));
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        fieldsPanel.add(nodeId, cs);
        if (Mode.EDIT == mode) {
            objectName = node.getObjectName();
            nodeId.setText(node.getName());
            for (NodeOption op : node.getNodeOptions()) {
                options.put(op.getOptionName(), op);
            }
        }

        JPanel optionsPanel = new JPanel(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        // "Add Option" button
        JButton addOption = new JButton("Add");
        addOption.setToolTipText("Adds new option");
        addOption.setIcon(Icons.ADD_ICON);
        addOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final EditNodeOptionDialog dlg = new EditNodeOptionDialog(null, owner);
                dlg.setSuccessListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        NodeOption op = dlg.getOption();
                        if (null != op.getOptionName())
                        options.put(op.getOptionName(), op);
                        optionsGrid.reload();
                    }
                });
                dlg.setVisible(true);
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
                int selectedIndex = optionsGrid.getSelectionModel().getLeadSelectionIndex();
                if (selectedIndex >= 0 && selectedIndex < optionsGrid.getModel().getRowCount()) {
                    GridRow row = ((GridTableModel) optionsGrid.getModel()).getRow(selectedIndex);
                    final EditNodeOptionDialog dlg = new EditNodeOptionDialog(options.get(row.getId().toString()), owner);
                    dlg.setSuccessListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            NodeOption op = dlg.getOption();
                            if (null != op.getOptionName())
                                options.put(op.getOptionName(), op);
                            optionsGrid.reload();
                        }
                    });
                    dlg.setVisible(true);
                }
            }
        });
        toolBar.add(editOption);
        toolBar.addSeparator();
        // "Delete Option" button
        JButton deleteOption = new JButton("Delete");
        deleteOption.setToolTipText("Delete option");
        deleteOption.setIcon(Icons.DELETE_ICON);
        deleteOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = optionsGrid.getSelectionModel().getLeadSelectionIndex();
                if (selectedIndex >= 0 && selectedIndex < optionsGrid.getModel().getRowCount()) {
                    GridRow row = ((GridTableModel) optionsGrid.getModel()).getRow(selectedIndex);
                    int n = JOptionPane.showConfirmDialog(
                            EditNodeDialog.this,
                            "Are you sure you want to delete selected option \"" + row.getId().toString() + "\"?",
                            "Confirm deletion",
                            JOptionPane.YES_NO_OPTION);
                    if (JOptionPane.YES_OPTION == n) {
                        options.remove(row.getId().toString());
                        optionsGrid.reload();
                    }
                }
            }
        });
        toolBar.add(deleteOption);
        toolBar.addSeparator();
        toolBar.setFloatable(false);
        optionsPanel.add(toolBar, BorderLayout.PAGE_START);

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
            public List<GridRow> loadData() {
                List<GridRow> result = new ArrayList<GridRow>();
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
                    int selectedIndex = optionsGrid.getSelectionModel().getLeadSelectionIndex();
                    if (selectedIndex >= 0 && selectedIndex < optionsGrid.getModel().getRowCount()) {
                        GridRow row = ((GridTableModel) optionsGrid.getModel()).getRow(selectedIndex);
                        final EditNodeOptionDialog dlg = new EditNodeOptionDialog(options.get(row.getId().toString()), owner);
                        dlg.setSuccessListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                NodeOption op = dlg.getOption();
                                if (null != op.getOptionName()){
                                    options.put(op.getOptionName(), op);
                                    optionsGrid.reload();
                                }
                            }
                        });
                        dlg.setVisible(true);
                    }
                }
            }
        });
        optionsPanel.add(new JScrollPane(optionsGrid), BorderLayout.CENTER);
        optionsGrid.reload();

        JButton editButton = new JButton((mode == Mode.ADD) ? "Add Node" : "Update");

        editButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (null != successListener) {
                    successListener.actionPerformed(new ActionEvent(EditNodeDialog.this, e.getID(), "editNode"));
                }
                dispose();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bp = new JPanel();
        bp.add(editButton);
        bp.add(cancelButton);

        getContentPane().add(fieldsPanel, BorderLayout.PAGE_START);
        getContentPane().add(optionsPanel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
        setPreferredSize(new Dimension(280, 300));
        setMinimumSize(new Dimension(280, 300));

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);

        Action dispatchClosing = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dispatchEvent(new WindowEvent(
                        EditNodeDialog.this, WindowEvent.WINDOW_CLOSING
                ));
            }
        };
        JRootPane root = getRootPane();
        root.setDefaultButton(editButton);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE_STROKE, ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION);
        root.getActionMap().put(ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION, dispatchClosing );
    }

    public Node getNode() {
        Node res = new Node(objectName, nodeId.getText());
        res.setNodeOptions(new ArrayList<NodeOption>(options.values()));
        return res;
    }

    public void setSuccessListener(ActionListener successListener) {
        this.successListener = successListener;
    }
}
