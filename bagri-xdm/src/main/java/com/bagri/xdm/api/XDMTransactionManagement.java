package com.bagri.xdm.api;


public interface XDMTransactionManagement {
	
	static final long TX_NO = 0L;
	static final long TX_INIT = 1L;
	
	long beginTransaction() throws XDMException;

	long beginTransaction(XDMTransactionIsolation txIsolation) throws XDMException;
	
	void commitTransaction(long txId) throws XDMException;
	
	void rollbackTransaction(long txId) throws XDMException;

	void finishCurrentTransaction(boolean rollback) throws XDMException;

	boolean isInTransaction();
	
	long getTransactionTimeout();
	
	void setTransactionTimeout(long timeout) throws XDMException;
}
