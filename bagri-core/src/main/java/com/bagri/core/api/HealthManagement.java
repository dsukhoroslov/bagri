package com.bagri.core.api;

/**
 * XDM health management interface; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public interface HealthManagement {
	
	/**
	 * perform XDM Schema health check
	 * 
	 * @throws BagriException in case of wron state
	 */
	void checkClusterState() throws BagriException;

	/**
	 * perform XDM Schema health check, does not throw exception in case of wrong state 
	 * 
	 * @return true in case of safe state, false otherwise
	 */
	boolean isClusterSafe();
	
	/**
	 * 
	 * @return number of nodes in XDM Schema cluster
	 */
	int getClusterSize();

	/**
	 * 
	 * @return XDM Schema health state
	 */
	HealthState getHealthState();
	
	/**
	 * 
	 * @return XDM client check state
	 */
	HealthCheckState getCheckState();
	
	/**
	 * 
	 * @param state the {@link HealthCheckState} value to set
	 */
	void setCheckSate(HealthCheckState state);
	
	/**
	 * 
	 * @param listener the {@link HealthChangeListener} to registered for listening on health events
	 */
	void addHealthChangeListener(HealthChangeListener listener);
	
	/**
	 * 
	 * @param listener the {@link HealthChangeListener} to unregister from listening on health events
	 */
	void removeHealthChangeListener(HealthChangeListener listener);
	
}
