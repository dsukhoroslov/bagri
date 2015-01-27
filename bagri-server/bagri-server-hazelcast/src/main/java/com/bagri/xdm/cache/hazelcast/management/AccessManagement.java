package com.bagri.xdm.cache.hazelcast.management;

import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.bagri.xdm.common.XDMEntity;
import com.bagri.xdm.system.XDMAccess;
import com.bagri.xdm.system.XDMRole;
import com.bagri.xdm.system.XDMUser;

public class AccessManagement extends EntityConfig {

	private XDMAccess config;
	
	public AccessManagement(String configPath) {
		super(configPath);
		try {
			jctx = JAXBContext.newInstance(XDMAccess.class);
			config = (XDMAccess) loadConfig();
		} catch (JAXBException ex) {
			logger.error("init.error: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public Collection<? extends XDMEntity> getEntities(Class<? extends XDMEntity> entityClass) {
		if (entityClass == XDMUser.class) {
			return config.getUsers();
		}
		if (entityClass == XDMRole.class) {
			return config.getRoles();
		}
		// throw ex ?
		return null;
	}

	@Override
	public void setEntities(Class<? extends XDMEntity> entityClass,	Collection<? extends XDMEntity> entities) {
		if (entityClass == XDMUser.class) {
			setEntities(config, config.getUsers(), entities);
		} else if (entityClass == XDMRole.class) {
			setEntities(config, config.getRoles(), entities);
		} else {
			// throw ex?
		}
	}

}
