package com.bagri.xqj;

import static com.bagri.common.util.CollectionUtils.copyIterator;
import static com.bagri.xqj.BagriXQErrors.ex_expression_closed;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;

import com.bagri.common.util.XMLUtils;

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
		
		checkState(ex_expression_closed);
		if (cmd == null) {
			throw new XQException("Provided command is null");
		}
		connection.executeCommand(cmd, getBindings());
	}

	@Override
	public void executeCommand(Reader cmd) throws XQException {
		
		String str;
		try {
			str = XMLUtils.textToString(cmd);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		executeCommand(str);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public XQResultSequence executeQuery(String query) throws XQException {
		
		checkState(ex_expression_closed);
		if (query == null) {
			throw new XQException("Provided query is null");
		}
		
		// run it...
		Iterator result = connection.executeQuery(query);
		
		if (context.getScrollability() == XQConstants.SCROLLTYPE_SCROLLABLE) {
			return new ScrollableXQResultSequence(this, copyIterator(result));
		}
		return new IterableXQResultSequence(this, result);
	}

	@Override
	public XQResultSequence executeQuery(Reader query) throws XQException {
		
		String str;
		try {
			str = XMLUtils.textToString(query);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		return executeQuery(str);
	}

	@Override
	public XQResultSequence executeQuery(InputStream query) throws XQException {
		
		String str;
		try {
			str = XMLUtils.textToString(query);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		return executeQuery(str);
	}
	
}
