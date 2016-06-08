package com.bagri.xdm.api;

/**
 * XDM Exception; 
 * 
 * @author Denis Sukhoroslov
 */
public class XDMException extends Exception {

	private static final long serialVersionUID = 8774397864143426913L;
	
	/**
	 * unknown error
	 */
	public static final int ecUnknown = 0;
	
	/**
	 * access error
	 */
	public static final int ecAccess = 10000;
	
	/**
	 * binding error
	 */
	public static final int ecBinding = 20000;
	
	/**
	 * document handling error
	 */
	public static final int ecDocument = 30000;
	
	/**
	 * health error 
	 */
	public static final int ecHealth = 40000;
	
	/**
	 * index handling error
	 */
	public static final int ecIndex = 50000;
	
	/**
	 * unique index violation
	 */
	public static final int ecIndexUnique = 50001;
	
	/**
	 * IO error
	 */
	public static final int ecInOut = 60000;
	
	/**
	 * model handling error
	 */
	public static final int ecModel = 70000;
	
	/**
	 * query error
	 */
	public static final int ecQuery = 80000;
	
	/**
	 * query cancelled
	 */
	public static final int ecQueryCancel = 80001;
	
	/**
	 * error compiling query 
	 */
	public static final int ecQueryCompile = 80002;

	/**
	 * query timed out
	 */
	public static final int ecQueryTimeout = 80003;
	
	/**
	 * transaction handling error
	 */
	public static final int ecTransaction = 90000;
	
	/**
	 * an attempt to begin new transaction from thread having current transaction in active state 
	 */
	public static final int ecTransNoNested = 90001;
	
	/**
	 * transaction not found
	 */
	public static final int ecTransNotFound = 90002;
	
	/**
	 * transaction timed out
	 */
	public static final int ecTransTimeout = 90003;
	
	/**
	 * unexpected transaction state
	 */
	public static final int ecTransWrongState = 90004;
	
	private int errorCode;
	
	public XDMException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public XDMException(Throwable cause, int errorCode) {
		super(cause);
		this.errorCode = errorCode;
	}
	
	public XDMException(String message, Throwable cause, int errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
	
	public String getVendorCode() {
		return String.valueOf(errorCode);
	}
}
