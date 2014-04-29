package com.bagri.xdm.cache.hazelcast.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.access.api.XDMNodeManager;
import com.bagri.xdm.system.XDMNode;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;

@ManagedResource(description="User Manager MBean")
public class UserManager implements SelfNaming {

    private static final transient Logger logger = LoggerFactory.getLogger(UserManager.class);
	private static final String type_user = "User";

	private String userName;
    //private HazelcastInstance hzInstance;
	//private IExecutorService execService;
	
	//@Autowired
    private IMap<String, XDMUser> userCache;
    
	public UserManager() {
		//this.userName = userName;
	}

	public UserManager(String userName) {
		this.userName = userName;
	}

    //public UserManager(HazelcastInstance hzInstance, String userName) {
	//	this.hzInstance = hzInstance;
	//	this.userName = userName;
		//execService = hzInstance.getExecutorService("xdm-exec-pool");
	//	userCache = hzInstance.getMap("users");
	//}
	
	public void setUserCache(IMap<String, XDMUser> userCache) {
		this.userCache = userCache;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	@ManagedOperation(description="Activates/Deactivates User")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "login", description = "User login"),
		@ManagedOperationParameter(name = "activate", description = "Activate or Deactivate the User identifieb by login")})
	public boolean activateUser(String login, boolean activate) {
		// TODO Auto-generated method stub
		return false;
	}

	@ManagedOperation(description="Changes User password")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "login", description = "User login"),
		@ManagedOperationParameter(name = "password", description = "New User's password")})
	public boolean changePassword(String login, String password) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		logger.debug("getObjectName.enter; userName: {}", userName);
		return JMXUtils.getObjectName(type_user, userName);
	}


}
