/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.XDMNode;
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
    
	//private Properties defaults; 
    private HazelcastInstance hzInstance;
	//private IExecutorService execService;
    private IMap<String, XDMUser> userCache;
    private Map<String, UserManager> mgrCache = new HashMap<String, UserManager>();
    
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
        		initUser(user);
        	}
        }
		
		JMXUtils.registerMBean(user_management, this);
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


	private boolean initUser(XDMUser user) throws Exception {
		if (!userCache.containsKey(user.getLogin())) {
			userCache.put(user.getLogin(), user);
		}

		if (!mgrCache.containsKey(user.getLogin())) {
			UserManager uMgr = new UserManager(hzInstance, user.getLogin()); 
			mgrCache.put(user.getLogin(), uMgr);
			uMgr.afterPropertiesSet();
			return true;
		}
		return false;
	}
	
	private boolean denitUser(XDMUser user) {
		// find and unreg UserManager...
		UserManager uMgr = mgrCache.remove(user.getLogin());
		if (uMgr != null) {
			uMgr.close();
			return true;
		}
		return false;
	}
	

	/* (non-Javadoc)
	 * @see com.bagri.xdm.cache.hazelcast.management.UserManagementMBean#adduser(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addUser(String login, String password) {
		XDMUser user = new XDMUser(login, password, true, new Date(), user_management);
		try {
			return initUser(user);
		} catch (Exception ex) {
			logger.error("addUser; error: " + ex.getMessage(), ex);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.cache.hazelcast.management.UserManagementMBean#deleteUser(java.lang.String)
	 */
	@Override
	public boolean deleteUser(String login) {
		// denit UserManager
		XDMUser user = userCache.get(login);
		if (user != null) {
			return denitUser(user);
		}
		return false;
	}


}
