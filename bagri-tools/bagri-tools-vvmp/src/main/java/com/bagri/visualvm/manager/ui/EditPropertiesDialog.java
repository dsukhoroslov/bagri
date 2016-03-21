package com.bagri.visualvm.manager.ui;

import com.bagri.visualvm.manager.event.ApplicationEvent;
import com.bagri.visualvm.manager.model.*;
import com.bagri.visualvm.manager.util.WindowUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import static com.bagri.visualvm.manager.util.Icons.*;

public class EditPropertiesDialog extends JDialog {
    private static final KeyStroke ESCAPE_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private JTextField schemaName;
    private JTextField description;
    private Schema original;
    private Properties properties; // = new Properties();
    private ActionListener successListener;
    private XTable propertiesGrid;

    public EditPropertiesDialog(Properties props, final JComponent owner) {
        super(WindowUtil.getFrameForComponent(owner), "Edit Properties", true);
        this.properties = props;
        JPanel propertiesPanel = new JPanel(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        // "Add Property" button
        JButton addProperty = new JButton("Add");
        addProperty.setToolTipText("Add new property");
        addProperty.setIcon(ADD_ICON);
        addProperty.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final EditPropertyDialog dlg = new EditPropertyDialog(null,"Property", owner);
                dlg.setSuccessListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Property prop = dlg.getProperty();
                        if (null != prop.getPropertyName()) {
                            properties.put(prop.getPropertyName(), prop.getPropertyValue());
                        }
                        propertiesGrid.reload();
                    }
                });
                dlg.setVisible(true);
            }
        });
        toolBar.add(addProperty);
        toolBar.addSeparator();
        // "Edit Property" button
        JButton editProperty = new JButton("Edit");
        editProperty.setToolTipText("Edit property");
        editProperty.setIcon(EDIT_ICON);
        editProperty.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onEdit(owner);
            }
        });
        toolBar.add(editProperty);
        toolBar.addSeparator();
        // "Delete Property" button
        JButton deleteProperty = new JButton("Delete");
        deleteProperty.setToolTipText("Delete property");
        deleteProperty.setIcon(DELETE_ICON);
        deleteProperty.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = propertiesGrid.getSelectionModel().getLeadSelectionIndex();
                if (selectedIndex >= 0 && selectedIndex < propertiesGrid.getModel().getRowCount()) {
                    GridRow row = ((GridTableModel) propertiesGrid.getModel()).getRow(selectedIndex);
                    int n = JOptionPane.showConfirmDialog(
                            EditPropertiesDialog.this,
                            "Are you sure you want to delete selected property \"" + row.getId().toString() + "\"?",
                            "Confirm deletion",
                            JOptionPane.YES_NO_OPTION);
                    if (JOptionPane.YES_OPTION == n) {
                        properties.remove(row.getId().toString());
                        propertiesGrid.reload();
                    }
                }
            }
        });
        toolBar.add(deleteProperty);
        toolBar.addSeparator();
        toolBar.setFloatable(false);
        propertiesPanel.add(toolBar, BorderLayout.PAGE_START);
        // Column configs
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig c = new ColumnConfig();
        c.setHeader("Property name");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Property value");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        propertiesGrid = new XTable(configs, new GridDataLoader() {
            @Override
            public java.util.List<GridRow> loadData() {
                java.util.List<GridRow> result = new ArrayList<GridRow>();
                for (Object propertyName : properties.keySet()) {
                    String strPropertyName = propertyName.toString();
                    GridRow row = new DefaultGridRow(strPropertyName, new Object[] {strPropertyName, properties.getProperty(strPropertyName)});
                    result.add(row);
                }
                return result;
            }
        });
        propertiesGrid.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()>=2) {
                    onEdit(owner);
                }
            }
        });
        propertiesPanel.add(new JScrollPane(propertiesGrid), BorderLayout.CENTER);
        propertiesGrid.reload();

        JButton okButton = new JButton("Ok");

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (null != successListener) {
                    successListener.actionPerformed(new ActionEvent(EditPropertiesDialog.this, e.getID(), "editProperties"));
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
        bp.add(okButton);
        bp.add(cancelButton);

        //getContentPane().add(fieldsPanel, BorderLayout.PAGE_START);
        getContentPane().add(propertiesPanel, BorderLayout.CENTER); //PAGE_START);
        getContentPane().add(bp, BorderLayout.PAGE_END);
        setPreferredSize(new Dimension(280, 300));
        setMinimumSize(new Dimension(280, 300));

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);

        Action dispatchClosing = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dispatchEvent(new WindowEvent(
                        EditPropertiesDialog.this, WindowEvent.WINDOW_CLOSING
                ));
            }
        };
        JRootPane root = getRootPane();
        root.setDefaultButton(okButton);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE_STROKE, ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION);
        root.getActionMap().put(ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION, dispatchClosing );
    }

    private void onEdit(JComponent owner) {
        int selectedIndex = propertiesGrid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < propertiesGrid.getModel().getRowCount()) {
            GridRow row = ((GridTableModel) propertiesGrid.getModel()).getRow(selectedIndex);
            final EditPropertyDialog dlg = new EditPropertyDialog(new Property((String) row.getId(), (String) properties.get(row.getId())), "Property", owner);
            dlg.setSuccessListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Property prop = dlg.getProperty();
                    if (null != prop.getPropertyName()) {
                        properties.put(prop.getPropertyName(), prop.getPropertyValue());
                    }
                    propertiesGrid.reload();
                }
            });
            dlg.setVisible(true);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void setSuccessListener(ActionListener successListener) {
        this.successListener = successListener;
    }
}
