package com.bagri.tools.vvm.ui;

import javax.swing.*;

import com.bagri.tools.vvm.model.ColumnConfig;
import com.bagri.tools.vvm.model.GridDataLoader;
import com.bagri.tools.vvm.model.GridTableModel;

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

