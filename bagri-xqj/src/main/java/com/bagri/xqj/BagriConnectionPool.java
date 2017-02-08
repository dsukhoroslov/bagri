package com.bagri.xqj;

import java.util.LinkedList;
import java.util.List;

import javax.xml.xquery.PooledXQConnection;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConnectionEvent;
import javax.xml.xquery.XQConnectionEventListener;
import javax.xml.xquery.XQException;

public class BagriConnectionPool implements XQConnectionEventListener {
	
	private List<PooledXQConnection> pool = new LinkedList<>();
	private BagriConnectionPoolXQDataSource poolSource;
	
	// take properties, use them to setup poolSource
	// then load pool of connections
	
	public XQConnection getConnection() throws XQException {
		// get pooled connection from pool
		// if no spare connections - grow pool..?
		PooledXQConnection xqConn = pool.get(0);
		xqConn.addConnectionEventListener(this);
		return xqConn.getConnection();
	}

	@Override
	public void connectionClosed(XQConnectionEvent event) {
		PooledXQConnection xqConn = (PooledXQConnection) event.getSource();
		// now return it to pool
		xqConn.removeConnectionEventListener(this);
		pool.add(xqConn);
	}

	@Override
	public void connectionErrorOccurred(XQConnectionEvent event) {
		PooledXQConnection xqConn = (PooledXQConnection) event.getSource();
		// close it and do not return
		xqConn.removeConnectionEventListener(this);
		try {
			xqConn.close();
		} catch (XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
