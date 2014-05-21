package com.bagri.xdm.cache.hazelcast.management;

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.system.XDMConfig;
import com.bagri.xdm.system.XDMNode;
import com.bagri.xdm.system.XDMSchema;

public class ConfigManagement {

    private static final transient Logger logger = LoggerFactory.getLogger(ConfigManagement.class);
	
	private XDMConfig config;
	private String configPath;
	private JAXBContext jctx;
	
	public ConfigManagement(String configPath) {
		this.configPath = configPath;
		try {
			jctx = JAXBContext.newInstance(XDMConfig.class);
			loadConfig();
		} catch (JAXBException ex) {
			logger.error("init.error: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}
	
	public List<XDMNode> getNodes() {
		return config.getNodes();
	}
	
	public void setNodes(Collection<XDMNode> nodes) {
		List<XDMNode> oldNodes = config.getNodes();
		oldNodes.clear();
		oldNodes.addAll(nodes);
		try {
			storeConfig();
		} catch (JAXBException ex) {
			logger.error("setNodes.error: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	public List<XDMSchema> getSchemas() {
		return config.getSchemas();
	}

	public void setSchemas(Collection<XDMSchema> schemas) {
		List<XDMSchema> oldSchemas = config.getSchemas();
		oldSchemas.clear();
		oldSchemas.addAll(schemas);
		try {
			storeConfig();
		} catch (JAXBException ex) {
			logger.error("setSchemas.error: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	private void loadConfig() throws JAXBException {
        Unmarshaller unmarshaller = jctx.createUnmarshaller();
        File xml = new File(configPath);
        config = (XDMConfig) unmarshaller.unmarshal(xml);
	}
	
	private void storeConfig() throws JAXBException {
        Marshaller marshaller = jctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "file:///C:/Documents%20and%20Settings/mojalal/Desktop/FirstXSD.xml");
        File xml = new File(configPath);
        marshaller.marshal(config, xml);
	}
	
}
