package com.bagri.tools.vvm.model;

public class TypedValue {
	
	private String type;
	private Object value;
	
	public TypedValue(String type, Object value) {
		this.type = type;
		this.value = value;
	}
	
	public String getType() {
		return type;
	}
	
	public Object getValue() {
		return value;
	}

}
