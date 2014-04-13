/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.XDMSchema;
import com.bagri.xdm.XDMUser;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;

/**
 * @author Denis Sukhoroslov
 *
 */
public class UserManagement implements InitializingBean, UserManagementMBean {

    private static final transient Logger logger = LoggerFactory.getLogger(UserManagement.class);
	private static final String user_management = "UserManagement";
	private static final String type_management = "Management";
    
	//private Properties defaults; 
    private HazelcastInstance hzInstance;
	//private IExecutorService execService;
    private IMap<String, XDMUser> userCache;
    
	public UserManagement(HazelcastInstance hzInstance) {
		//super();
		this.hzInstance = hzInstance;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        Set<String> names = userCache.keySet();
        for (String name: names) {
        	XDMUser user = userCache.get(name);
        	if (user.isActive()) {
        		//initUser(user);
        	}
        }
		
		JMXUtils.registerMBean(type_management, user_management, this);
	}
	
	public void setUserCache(IMap<String, XDMUser> userCache) {
		this.userCache = userCache;
	}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.cache.hazelcast.management.UserManagementMBean#getUserNames()
	 */
	@Override
	public String[] getUserNames() {
		return userCache.keySet().toArray(new String[0]);
	}
	
	//@Override
	public Collection<XDMUser> getUsers() {
		return new ArrayList<XDMUser>(userCache.values());
	}


	/* (non-Javadoc)
	 * @see com.bagri.xdm.cache.hazelcast.management.UserManagementMBean#adduser(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addUser(String login, String password) {
		XDMUser user = new XDMUser(login, password, true, new Date(), user_management);
		userCache.put(login, user);
		// init UserManager
		return true;
	}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.cache.hazelcast.management.UserManagementMBean#deleteUser(java.lang.String)
	 */
	@Override
	public boolean deleteUser(String login) {
		// denit UserManager
		return userCache.remove(login) != null;
	}


}
