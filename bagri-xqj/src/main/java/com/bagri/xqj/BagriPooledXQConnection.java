package com.bagri.xqj;

import java.util.HashSet;
import java.util.Set;

import javax.xml.xquery.PooledXQConnection;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConnectionEvent;
import javax.xml.xquery.XQConnectionEventListener;
import javax.xml.xquery.XQException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagriPooledXQConnection implements PooledXQConnection {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriPooledXQConnection.class);
	
	private final XQConnection xqConnection;
	private Set<XQConnectionEventListener> listeners = new HashSet<>();
	
	BagriPooledXQConnection(XQConnection xqConn) {
		this.xqConnection = xqConn;
	}
	
	void freeConnection() {
		//
		// change state, return to pool..
		fireCloseEvent();
	}

	@Override
	public void close() throws XQException {
		//fireCloseEvent();
		// ??
		listeners.clear();
	}

	@Override
	public XQConnection getConnection() throws XQException {
		//fireErrorEvent(error);
		//conn.getProcessor().getRepository().getHealthManagement().addHealthChangeListener(this);
		return xqConnection;
	}

	@Override
	public void addConnectionEventListener(XQConnectionEventListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeConnectionEventListener(XQConnectionEventListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}
	
	private void fireCloseEvent() {
		XQConnectionEvent event = new XQConnectionEvent(this);
		for (XQConnectionEventListener listener: listeners) {
			listener.connectionClosed(event);
		}
	}
	
	private void fireErrorEvent(XQException error) {
		XQConnectionEvent event = new XQConnectionEvent(this, error);
		for (XQConnectionEventListener listener: listeners) {
			listener.connectionErrorOccurred(event);
		}
	}
	
/*	
	private class XQConnection extends BagriXQConnection {
		
		XQConnection() {
			super();
		}
		
		XQConnection(String username, boolean transactional) {
			super(username, transactional);
		}

		@Override
		public void close() throws XQException {
			//checkState();
			closeTransaction();
			closed = true;
			// now ask poolead parent to go back to pool... 
			fireCloseEvent();
		}
		
	}
*/
}
