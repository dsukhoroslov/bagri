package com.bagri.xqj;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xquery.api.XQProcessor;

/**
 * @author Denis Sukhoroslov
 * date: 07.02.2013
 *
 */
public class BagriXQDataSource implements XQDataSource {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriXQDataSource.class);
	
    // must be range of hosts/ports
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String SCHEMA = "schema";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	public static final String ADDRESS = "address";
	
	public static final String XQ_PROCESSOR = "query.processor";
	public static final String XDM_REPOSITORY = "xdm.repository";
	
	private PrintWriter writer;
	private int timeout = 100;
	private Properties properties = new Properties();
	
	// DataSource initialization: init query processor
	// connection -> set processor
	// processor -> set XDM
	// XDM -> initialize dictionary, factory
	
	public BagriXQDataSource() {
		// ...
		//properties.put(HOST, "localhost");
		//properties.put(PORT, "5701");
		properties.put(ADDRESS, "localhost:5701");
		properties.put(USER, "anonymous");
		properties.put(PASSWORD, "syspwd");
		properties.put(SCHEMA, "system");
		properties.put(XQ_PROCESSOR, ""); //"com.bagri.xquery.saxon.BagriXQProcessor"); //Proxy
		properties.put(XDM_REPOSITORY, ""); //"com.bagri.xdm.client.hazelcast.impl.RepositoryImpl"); 
	}

	@Override
	public XQConnection getConnection() throws XQException {
		
		String address = getAddress();
		logger.trace("getConnection. creating new connection for address: {}", address);
		return initConnection(address, timeout);
	}
	
	@Override
	public XQConnection getConnection(Connection connection) throws XQException {
		
		// will work only if the Connection provided is an 
		// another connection to the underlying cache
		throw new XQException("method not supported"); //return null;
	}
	
	@Override
	public XQConnection getConnection(String username, String password) throws XQException {
		
		String address = getAddress();
		logger.trace("getConnection. creating new connection for address: {}; user: {}", address, username);
		//return new BagriXQConnection(address, timeout); //, username, password);
		properties.put(USER, username);
		properties.put(PASSWORD, password);
		return initConnection(address, timeout);
	}

	private String getAddress() {
		String address = properties.getProperty(ADDRESS);
		if (address == null) {
			address = properties.getProperty(HOST) + ":" + properties.getProperty(PORT);
		}
		return address; 
	}

	private Object makeInstance(String propName) throws XQException {

		String className = properties.getProperty(propName);
		if (className == null || className.trim().length() == 0) {
			return null; 
		}
		
		try {
			Class procClass = Class.forName(className);
			Object instance = procClass.newInstance();
			return instance;
		} catch (ClassNotFoundException ex) {
			throw new XQException("Unknown " + propName + " class: " + className);
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new XQException("Cannot instantiate " + className + ". Exception: " + ex.getMessage());
		}
	}
	
	private Object initRepository(BagriXQConnection connect) throws XQException {

		String className = properties.getProperty(XDM_REPOSITORY);
		if (className == null || className.trim().length() == 0) {
			return null; 
		}
		
		try {
			Class procClass = Class.forName(className);
			
			try {
				Constructor init = procClass.getConstructor(Properties.class);
				if (init != null) {
					Properties props = new Properties(properties);
					props.put("xqDataFactory", connect);
					return init.newInstance(props);
				}
			} catch (Exception ex) {
				logger.error("initRepository. error creating Repository of type " + className + 
						"with Properties. Falling back to default constructor", ex);
			}
			
			return procClass.newInstance(); 
		} catch (ClassNotFoundException ex) {
			throw new XQException("Unknown class: " + className);
		} catch (InstantiationException | IllegalAccessException ex) { 
			throw new XQException("Cannot instantiate " + className + ". Exception: " + ex.getMessage());
		}
		
	}
	
	private XQConnection initConnection(String address, int timeout) throws XQException {

		BagriXQConnection connect = new BagriXQConnection(address, timeout);
		if (connect.getProcessor() == null) {
			Object xqp = makeInstance(XQ_PROCESSOR);
			if (xqp != null) {
				if (xqp instanceof XQProcessor) {
					//Object xdm = makeInstance(XDM_MANAGER);
					Object xdm = initRepository(connect);
					if (xdm != null) {
						if (xdm instanceof XDMRepository) {
							((XQProcessor) xqp).setRepository((XDMRepository) xdm);
						} else {
							throw new XQException("Specified Repository class does not implement XDMRepository interface: " + 
									properties.getProperty(XDM_REPOSITORY));
						}
					}						
					connect.setProcessor((XQProcessor) xqp);
					((XQProcessor) xqp).setXQDataFactory(connect);
				} else {
					throw new XQException("Specified XQ Processor class does not implement XQProcessor interface: " + 
							properties.getProperty(XQ_PROCESSOR));
				}
			}
		}
		return connect;
	}

	@Override
	public PrintWriter getLogWriter() throws XQException {
		
		return writer;
	}

	@Override
	public int getLoginTimeout() throws XQException {
		
		return timeout;
	}

	@Override
	public String getProperty(String name) throws XQException {
		
		if (name == null) {
			throw new XQException("name is null");
		}
		if (!properties.containsKey(name)) {
			throw new XQException("unknown property: " + name);
		}
		return properties.getProperty(name);
	}

	@Override
	public String[] getSupportedPropertyNames() {
		
		return properties.keySet().toArray(new String[properties.size()]);
	}

	@Override
	public void setLogWriter(PrintWriter writer) throws XQException {
		
		//if (writer == null) {
		//	throw new XQException("writer is null");
		//}
		this.writer = writer;
	}

	@Override
	public void setLoginTimeout(int timeout) throws XQException {
		
		this.timeout = timeout;
	}

	@Override
	public void setProperties(Properties props) throws XQException {

		if (props == null) {
			throw new XQException("Properties are null");
		}
		for (Map.Entry prop: props.entrySet()) {
			setProperty((String) prop.getKey(), (String) prop.getValue());
		}
	}

	@Override
	public void setProperty(String name, String value) throws XQException {
		
		if (name == null) {
			throw new XQException("name is null");
		}
		if (!properties.containsKey(name)) {
			throw new XQException("unknown property: " + name);
		}
		properties.setProperty(name, value);
	}

}
