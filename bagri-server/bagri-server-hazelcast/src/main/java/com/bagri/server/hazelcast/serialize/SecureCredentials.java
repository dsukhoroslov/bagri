package com.bagri.server.hazelcast.serialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.AccessManagement;
import com.bagri.server.hazelcast.impl.AccessManagementImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
@SuppressWarnings("serial")
public class SecureCredentials extends com.bagri.client.hazelcast.impl.SecureCredentials {
	
	private static final transient Logger logger = LoggerFactory.getLogger(SecureCredentials.class);
	
	private AccessManagement accMgr;
	private boolean authenticated = false;

	public SecureCredentials() {
		super();
	}
	
	@Autowired
	public void setAccessManager(AccessManagement accMgr) {
		this.accMgr = accMgr;
		String username = super.getUsername();
		String password = super.getPassword();
		authenticated = accMgr.authenticate(username, password);
		logger.trace("setAccessManager; got: {}; authentecated: {}", accMgr, authenticated);
	}
	
	@Override
    public String getUsername() {
		if (authenticated) {
			return ((AccessManagementImpl) accMgr).getSchemaName();
		}
        return "";
    }

	@Override
    public String getPassword() {
		if (authenticated) {
			return ((AccessManagementImpl) accMgr).getSchemaPass();
		}
        return "";
	}

	
}
