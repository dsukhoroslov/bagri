/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.management;

import java.util.Set;

import javax.management.MalformedObjectNameException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.process.hazelcast.user.UserCreator;
import com.bagri.xdm.process.hazelcast.user.UserRemover;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Denis Sukhoroslov email: dsukhoroslov@gmail.com
 *
 */
@ManagedResource(objectName="com.bagri.xdm:type=Management,name=UserManagement", 
	description="User Management MBean")
public class UserManagement extends EntityManagement<String, XDMUser> implements InitializingBean {

	public UserManagement(HazelcastInstance hzInstance) {
		super(hzInstance);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        Set<String> names = entityCache.keySet();
		logger.debug("afterPropertiesSet.enter; got users: {}", names); 
        for (String name: names) {
        	//XDMUser user = userCache.get(name);
       		EntityManager<XDMUser> uMgr = mgrCache.get(name);
       		if (uMgr == null) {
   				logger.debug("afterPropertiesSet; cannot get UserManager for user {}; initializing a new one", name); 
       			try {
       				uMgr = initEntityManager(name);
       			} catch (MBeanExportException | MalformedObjectNameException ex) {
       				// JMX registration failed.
       				logger.error("afterPropertiesSet.error: ", ex);
       			}
       		}
   			//if (uMgr != null) {
   			//	sMgr.setState("Failed user initialization");
   			//}
        }
	}
	
	@ManagedAttribute(description="Registered User Names")
	public String[] getUserNames() {
		return entityCache.keySet().toArray(new String[0]);
	}
	
	@ManagedOperation(description="Create new User")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "login", description = "User login"),
		@ManagedOperationParameter(name = "password", description = "User password")})
	public boolean addUser(String login, String password) {

		if (!entityCache.containsKey(login)) {
	    	Object result = entityCache.executeOnKey(login, new UserCreator(JMXUtils.getCurrentUser(), password));
	    	logger.debug("addUser; execution result: {}", result);
			return true;
		}
		return false;
	}

	@ManagedOperation(description="Delete User")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "login", description = "User login")})
	public boolean deleteUser(String login) {
		//return userCache.remove(login) != null;
		XDMUser user = entityCache.get(login);
		if (user != null) {
	    	Object result = entityCache.executeOnKey(login, new UserRemover(user.getVersion(), JMXUtils.getCurrentUser()));
	    	logger.debug("deleteUser; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@Override
	protected EntityManager<XDMUser> createEntityManager(String userName) {
		UserManager mgr = new UserManager(userName);
		mgr.setEntityCache(entityCache);
		return mgr;
	}
	
	public boolean authenticate(String login, String password) {
		XDMUser user = entityCache.get(login);
		if (user != null) {
			// @TODO: we'll NOT store passwords in open text,
			// will have to hash pwd and compare with original hash
			return password.equals(user.getPassword());
		}
		// throw NotFound exception?
		return false;
	}


}
