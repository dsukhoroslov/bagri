package com.bagri.xdm.api;

public interface XDMHealthManagement {
	
	boolean isClusterSafe();
	
	int getClusterSize();
	
	//int getDocumentCount();
}
