package com.bagri.core.api;


/**
 * XDM transaction management interface; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public interface TransactionManagement {
	
	/**
	 * no transaction
	 */
	static final long TX_NO = 0L;
	
	/**
	 * init transaction, used at data population phase
	 */
	static final long TX_INIT = 1L;
	
	/**
	 * starts new transaction with default isolation level
	 * 
	 * @return started transaction id
	 * @throws BagriException in case of any errors
	 */
	long beginTransaction() throws BagriException;

	/**
	 * starts new transaction with specified isolation level
	 * 
	 * @param txIsolation the tx isolation level
	 * @return started transaction id
	 * @throws BagriException in case of any errors
	 */
	long beginTransaction(TransactionIsolation txIsolation) throws BagriException;
	
	/**
	 * commits active transaction
	 * 
	 * @param txId the active transaction identifier
	 * @throws BagriException in case of any error
	 */
	void commitTransaction(long txId) throws BagriException;
	
	/**
	 * rolls back active transaction
	 * 
	 * @param txId the active transaction identifier
	 * @throws BagriException in case of any error
	 */
	void rollbackTransaction(long txId) throws BagriException;

	/**
	 * commits/rolls back current client transaction if any
	 * 
	 * @param rollback to roll back current transaction or not
	 * @return true if transaction was finished, false otherwise
	 * @throws BagriException in case of any error
	 */
	boolean finishCurrentTransaction(boolean rollback) throws BagriException;

	/**
	 * 
	 * @return true if client is in active transaction, false otherwise
	 */
	boolean isInTransaction();
	
	/**
	 * 
	 * @return transaction timeout value in milliseconds
	 */
	long getTransactionTimeout();
	
	/**
	 * 
	 * @param timeout the timeout value to set in milliseconds
	 * @throws BagriException in case of any error
	 */
	void setTransactionTimeout(long timeout) throws BagriException;
	
}
