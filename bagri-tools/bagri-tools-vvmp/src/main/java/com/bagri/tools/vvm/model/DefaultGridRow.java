package com.bagri.tools.vvm.model;

import java.util.Arrays;
import java.util.List;

public class DefaultGridRow implements GridRow {
    private List<Object> values;
    private final Object id;

    public DefaultGridRow(Object id) {
        this.id = id;
    }

    public DefaultGridRow(Object id, Object[] values) {
        this.id = id;
        this.values = Arrays.asList(values);
    }

    public DefaultGridRow(Object id, List<Object> values) {
        this.id = id;
        this.values = values;
    }

    @Override
    public Object getValueAt(int index) {
        if (null == values || index >= values.size() || index < 0) {
            return null;
        }
        return values.get(index);
    }

    @Override
    public Object getId() {
        return id;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }
}
