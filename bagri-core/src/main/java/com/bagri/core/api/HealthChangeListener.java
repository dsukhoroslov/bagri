package com.bagri.core.api;

/**
 * XDM health state change listening interface; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public interface HealthChangeListener {
	
	/**
	 * fires when Schema health state changes
	 * 
	 * @param newState {@link HealthState} 
	 */
	void onHealthStateChange(HealthState newState);

}
