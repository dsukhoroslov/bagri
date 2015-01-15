package com.bagri.xqj;

//import static com.bagri.xqj.BagriXQConstants.ex_connection_closed;

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;

public class BagriXQExpression extends BagriXQDynamicContext implements XQExpression {
	

	BagriXQExpression(XQStaticContext context) {
		super(context);
	}

	BagriXQExpression(BagriXQConnection connection) {
		super(connection);
	}

	BagriXQExpression(BagriXQConnection connection, XQStaticContext context) {
		super(connection, context);
	}

	@Override
	public void executeCommand(String cmd) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (cmd == null) {
			throw new XQException("Provided command is null");
		}
		connection.executeCommand(cmd, getBindings());
	}

	@Override
	public void executeCommand(Reader cmd) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (cmd == null) {
			throw new XQException("Provided command reader is null");
		}
		String s = BagriXQUtils.textToString(cmd);
		connection.executeCommand(s, getBindings());
	}

	@Override
	public XQResultSequence executeQuery(String query) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (query == null) {
			throw new XQException("Provided query is null");
		}
		// run it...
		Iterator result = connection.executeQuery(query);
		
		if (context.getScrollability() == XQConstants.SCROLLTYPE_SCROLLABLE) {
			return new ScrollableXQResultSequence(this);
		}
		return new IterableXQResultSequence(this);
	}

	@Override
	public XQResultSequence executeQuery(Reader query) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (query == null) {
			throw new XQException("Provided query reader is null");
		}
		// run it...
		String s = BagriXQUtils.textToString(query);
		Object result = connection.executeQuery(s);

		if (context.getScrollability() == XQConstants.SCROLLTYPE_SCROLLABLE) {
			return new ScrollableXQResultSequence(this);
		}
		return new IterableXQResultSequence(this);
	}

	@Override
	public XQResultSequence executeQuery(InputStream query) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (query == null) {
			throw new XQException("Provided query stream is null");
		}
		// run it...
		String s = BagriXQUtils.textToString(query);
		Object result = connection.executeQuery(s);

		if (context.getScrollability() == XQConstants.SCROLLTYPE_SCROLLABLE) {
			return new ScrollableXQResultSequence(this);
		}
		return new IterableXQResultSequence(this);
	}
	
}
