package com.bagri.xdm.cache.hazelcast.management;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.process.hazelcast.user.UserUpdater;
import com.bagri.xdm.system.XDMUser;

@ManagedResource(description="User Manager MBean")
public class UserManager extends EntityManager<XDMUser> {

	public UserManager() {
		super();
	}

	public UserManager(String userName) {
		super(userName);
	}

	@ManagedAttribute(description="Returns User name")
	public String getName() {
		return entityName;
	}
	
	@ManagedAttribute(description="Returns User version")
	public int getVersion() {
		return super.getVersion();
	}

	@ManagedAttribute(description="Returns User state")
	public boolean isActive() {
		return getEntity().isActive();
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
	public boolean changePassword(String login, String oldPassword, String newPassword) {
		XDMUser user = getEntity();
		if (user != null) {
	    	Object result = entityCache.executeOnKey(login, new UserUpdater(user.getVersion(), 
	    			JMXUtils.getCurrentUser(), oldPassword, newPassword));
	    	logger.trace("changePassword; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@Override
	protected String getEntityType() {
		return "User";
	}

}
