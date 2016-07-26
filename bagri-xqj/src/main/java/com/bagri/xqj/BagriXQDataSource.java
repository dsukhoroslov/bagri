package com.bagri.xqj;

import static com.bagri.xdm.common.Constants.*;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xquery.api.XQProcessor;

/**
 * @author Denis Sukhoroslov
 * @see javax.xml.xquery.XQDataSource
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
	public static final String TRANSACTIONAL = "transactional";

	public static final String XQ_PROCESSOR = "query.processor";
	public static final String XDM_REPOSITORY = "xdm.repository";
	
	// TODO: make some relevant writer which will do logging
	private PrintWriter writer;
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
		properties.put(PASSWORD, "password");
		properties.put(SCHEMA, "default");
		properties.put(TRANSACTIONAL, "false");
		properties.put(pn_client_fetchSize, "50");
		properties.put(pn_client_loginTimeout, "30");
		properties.put(pn_client_bufferSize, "32"); 
		properties.put(pn_client_connectAttempts, "3");
		properties.put(XQ_PROCESSOR, ""); //"com.bagri.xquery.saxon.BagriXQProcessor"); //Proxy
		properties.put(XDM_REPOSITORY, ""); //"com.bagri.xdm.client.hazelcast.impl.SchemaRepositoryImpl"); 
	}

	/** {@inheritDoc}
	 */
	@Override
	public XQConnection getConnection() throws XQException {
		
		String address = getAddress();
		logger.trace("getConnection. creating new connection for address: {}", address);
		return initConnection(null);
	}
	
	/** {@inheritDoc} */
	@Override
	public XQConnection getConnection(Connection connection) throws XQException {
		
		// will work only if the Connection provided is an 
		// another connection to the underlying cache
		throw new XQException("method not supported"); 
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public XQConnection getConnection(String username, String password) throws XQException {
		
		String address = getAddress();
		logger.trace("getConnection. creating new connection for address: {}; user: {}", address, username);
		properties.put(USER, username);
		properties.put(PASSWORD, password);
		return initConnection(username);
	}

	private String getAddress() {
		String address = properties.getProperty(ADDRESS);
		if (address == null) {
			address = properties.getProperty(HOST) + ":" + properties.getProperty(PORT);
		}
		return address; 
	}
	
	private boolean isTransactional() {
		String transactional = properties.getProperty(TRANSACTIONAL);
		return ("true".equalsIgnoreCase(transactional));
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
					Properties props = new Properties(); //properties);
					props.putAll(properties);
					props.put(pn_client_dataFactory, connect);
					return init.newInstance(props);
				}
			} catch (Exception ex) {
				logger.error("initRepository. error creating Repository of type " + className + 
						" with Properties. Falling back to default constructor", ex);
			}
			
			return procClass.newInstance(); 
		} catch (ClassNotFoundException ex) {
			throw new XQException("Unknown class: " + className);
		} catch (InstantiationException | IllegalAccessException ex) { 
			throw new XQException("Cannot instantiate " + className + ". Exception: " + ex.getMessage());
		}
	}
	
	private XQConnection initConnection(String username) throws XQException {

		BagriXQConnection connect = new BagriXQConnection(username, isTransactional());
		if (connect.getProcessor() == null) {
			Object xqp = makeInstance(XQ_PROCESSOR);
			if (xqp != null) {
				if (xqp instanceof XQProcessor) {
					connect.setProcessor((XQProcessor) xqp);
					((XQProcessor) xqp).setXQDataFactory(connect);

					Object xdm = initRepository(connect);
					if (xdm != null) {
						if (xdm instanceof SchemaRepository) {
							((XQProcessor) xqp).setRepository((SchemaRepository) xdm);
						} else {
							throw new XQException("Specified Repository class does not implement XDMRepository interface: " + 
									properties.getProperty(XDM_REPOSITORY));
						}
					}						
				} else {
					throw new XQException("Specified XQ Processor class does not implement XQProcessor interface: " + 
							properties.getProperty(XQ_PROCESSOR));
				}
			}
		}
		return connect;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PrintWriter getLogWriter() throws XQException {
		
		return writer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLoginTimeout() throws XQException {
		
		return Integer.parseInt(properties.getProperty(pn_client_loginTimeout));
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getSupportedPropertyNames() {
		
		return properties.keySet().toArray(new String[properties.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLogWriter(PrintWriter writer) throws XQException {
		
		//if (writer == null) {
		//	throw new XQException("writer is null");
		//}
		this.writer = writer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLoginTimeout(int timeout) throws XQException {
		
		properties.setProperty(pn_client_loginTimeout, String.valueOf(timeout));
	}

	/** {@inheritDoc}
	 */
	@Override
	public void setProperties(Properties props) throws XQException {

		if (props == null) {
			throw new XQException("Properties are null");
		}
		for (Map.Entry prop: props.entrySet()) {
			setProperty((String) prop.getKey(), (String) prop.getValue());
		}
	}

	/** 
	 * {@inheritDoc}
	 * @see javax.xml.xquery.XQDataSource#setProperty(String, String)
	 */
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
