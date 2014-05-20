package com.bagri.visualvm.manager.ui;

import com.bagri.visualvm.manager.model.ColumnConfig;
import com.bagri.visualvm.manager.model.GridDataLoader;
import com.bagri.visualvm.manager.model.GridTableModel;

import javax.swing.*;
import java.util.List;

public class XTable extends JTable {
    private GridTableModel gridModel;
    public XTable(List<ColumnConfig> columns, GridDataLoader loader) {
        super();
        ListSelectionModel sm = createDefaultSelectionModel();
        this.gridModel = new GridTableModel(columns, loader);
        autoCreateColumnsFromModel = false;
        setColumnModel(gridModel.getColumnModel());
        setModel(gridModel);
        sm.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        setSelectionModel(sm);
    }

    public void reload() {
        gridModel.load();
        gridModel.fireTableDataChanged();
//        super.resizeAndRepaint();
    }

    public boolean isLoaded() {
        return gridModel.isLoaded();
    }
}

