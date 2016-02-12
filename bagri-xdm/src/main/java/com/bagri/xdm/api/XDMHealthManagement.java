package com.bagri.xdm.api;

public interface XDMHealthManagement {
	
	void checkClusterState() throws XDMException;

	boolean isClusterSafe();
	
	int getClusterSize();
	
	XDMHealthState getHealthState();
	
	XDMHealthCheckState getCheckState();
	void setCheckSate(XDMHealthCheckState state);
	
	//int getDocumentCount();
	
	void addHealthChangeListener(XDMHealthChangeListener listener);
	
	void removeHealthChangeListener(XDMHealthChangeListener listener);
}
