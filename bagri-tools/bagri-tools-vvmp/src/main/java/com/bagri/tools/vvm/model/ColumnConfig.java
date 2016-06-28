package com.bagri.tools.vvm.model;

public class ColumnConfig {
    private String header;
    private Integer width;
    private Integer minWidth;
    private Integer maxWidth;
    private Integer preferredWidth;
    private Boolean resizable;
    private Class columnClass;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setFixedWidth(Integer width) {
        this.width = width;
        this.minWidth = width;
        this.maxWidth = width;
        this.preferredWidth = width;
    }

    public Integer getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    public Integer getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredWidth(Integer preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public Boolean getResizable() {
        return resizable;
    }

    public void setResizable(Boolean resizable) {
        this.resizable = resizable;
    }

    public Class getColumnClass() {
        return columnClass;
    }

    public void setColumnClass(Class columnClass) {
        this.columnClass = columnClass;
    }
}
