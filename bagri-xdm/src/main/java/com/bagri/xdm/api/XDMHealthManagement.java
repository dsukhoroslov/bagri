package com.bagri.xdm.api;

/**
 * XDM health management interface; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public interface XDMHealthManagement {
	
	/**
	 * perform XDM Schema health check
	 * 
	 * @throws XDMException in case of wron state
	 */
	void checkClusterState() throws XDMException;

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
	XDMHealthState getHealthState();
	
	/**
	 * 
	 * @return XDM client check state
	 */
	XDMHealthCheckState getCheckState();
	
	/**
	 * 
	 * @param state the {@link XDMHealthCheckState} value to set
	 */
	void setCheckSate(XDMHealthCheckState state);
	
	//int getDocumentCount();
	
	/**
	 * 
	 * @param listener the {@link XDMHealthChangeListener} to registered for listening on health events
	 */
	void addHealthChangeListener(XDMHealthChangeListener listener);
	
	/**
	 * 
	 * @param listener the {@link XDMHealthChangeListener} to unregister from listening on health events
	 */
	void removeHealthChangeListener(XDMHealthChangeListener listener);
	
}
