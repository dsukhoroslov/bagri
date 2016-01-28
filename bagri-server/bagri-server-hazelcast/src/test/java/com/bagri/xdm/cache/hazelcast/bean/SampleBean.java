package com.bagri.xdm.cache.hazelcast.bean;

public class SampleBean { 

	private int intProp;
	private boolean boolProp;
	private String strProp;
	
	public SampleBean() {
		intProp = 0;
		strProp = "";
		boolProp = true;
	}
	
	public SampleBean(int intValue, boolean boolProp, String strValue) {
		this.intProp = intValue;
		this.boolProp = boolProp;
		this.strProp = strValue;
	}
	
	public int getIntProperty() {
		return intProp;
	}
	
	public void setIntProperty(int value) {
		this.intProp = value;
	}
	
	public boolean isBooleanProperty() {
		return boolProp;
	}
	
	public void setBooleanProperty(boolean value) {
		this.boolProp = value;
	}

	public String getStringProperty() {
		return strProp;
	}
	
	public void setStringProperty(String value) {
		this.strProp = value;
	}

}
