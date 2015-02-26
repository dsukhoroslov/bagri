package com.bagri.xdm.api;

public interface XDMTransactionManagement {
	
	String beginTransaction();
	
	void commitTransaction(String txId);
	
	void rollbackTransaction(String txId);

}
