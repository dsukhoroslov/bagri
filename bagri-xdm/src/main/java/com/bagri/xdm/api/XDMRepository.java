package com.bagri.xdm.api;

public interface XDMRepository {
	
	static String bean_id = "xdmRepo";
	
	void close();
	
	String getClientId();

	String getUserName();
	
	XDMAccessManagement getAccessManagement();
	
	XDMBindingManagement getBindingManagement();
	
	XDMDocumentManagement getDocumentManagement();

	XDMHealthManagement getHealthManagement();
	
	XDMModelManagement getModelManagement();

	XDMQueryManagement getQueryManagement();
	
	XDMTransactionManagement getTxManagement();
	
}
