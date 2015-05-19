package com.bagri.xdm.cache.hazelcast.management;

import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.bagri.xdm.common.XDMEntity;
import com.bagri.xdm.system.XDMConfig;
import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMNode;
import com.bagri.xdm.system.XDMSchema;

public class ConfigManagement extends EntityConfig {

	private XDMConfig config;

	public ConfigManagement(String configPath) {
		super(configPath);
		try {
			jctx = JAXBContext.newInstance(XDMConfig.class);
			config = (XDMConfig) loadConfig();
		} catch (JAXBException ex) {
			logger.error("init.error: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public Collection<? extends XDMEntity> getEntities(Class<? extends XDMEntity> entityClass) {
		if (entityClass == XDMNode.class) {
			return config.getNodes();
		}
		if (entityClass == XDMSchema.class) {
			return config.getSchemas();
		}
		if (entityClass == XDMModule.class) {
			return config.getModules();
		}
		if (entityClass == XDMLibrary.class) {
			return config.getLibraries();
		}
		// throw ex ?
		return null;
	}

	@Override
	public void setEntities(Class<? extends XDMEntity> entityClass,	Collection<? extends XDMEntity> entities) {
		if (entityClass == XDMNode.class) {
			setEntities(config, config.getNodes(), entities);
		} else if (entityClass == XDMSchema.class) {
			setEntities(config, config.getSchemas(), entities);
		} else if (entityClass == XDMModule.class) {
			setEntities(config, config.getModules(), entities);
		} else if (entityClass == XDMLibrary.class) {
			setEntities(config, config.getLibraries(), entities);
		} else {
			// throw ex?
		}
	}

}
