package com.bagri.xdm.api;

public interface XDMRepository {
	
	void close();
	
	//XDMAccessManagement getAccessManagement();
	
	XDMDocumentManagement getDocumentManagement();
	
	XDMQueryManagement getQueryManagement();
	
	XDMModelManagement getModelManagement();

}
