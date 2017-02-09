package com.bagri.xqj;

import javax.xml.xquery.ConnectionPoolXQDataSource;
import javax.xml.xquery.PooledXQConnection;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;

public class BagriConnectionPoolXQDataSource extends BagriXQDataSource implements ConnectionPoolXQDataSource {
	
	public static final String MIN_POOL_SIZE = "min_pool_size";
	public static final String MAX_POOL_SIZE = "max_pool_size";
	
	public BagriConnectionPoolXQDataSource() {
		super();
		// add pool-related properties here
		//properties.setProperty(MIN_POOL_SIZE, "1");
		//properties.setProperty(MAX_POOL_SIZE, "50");
	}
	
	@Override
	protected BagriXQConnection createConnection(String username) {
		return new BagriLogicalXQConnection(username, isTransactional());
	}

	@Override
	public XQConnection getConnection() throws XQException {
		// get pooled connection and create logical connection from it..
		return super.getConnection();
	}

	@Override
	public XQConnection getConnection(String user, String password) throws XQException {
		// as above
		return super.getConnection(user, password);
	}

	@Override
	public PooledXQConnection getPooledConnection() throws XQException {
		// creates new connection.
		// I have to pass it LogicalXQConn somehow. 
		// where should I take it??
		return new BagriPooledXQConnection(getConnection());
	}

	@Override
	public PooledXQConnection getPooledConnection(String user, String password) throws XQException {
		// as above
		return new BagriPooledXQConnection(getConnection(user, password));
	}

}
