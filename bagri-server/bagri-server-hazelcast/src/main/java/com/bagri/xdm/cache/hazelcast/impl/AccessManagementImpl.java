package com.bagri.xdm.cache.hazelcast.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMAccessManagement;

public class AccessManagementImpl implements XDMAccessManagement {

	private static final transient Logger logger = LoggerFactory.getLogger(AccessManagementImpl.class);
	
	private String schemaName;
	private String schemaPass;

	public String getSchemaName() {
		return schemaName;
	}
	
	public String getSchemaPass() {
		return schemaPass;
	}
	
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public void setSchemaPass(String schemaPass) {
		this.schemaPass = schemaPass;
	}
	
	@Override
	public boolean authenticate(String username, String password) {
		logger.trace("authenticate; user: {}, password: {}", username, password);
		if (username.equals(schemaName) && password.equals(schemaPass)) {
			return true;
		}
		// TODO: check username/password against access DB
		return true;
	}

}
