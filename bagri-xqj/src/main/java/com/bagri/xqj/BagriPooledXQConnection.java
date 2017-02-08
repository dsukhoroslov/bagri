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
	
	private Set<XQConnectionEventListener> listeners = new HashSet<>();

	@Override
	public XQConnection getConnection() throws XQException {
		//fireErrorEvent(error);
		//BagriXQConnection conn;
		//conn.getProcessor().getRepository().getHealthManagement().addHealthChangeListener(this);
		return null;
	}

	@Override
	public void close() throws XQException {
		fireCloseEvent();
		// ??
		listeners.clear();
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

}
