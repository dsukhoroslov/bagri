package com.bagri.xdm.cache.hazelcast.security;

import java.util.Collections;
import java.util.Map;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.management.UserManagement;
import com.bagri.xdm.system.XDMUser;


public class BagriJMXAuthenticator implements JMXAuthenticator {
	
    private static final transient Logger logger = LoggerFactory.getLogger(BagriJMXAuthenticator.class);

    private UserManagement uMgr;

    @Override
	public Subject authenticate(Object credentials) {

		//logger.info("authenticate.enter; got credentials: {}", credentials); 
		// Verify that credentials is of type String[].
		//
		if (!(credentials instanceof String[])) {
			// Special case for null so we get a more informative message
			if (credentials == null) {
				throw new SecurityException("Credentials required");
			}
			throw new SecurityException("Credentials should be String[]");
		}

		// Verify that the array contains two elements
		// (username/password).
		//
		final String[] aCredentials = (String[]) credentials;
		if (aCredentials.length != 2) {
			throw new SecurityException("Credentials should have 2 elements");
		}

		// Perform authentication
		//
		String username = (String) aCredentials[0];
		String password = (String) aCredentials[1];

		if (checkCreds(username, password)) {
			Subject result = new Subject(true,
					Collections.singleton(new JMXPrincipal(username)),
					Collections.EMPTY_SET,
					Collections.EMPTY_SET);
			logger.debug("authenticate.exit; returning: {}", result);
			return result;
		} else {
			throw new SecurityException("Invalid credentials");
		}
	}
	
	public void setUserManager(UserManagement uMgr) {
		this.uMgr = uMgr;
	}
	
	private boolean checkCreds(String login, String password) {
		// check user from userCache..
		logger.debug("authenticate; login: {}", login);
		return uMgr.authenticate(login, password);
	}

}
