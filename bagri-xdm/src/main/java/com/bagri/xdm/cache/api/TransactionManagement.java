package com.bagri.xdm.cache.api;

import java.util.concurrent.Callable;

import com.bagri.xdm.api.XDMException;

/**
 * XDM Transaction management server-side extension.
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface TransactionManagement extends com.bagri.xdm.api.TransactionManagement {

	/**
	 * A generic method to run any callable task within transaction boundaries 
	 * 
	 * @param txId the transaction identifier. Can be 0, then new transaction will be started if the task is not read-only  
	 * @param readOnly indicates call task nature: is it read-only or read-write
	 * @param call the task to be performed within transaction
	 * @param <V> the task result type
	 * @return the task execution result
	 * @throws XDMException in case of any error
	 */
	<V> V callInTransaction(long txId, boolean readOnly, Callable<V> call) throws XDMException;	

}
