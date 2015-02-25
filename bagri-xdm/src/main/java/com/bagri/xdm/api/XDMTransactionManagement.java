package com.bagri.xdm.api;

public interface XDMTransactionManagement {
	
	void beginTransaction();
	
	void commitTransaction();
	
	void rollbackTransaction();

}
