package com.bagri.xdm.cache.hazelcast.store;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bagri.xdm.XDMSchema;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class XDMSchemaCacheStore implements MapStore<String, XDMSchema>, MapLoaderLifecycleSupport {

    private static final transient Logger logger = LoggerFactory.getLogger(XDMSchemaCacheStore.class);
    private static final String schemas_file_name = "xdm.schemas.filename";
    
    private Properties props = new Properties();
    private Map<String, XDMSchema> schemas = new HashMap<String, XDMSchema>();

	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; cache: {}; properties: {}", mapName, properties);
		props.putAll(properties);
		loadSchemas();
	}

	@Override
	public void destroy() {
		logger.trace("destroy.enter");
	}
    
	@Override
	public XDMSchema load(String key) {
		logger.trace("load.enter; key: {}", key);
		return null;
	}

	@Override
	public Map<String, XDMSchema> loadAll(Collection<String> keys) {
		logger.trace("loadAll.enter; keys: {}", keys);
		return schemas;
	}

	@Override
	public Set<String> loadAllKeys() {
		logger.trace("loadAllKeys.enter; ");
		Set<String> result = new HashSet<String>(schemas.keySet());
		return result;
	}

	@Override
	public void store(String key, XDMSchema value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
	}

	@Override
	public void storeAll(Map<String, XDMSchema> map) {
		logger.trace("storeAll.enter; map: {}", map);
	}

	@Override
	public void delete(String key) {
		logger.trace("delete.enter; key: {}", key);
	}

	@Override
	public void deleteAll(Collection<String> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys);
	}

	private void loadSchemas() {
		String schemasFileName = props.getProperty(schemas_file_name);
		
		// read file ...
		try {
			  
			File xmlFile = new File(schemasFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			 
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			//doc.getDocumentElement().normalize();
			 
			NodeList sList = doc.getElementsByTagName("schema"); //getDocumentElement().getChildNodes();
			for (int i = 0; i < sList.getLength(); i++) {
				Element sElt = (Element) sList.item(i);
				String name = sElt.getAttribute("name");
				boolean active = Boolean.valueOf(sElt.getAttribute("active"));
				NodeList sNodes = sElt.getChildNodes();
				String desc = null;
				String at = null;
				String by = null;
				Properties sProps = null;
				for (int j=0; j < sNodes.getLength(); j++) {
					Node node = sNodes.item(j);
					if ("description".equals(node.getNodeName())) {
						desc = node.getTextContent();
					} else if ("createdAt".equals(node.getNodeName())) {
						at = node.getTextContent();
					} else if ("createdBy".equals(node.getNodeName())) {
						by = node.getTextContent();
					} else if ("properties".equals(node.getNodeName())) {
						NodeList pNodes = ((Element) node).getElementsByTagName("property"); 
						sProps = new Properties(); //pNodes.getLength());
						for (int k=0; k < pNodes.getLength(); k++) {
							Element prop = (Element) pNodes.item(k);
							sProps.put(prop.getAttribute("name"), prop.getTextContent());
						}
					} else {
						logger.info("loadSchema. unknown schema node: {}", node);
					}
				}
				XDMSchema schema = new XDMSchema(name, desc, active, new Date(), by, sProps);
				schemas.put(name, schema);
			}
	    } catch (Exception ex) {
			logger.error("loadSchema.error: " + ex.getMessage(), ex);
	    }
	}

}
