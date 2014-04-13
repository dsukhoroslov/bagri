package com.bagri.xdm.cache.hazelcast.security;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.hazelcast.security.
import com.hazelcast.spi.Callback;

public class BagriLoginModule implements LoginModule {
		
    private static final transient Logger logger = LoggerFactory.getLogger(BagriLoginModule.class);
	
	private Subject subject;
	private CallbackHandler callbackHandler;
	
	public BagriLoginModule() {
		logger.debug("<init>; login module initialized"); 
	}
		
	public void initialize(Subject subject, CallbackHandler callbackHandler,
		Map<String, ?> sharedState, Map<String, ?> options) {

		logger.trace("initialize.enter; subject: {}; state: {}; options: {}", subject, sharedState, options);
		
		this.subject = subject;
		this.callbackHandler = callbackHandler;
	}
	
	public boolean login() throws LoginException {
		//CredentialsCallback callback = new CredentialsCallback();
		//try {
		//	callbackHandler.handle(new Callback[] {callback});
		//	credentials = cb.getCredentials();
		//} catch (Exception e) {
		//	throw new LoginException(e.getMessage());
		//}
		logger.trace("login.enter;");
		return true;
	}

	@Override
	public boolean abort() throws LoginException {
		logger.trace("abort.enter;");
		return false;
	}

	@Override
	public boolean commit() throws LoginException {
		logger.trace("commit.enter;");
		return true;
	}

	@Override
	public boolean logout() throws LoginException {
		logger.trace("logout.enter;");
		return false;
	}
	
}
