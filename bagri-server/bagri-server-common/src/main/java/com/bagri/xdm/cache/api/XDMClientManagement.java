package com.bagri.xdm.cache.api;

import java.util.Properties;

public interface XDMClientManagement {

	String[] getClients();
	
	String getCurrentUser();
	
	Properties getClientProperties(String clientId);
	
}
