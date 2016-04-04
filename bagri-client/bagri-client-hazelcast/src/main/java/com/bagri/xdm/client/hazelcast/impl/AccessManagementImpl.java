package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMAccessManagement;
import com.bagri.xdm.client.hazelcast.task.auth.UserAuthenticator;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

public class AccessManagementImpl implements XDMAccessManagement {

    private final static Logger logger = LoggerFactory.getLogger(AccessManagementImpl.class);
	
	private RepositoryImpl repo;
	private IExecutorService execService;
	
	void initialize(RepositoryImpl repo) {
		this.repo = repo;
		HazelcastInstance hzClient = repo.getHazelcastClient();
		execService = hzClient.getExecutorService(PN_XDM_SCHEMA_POOL);
	}
	
	@Override
	public boolean authenticate(String username, String password) {
		logger.trace("authenticate.enter; got username: {}", username);
		UserAuthenticator auth = new UserAuthenticator(username, password);
		Future<Boolean> future = execService.submit(auth);
		try {
			Boolean result = future.get();
			logger.trace("authenticate.exit; returning: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("authenticate; error getting result", ex);
			//throw new XDMException(ex, XDMException.ecDocument);
		}
		return false;
	}

	@Override
	public boolean hasPermission(String username, Permission permission) {
		// not implemented on client side. yet?
		return false;
	}
	
}


