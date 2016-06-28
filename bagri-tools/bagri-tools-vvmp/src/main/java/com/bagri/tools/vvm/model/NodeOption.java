package com.bagri.tools.vvm.model;

public class NodeOption {
    private String optionName;
    private String optionValue;

    public NodeOption() {
    }

    public NodeOption(String optionName, String optionValue) {
        this.optionName = optionName;
        this.optionValue = optionValue;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }
}
