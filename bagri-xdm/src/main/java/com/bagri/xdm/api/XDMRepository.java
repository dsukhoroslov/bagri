package com.bagri.xdm.api;

public interface XDMRepository {
	
	void close();
	
	//XDMAccessManagement getAccessManagement();
	
	XDMDocumentManagement getDocumentManagement();
	
	XDMModelManagement getModelManagement();

	XDMQueryManagement getQueryManagement();
	
	XDMTransactionManagement getTxManagement();
	
}
