package com.bagri.xdm.api;

/**
 * XDM health state change listening interface; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public interface XDMHealthChangeListener {
	
	/**
	 * fires when Schema health state changes
	 * 
	 * @param newState {@link XDMHealthState} 
	 */
	void onHealthStateChange(XDMHealthState newState);

}
