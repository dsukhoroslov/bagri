package com.bagri.xdm.cache.hazelcast.config;

import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.bagri.xdm.system.Access;
import com.bagri.xdm.system.Role;
import com.bagri.xdm.system.User;
import com.bagri.xdm.system.Entity;

public class AccessConfig extends EntityConfig {

	private Access config;
	
	public AccessConfig(String configPath) {
		super(configPath);
		try {
			jctx = JAXBContext.newInstance(Access.class);
			config = (Access) loadConfig();
		} catch (JAXBException ex) {
			logger.error("init.error: " + ex.getMessage(), ex);
			//throw new RuntimeException(ex);
		}
	}
	
	@Override
	public Collection<? extends Entity> getEntities(Class<? extends Entity> entityClass) {
		if (entityClass == User.class) {
			return config.getUsers();
		}
		if (entityClass == Role.class) {
			return config.getRoles();
		}
		// throw ex ?
		return null;
	}

	@Override
	public void setEntities(Class<? extends Entity> entityClass,	Collection<? extends Entity> entities) {
		if (entityClass == User.class) {
			setEntities(config, config.getUsers(), entities);
		} else if (entityClass == Role.class) {
			setEntities(config, config.getRoles(), entities);
		} else {
			// throw ex?
		}
	}

}
