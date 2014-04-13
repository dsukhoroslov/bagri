package com.bagri.xdm.cache.hazelcast.management;

public interface UserManagementMBean {

	String[] getUserNames();
	boolean addUser(String login, String password);
	boolean deleteUser(String login);
	
}
