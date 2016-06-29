package com.bagri.xdm.api;

/**
 * XDM health state enumeration; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public enum HealthState {
	
	/**
	 * healthy: the number of cached documents is equal to expected.
	 */
	good,   
	
	/**
	 * weird, but can work: the number of cached documents is greater then expected. Some gap in document state notification is possible  
	 */
	bad,   
	
	/**
	 * dangerous: the number of cached documents is less then expected. Means some documents are missing from schema cache
	 */
	ugly;

}
