package com.bagri.core.api;

/**
 * XDM transaction state enumeration; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public enum TransactionState {
	
	/**
	 * transaction is in active state
	 */
	started,
	
	/**
	 * transaction has been commited
	 */
	commited,
	
	/**
	 * transaction has been rolled back
	 */
	rolledback;

	//suspended ?
	
}
