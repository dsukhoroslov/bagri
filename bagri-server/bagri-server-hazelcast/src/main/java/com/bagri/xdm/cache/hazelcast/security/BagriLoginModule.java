package com.bagri.xdm.cache.hazelcast.security;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class BagriLoginModule implements LoginModule {
		
    private static final transient Logger logger = LoggerFactory.getLogger(BagriLoginModule.class);
	
	private Subject subject;
	private CallbackHandler callbackHandler;
	
	public BagriLoginModule() {
		logger.trace("<init>; login module initialized"); 
	}
		
	public void initialize(Subject subject, CallbackHandler callbackHandler,
		Map<String, ?> sharedState, Map<String, ?> options) {

		logger.trace("initialize.enter; subject: {}; state: {}; options: {}; callback: {}", 
				subject, sharedState, options, callbackHandler);
		
		this.subject = subject;
		this.callbackHandler = callbackHandler;
	}
	
	public boolean login() throws LoginException {
		logger.trace("login.enter;");

        // Setup default callback handlers.
        Callback[] callbacks = new Callback[] {
            new NameCallback("Username: "),
            new PasswordCallback("Password: ", false)
        };
 
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception ex) {
            //_succeeded = false;
        	logger.error("login.error: " + ex.getMessage(), ex);
            throw new LoginException(ex.getMessage());
        }
 
        String username = ((NameCallback)callbacks[0]).getName();
        String password = new String(((PasswordCallback)callbacks[1]).getPassword());		

        logger.debug("login; user: {}; password: {}", username, password);
        
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
