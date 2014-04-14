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
import com.bagri.xdm.XDMUser;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class XDMUserCacheStore implements MapStore<String, XDMUser>, MapLoaderLifecycleSupport {

    private static final transient Logger logger = LoggerFactory.getLogger(XDMUserCacheStore.class);
    private static final String users_file_name = "xdm.users.filename";
    
    private Properties props = new Properties();
    private Map<String, XDMUser> users = new HashMap<String, XDMUser>();

	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; cache: {}; properties: {}", mapName, properties);
		props.putAll(properties);
		loadUsers();
	}

	@Override
	public void destroy() {
		logger.trace("destroy.enter");
	}
    
	@Override
	public XDMUser load(String key) {
		logger.trace("load.enter; key: {}", key);
		return null;
	}

	@Override
	public Map<String, XDMUser> loadAll(Collection<String> keys) {
		logger.trace("loadAll.enter; keys: {}", keys);
		return users;
	}

	@Override
	public Set<String> loadAllKeys() {
		logger.trace("loadAllKeys.enter; ");
		Set<String> result = new HashSet<String>(users.keySet());
		return result;
	}

	@Override
	public void store(String key, XDMUser value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
	}

	@Override
	public void storeAll(Map<String, XDMUser> map) {
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

	private void loadUsers() {
		String usersFileName = props.getProperty(users_file_name);
		
		// read file ...
		try {
			  
			File xmlFile = new File(usersFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			 
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			//doc.getDocumentElement().normalize();
			 
			NodeList sList = doc.getElementsByTagName("user"); //getDocumentElement().getChildNodes();
			for (int i = 0; i < sList.getLength(); i++) {
				Element sElt = (Element) sList.item(i);
				String login = sElt.getAttribute("login");
				boolean active = Boolean.valueOf(sElt.getAttribute("active"));
				NodeList sNodes = sElt.getChildNodes();
				String pass = null;
				String at = null;
				String by = null;
				Properties sProps = null;
				for (int j=0; j < sNodes.getLength(); j++) {
					Node node = sNodes.item(j);
					if ("password".equals(node.getNodeName())) {
						pass = node.getTextContent();
					} else if ("createdAt".equals(node.getNodeName())) {
						at = node.getTextContent();
					} else if ("createdBy".equals(node.getNodeName())) {
						by = node.getTextContent();
					} else {
						logger.info("loadUsers. unknown user node: {}", node);
					}
				}
				XDMUser user = new XDMUser(login, pass, active, new Date(), by);
				users.put(login, user);
			}
	    } catch (Exception ex) {
			logger.error("loadUsers.error: " + ex.getMessage(), ex);
	    }
	}



}
