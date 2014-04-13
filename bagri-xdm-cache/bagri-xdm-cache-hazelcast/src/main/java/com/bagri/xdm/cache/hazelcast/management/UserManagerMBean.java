package com.bagri.xdm.cache.hazelcast.management;

public interface UserManagerMBean {
	
	boolean activateUser(String login, boolean activate);
	boolean changePassword(String login, String password);

}
