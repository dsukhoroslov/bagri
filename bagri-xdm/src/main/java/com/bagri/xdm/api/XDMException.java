package com.bagri.xdm.api;

public class XDMException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8774397864143426913L;
	
	public static final int ecUnknown = 0;
	public static final int ecAccess = 10000;
	public static final int ecBinding = 20000;
	public static final int ecDocument = 30000;
	public static final int ecInOut = 40000;
	public static final int ecModel = 50000;
	public static final int ecIndex = 60000;
	public static final int ecQuery = 70000;
	public static final int ecTransaction = 80000;
	
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
}
