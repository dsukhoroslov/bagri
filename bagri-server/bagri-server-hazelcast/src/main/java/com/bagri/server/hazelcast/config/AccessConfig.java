package com.bagri.server.hazelcast.config;

import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.bagri.core.system.Access;
import com.bagri.core.system.Entity;
import com.bagri.core.system.Role;
import com.bagri.core.system.User;

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
