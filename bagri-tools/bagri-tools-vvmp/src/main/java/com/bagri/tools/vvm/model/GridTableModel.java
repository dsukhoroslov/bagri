package com.bagri.tools.vvm.model;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.List;

public class GridTableModel extends AbstractTableModel {
    private TableColumnModel columnModel;
    private List<ColumnConfig> columnConfigs;
    final private GridDataLoader loader;
    private transient List<GridRow> data;
    private boolean loaded;

    public GridTableModel(List<ColumnConfig> columns, GridDataLoader loader) {
        this.loader = loader;
        this.columnConfigs = columns;
        DefaultTableColumnModel cm = new DefaultTableColumnModel();
        for (int i = 0; i < columns.size(); i++) {
            ColumnConfig cc = columns.get(i);
            TableColumn tc = new TableColumn(i);
            tc.setHeaderValue(cc.getHeader());
            if (null != cc.getMaxWidth()) {
                tc.setMaxWidth(cc.getMaxWidth());
            }
            if (null != cc.getMinWidth()) {
                tc.setMinWidth(cc.getMinWidth());
            }
            if (null != cc.getPreferredWidth()) {
                tc.setPreferredWidth(cc.getPreferredWidth());
            }
            if (null != cc.getWidth()) {
                tc.setWidth(cc.getWidth());
            }
            if (null != cc.getResizable()) {
                tc.setResizable(cc.getResizable());
            }
            cm.addColumn(tc);
        }
        setColumnModel(cm);
    }

    public TableColumnModel getColumnModel() {
        return columnModel;
    }

    public void setColumnModel(TableColumnModel columnModel) {
        this.columnModel = columnModel;
    }

    @Override
    public int getRowCount() {
        return (null != data) ? data.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return getColumnModel().getColumnCount();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return getColumnModel().getColumn(columnIndex).getHeaderValue().toString();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnConfigs.get(columnIndex).getColumnClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (!isLoaded()) {
            load();
        }
        GridRow row = getRow(rowIndex);
        return row.getValueAt(columnIndex);
    }

    public GridRow getRow(int rowIndex) {
        if (!isLoaded()) {
            load();
        }
        if (rowIndex >= data.size() || rowIndex < 0) {
            return null;
        }
        return data.get(rowIndex);
    }

    public void load() {
        data = loader.loadData();
        setLoaded(true);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
