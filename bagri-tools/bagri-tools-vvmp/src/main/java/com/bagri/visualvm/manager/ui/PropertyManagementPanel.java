package com.bagri.visualvm.manager.ui;

import static com.bagri.visualvm.manager.util.Icons.ADD_ICON;
import static com.bagri.visualvm.manager.util.Icons.DELETE_ICON;
import static com.bagri.visualvm.manager.util.Icons.EDIT_ICON;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.bagri.visualvm.manager.model.ColumnConfig;
import com.bagri.visualvm.manager.model.DefaultGridRow;
import com.bagri.visualvm.manager.model.GridDataLoader;
import com.bagri.visualvm.manager.model.GridRow;
import com.bagri.visualvm.manager.model.GridTableModel;
import com.bagri.visualvm.manager.model.Property;

public class PropertyManagementPanel extends JPanel {
	
	private Properties properties;
    private XTable propertiesGrid;
	
	public PropertyManagementPanel(Properties properties, final JComponent owner) {
		super(new BorderLayout());
		this.properties = properties;
		
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
                        	PropertyManagementPanel.this.properties.put(prop.getPropertyName(), prop.getPropertyValue());
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
                    		PropertyManagementPanel.this.getParent(),
                            "Are you sure you want to delete selected property \"" + row.getId().toString() + "\"?",
                            "Confirm deletion",
                            JOptionPane.YES_NO_OPTION);
                    if (JOptionPane.YES_OPTION == n) {
                    	PropertyManagementPanel.this.properties.remove(row.getId().toString());
                        propertiesGrid.reload();
                    }
                }
            }
        });
        toolBar.add(deleteProperty);
        toolBar.addSeparator();
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.PAGE_START);
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
                for (Object propertyName : PropertyManagementPanel.this.properties.keySet()) {
                    String strPropertyName = propertyName.toString();
                    GridRow row = new DefaultGridRow(strPropertyName, new Object[] {strPropertyName, 
                    		PropertyManagementPanel.this.properties.getProperty(strPropertyName)});
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
        add(new JScrollPane(propertiesGrid), BorderLayout.CENTER);
        propertiesGrid.reload();
		
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

	

}
