package com.bagri.xdm.api;

public interface XDMTransactionManagement {
	
	public static final long TX_NO = 0L;
	public static final long TX_INIT = 1L;
	
	long beginTransaction();
	
	void commitTransaction(long txId);
	
	void rollbackTransaction(long txId);

}
