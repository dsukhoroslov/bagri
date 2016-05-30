package com.bagri.xdm.cache.hazelcast.config;

import java.io.File;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.common.XDMEntity;

public abstract class EntityConfig {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    
	protected String configPath;
	protected JAXBContext jctx;
    
	public EntityConfig(String configPath) {
		this.configPath = configPath;
	}
	
	protected void setEntities(Object config, Collection oldEntities, Collection newEntities) {
		oldEntities.clear();
		oldEntities.addAll(newEntities);
		try {
			storeConfig(config);
		} catch (JAXBException ex) {
			logger.error("setEntries.error: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}
	
	protected Object loadConfig() throws JAXBException {
        Unmarshaller unmarshaller = jctx.createUnmarshaller();
        File xml = new File(configPath);
        return unmarshaller.unmarshal(xml);
	}
	
	private void storeConfig(Object config) throws JAXBException {
        Marshaller marshaller = jctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "file:///C:/Documents%20and%20Settings/mojalal/Desktop/FirstXSD.xml");
        File xml = new File(configPath);
        marshaller.marshal(config, xml);
	}

	public abstract Collection<? extends XDMEntity> getEntities(Class<? extends XDMEntity> entityClass);
	public abstract void setEntities(Class<? extends XDMEntity> entityClass, Collection<? extends XDMEntity> entities);
	
}
