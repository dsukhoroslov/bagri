package com.bagri.xqj;

import javax.xml.xquery.ConnectionPoolXQDataSource;
import javax.xml.xquery.PooledXQConnection;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;

public class BagriConnectionPoolXQDataSource extends BagriXQDataSource implements ConnectionPoolXQDataSource {
	
	public BagriConnectionPoolXQDataSource() {
		super();
		// add pool-related properties here
	}
	
	@Override
	protected BagriXQConnection createConnection(String username) {
		return new BagriLogicalXQConnection(username, isTransactional());
	}

	@Override
	public XQConnection getConnection() throws XQException {
		// get pooled connection and create logical connection from it..
		return null;
	}

	@Override
	public XQConnection getConnection(String user, String password) throws XQException {
		// as above
		return null;
	}

	@Override
	public PooledXQConnection getPooledConnection() throws XQException {
		// create new or get it from pool?
		return null;
	}

	@Override
	public PooledXQConnection getPooledConnection(String user, String password) throws XQException {
		// as above
		return null;
	}

}
