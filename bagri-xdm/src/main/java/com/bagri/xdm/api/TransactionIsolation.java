package com.bagri.xdm.api;

/**
 * XDM transaction isolation levels; corresponds to standard RDBMS tx isolation levels
 * 
 * @author Denis Sukhoroslov
 */
public enum TransactionIsolation {
	
	/**
	 * changes done by not-commited-yet transaction are visible by other transactions 
	 */
	dirtyRead,
	
	/**
	 * changes done by transaction became visible for other transactions after commit only
	 */
	readCommited,
	
	/**
	 * transaction do not see changes done by other transactions started and commited after it
	 */
	repeatableRead,
	
	/**
	 * all transactions are ordered and their changes are serialized. Not used as of now 
	 */
	serializable;

}
