package com.bagri.xdm.access.api;

public interface XDMNodeManager {

	String getOption(String name);
	void setOption(String name, String value);
	void removeOption(String name);
	
}
