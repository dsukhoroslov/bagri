package com.bagri.xdm.api;

public interface XDMRepository {
	
	void close();
	
	//XDMAccessManagement getAccessManagement();
	
	//XDMTransactionManagement getTxManagement();
	
	XDMDocumentManagement getDocumentManagement();
	
	XDMQueryManagement getQueryManagement();
	
	XDMModelManagement getModelManagement();

}
