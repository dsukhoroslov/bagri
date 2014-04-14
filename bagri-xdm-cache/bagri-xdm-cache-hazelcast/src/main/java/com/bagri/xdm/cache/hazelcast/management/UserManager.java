package com.bagri.xdm.cache.hazelcast.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.XDMNode;
import com.bagri.xdm.XDMUser;
import com.bagri.xdm.access.api.XDMNodeManager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;

public class UserManager implements InitializingBean, UserManagerMBean {

    private static final transient Logger logger = LoggerFactory.getLogger(UserManager.class);
	private static final String type_user = "User";

	private String userName;
    private HazelcastInstance hzInstance;
	//private IExecutorService execService;
    private IMap<String, XDMUser> userCache;
    
	public UserManager(HazelcastInstance hzInstance, String userName) {
		this.hzInstance = hzInstance;
		this.userName = userName;
		//execService = hzInstance.getExecutorService("xdm-exec-pool");
		userCache = hzInstance.getMap("users");
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		JMXUtils.registerMBean(type_user, userName, this);
	}
	
	public void close() {
		JMXUtils.unregisterMBean(type_user, userName);
	}

	@Override
	public boolean activateUser(String login, boolean activate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean changePassword(String login, String password) {
		// TODO Auto-generated method stub
		return false;
	}


}
