package com.bagri.xdm.api;

import com.bagri.xdm.common.XDMTransactionIsolation;

public interface XDMTransactionManagement {
	
	public static final long TX_NO = 0L;
	public static final long TX_INIT = 1L;
	
	long beginTransaction() throws XDMException;

	long beginTransaction(XDMTransactionIsolation txIsolation) throws XDMException;
	
	void commitTransaction(long txId) throws XDMException;
	
	void rollbackTransaction(long txId) throws XDMException;

	long getTransactionTimeout();
	
	void setTransactionTimeout(long timeout) throws XDMException;
}
