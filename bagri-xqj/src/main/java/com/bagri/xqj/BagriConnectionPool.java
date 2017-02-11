package com.bagri.xqj;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.xquery.PooledXQConnection;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConnectionEvent;
import javax.xml.xquery.XQConnectionEventListener;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagriConnectionPool implements XQConnectionEventListener, XQDataSource {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriConnectionPool.class);
	
	private Deque<PooledXQConnection> pool = new LinkedList<>();
	private BagriConnectionPoolXQDataSource poolSource = new BagriConnectionPoolXQDataSource(); 
	
	// take properties, use them to setup poolSource
	// then load pool of connections
	public BagriConnectionPool() {
		//
	}
	
	@Override
	public void connectionClosed(XQConnectionEvent event) {
		PooledXQConnection xqConn = (PooledXQConnection) event.getSource();
		// now return it to pool
		xqConn.removeConnectionEventListener(this);
		pool.add(xqConn);
		logger.debug("connectionClosed; pool size: {}", pool.size());
	}

	@Override
	public void connectionErrorOccurred(XQConnectionEvent event) {
		PooledXQConnection xqConn = (PooledXQConnection) event.getSource();
		// close it and do not return
		xqConn.removeConnectionEventListener(this);
		try {
			xqConn.close();
		} catch (XQException ex) {
			logger.error("connectionErrorOccured. on close: ", ex);
		}
	}

	@Override
	public XQConnection getConnection() throws XQException {
		// get pooled connection from pool
		// if no spare connections - grow pool..?
		PooledXQConnection xqConn;
		synchronized (pool) {
			if (pool.size() > 0) {
				xqConn = pool.pop();
			} else {
				xqConn = poolSource.getPooledConnection(); 
			}
		}
		xqConn.addConnectionEventListener(this);
		XQConnection conn = xqConn.getConnection();
		logger.debug("getConnection; pool size: {}; returning: {}", pool.size(), conn);
		return conn;
	}

	@Override
	public XQConnection getConnection(Connection conn) throws XQException {
		return poolSource.getConnection(conn);
	}

	@Override
	public XQConnection getConnection(String username, String password) throws XQException {
		PooledXQConnection xqConn;
		synchronized (pool) {
			if (pool.size() > 0) {
				xqConn = pool.pop();
			} else {
				xqConn = poolSource.getPooledConnection(username, password); 
			}
		}
		xqConn.addConnectionEventListener(this);
		return xqConn.getConnection();
	}

	@Override
	public int getLoginTimeout() throws XQException {
		return poolSource.getLoginTimeout();
	}

	@Override
	public PrintWriter getLogWriter() throws XQException {
		return poolSource.getLogWriter();
	}

	@Override
	public String[] getSupportedPropertyNames() {
		return poolSource.getSupportedPropertyNames();
	}

	@Override
	public String getProperty(String name) throws XQException {
		return poolSource.getProperty(name);
	}

	@Override
	public void setProperty(String name, String value) throws XQException {
		poolSource.setProperty(name, value);		
	}

	@Override
	public void setProperties(Properties props) throws XQException {
		poolSource.setProperties(props);		
	}

	@Override
	public void setLoginTimeout(int seconds) throws XQException {
		poolSource.setLoginTimeout(seconds);		
	}

	@Override
	public void setLogWriter(PrintWriter out) throws XQException {
		poolSource.setLogWriter(out);		
	}

}
