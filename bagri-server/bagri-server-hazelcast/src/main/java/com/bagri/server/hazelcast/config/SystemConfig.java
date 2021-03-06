package com.bagri.server.hazelcast.config;

import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.bagri.core.system.Config;
import com.bagri.core.system.DataFormat;
import com.bagri.core.system.DataStore;
import com.bagri.core.system.Entity;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Node;
import com.bagri.core.system.Schema;

public class SystemConfig extends EntityConfig {

	private Config config;

	public SystemConfig(String configPath) {
		super(configPath);
		try {
			jctx = JAXBContext.newInstance(Config.class);
			config = (Config) loadConfig();
		} catch (JAXBException ex) {
			logger.error("init.error: " + ex.getMessage(), ex);
			//throw new RuntimeException(ex);
		}
	}
	
	@Override
	public Collection<? extends Entity> getEntities(Class<? extends Entity> entityClass) {
		if (entityClass == Node.class) {
			return config.getNodes();
		}
		if (entityClass == Schema.class) {
			return config.getSchemas();
		}
		if (entityClass == Module.class) {
			return config.getModules();
		}
		if (entityClass == Library.class) {
			return config.getLibraries();
		}
		if (entityClass == DataFormat.class) {
			return config.getDataFormats();
		}
		if (entityClass == DataStore.class) {
			return config.getDataStores();
		}
		// throw ex ?
		return null;
	}

	@Override
	public void setEntities(Class<? extends Entity> entityClass,	Collection<? extends Entity> entities) {
		if (entityClass == Node.class) {
			setEntities(config, config.getNodes(), entities);
		} else if (entityClass == Schema.class) {
			setEntities(config, config.getSchemas(), entities);
		} else if (entityClass == Module.class) {
			setEntities(config, config.getModules(), entities);
		} else if (entityClass == Library.class) {
			setEntities(config, config.getLibraries(), entities);
		} else if (entityClass == DataFormat.class) {
			setEntities(config, config.getDataFormats(), entities);
		} else if (entityClass == DataStore.class) {
			setEntities(config, config.getDataStores(), entities);
		} else {
			// throw ex?
		}
	}

}
