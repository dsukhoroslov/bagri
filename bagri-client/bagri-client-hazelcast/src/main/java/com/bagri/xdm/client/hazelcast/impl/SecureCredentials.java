package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.hazelcast.serialize.PortableFactoryImpl.cli_XDMCredentials;
import static com.bagri.xdm.client.hazelcast.serialize.PortableFactoryImpl.factoryId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.security.UsernamePasswordCredentials;

public class SecureCredentials extends UsernamePasswordCredentials { 
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 87879696779268414L;
	
	private static final transient Logger logger = LoggerFactory.getLogger(SecureCredentials.class);
	
	public SecureCredentials() {
		super();
		logger.trace("<init>");
	}
	
	public SecureCredentials(String username, String password) {
		super(username, password);
		logger.trace("<init>; username: {}, password: {}", username, password);
	}

	@Override
	public int getClassId() {
		return cli_XDMCredentials;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
}
