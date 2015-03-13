package com.bagri.xdm.api;

public interface XDMTransactionManagement {
	
	long beginTransaction();
	
	void commitTransaction(long txId);
	
	void rollbackTransaction(long txId);

}
