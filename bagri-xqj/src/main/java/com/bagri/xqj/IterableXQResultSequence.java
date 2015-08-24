package com.bagri.xqj;

import java.util.Iterator;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;

public class IterableXQResultSequence extends IterableXQSequence implements XQResultSequence {

	private BagriXQDynamicContext expression;
	
	IterableXQResultSequence(BagriXQDynamicContext expression, Iterator itr) {
		super(expression.connection, expression.connection.getProcessor(), itr);
		this.expression = expression;
	}

	@Override
	public XQConnection getConnection() throws XQException {

		checkSequence();
		return expression.connection;
	}
	
	@Override
	public XQItem getItem() throws XQException {
		
		checkSequence();
		//checkAccess();
		super.getItem();
		return new BagriXQResultItem(type, value, this);
	}

	@Override
	public boolean isClosed() {
		
		return expression.isClosed() || super.isClosed();
	}
}
