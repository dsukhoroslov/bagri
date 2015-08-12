package com.bagri.xdm.api;

public class XDMException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8774397864143426913L;
	
	public XDMException(String message) {
		super(message);
	}
	
	public XDMException(Throwable cause) {
		super(cause);
	}
	
	public XDMException(String message, Throwable cause) {
		super(message, cause);
	}

}
