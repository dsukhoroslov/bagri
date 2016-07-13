package com.bagri.xqj;

import static com.bagri.xqj.BagriXQErrors.ex_expression_closed;
import static com.bagri.xquery.api.XQUtils.getXQException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;

import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.XDMException;

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
	public XQResultSequence executeQuery(String query) throws XQException {
		
		checkState(ex_expression_closed);
		if (query == null) {
			throw new XQException("Provided query is null");
		}
		
		// run it...
		ResultCursor result = connection.executeQuery(query, context);
		if (context.getScrollability() == XQConstants.SCROLLTYPE_SCROLLABLE) {
			try {
				return new ScrollableXQResultSequence(this, result.getList());
			} catch (XDMException ex) {
				throw getXQException(ex); 
			}
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
