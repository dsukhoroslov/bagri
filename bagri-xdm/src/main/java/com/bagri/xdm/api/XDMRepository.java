package com.bagri.xdm.api;

public interface XDMRepository {
	
	void close();
	
	String getClientId();

	String getUserName();
	
	//XDMAccessManagement getAccessManagement();
	
	XDMBindingManagement getBindingManagement();
	
	XDMDocumentManagement getDocumentManagement();
	
	XDMModelManagement getModelManagement();

	XDMQueryManagement getQueryManagement();
	
	XDMTransactionManagement getTxManagement();
	
}
