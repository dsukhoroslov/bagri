package com.bagri.xdm.api;

import com.bagri.xdm.common.XDMTransactionIsolation;

public interface XDMTransactionManagement {
	
	public static final long TX_NO = 0L;
	public static final long TX_INIT = 1L;
	
	long beginTransaction();

	long beginTransaction(XDMTransactionIsolation txIsolation);
	
	void commitTransaction(long txId);
	
	void rollbackTransaction(long txId);

}
