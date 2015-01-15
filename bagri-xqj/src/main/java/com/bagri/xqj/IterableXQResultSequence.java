package com.bagri.xqj;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;

public class IterableXQResultSequence extends IterableXQSequence implements XQResultSequence {

	private BagriXQDynamicContext expression;
	
	IterableXQResultSequence(BagriXQDynamicContext expression) {
		super(expression.connection, expression.connection.getProcessor(), expression.connection.getResultIterator());
		this.expression = expression;
	}

	@Override
	public XQConnection getConnection() throws XQException {
		
		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		return expression.connection;
	}
	
	@Override
	public XQItem getItem() throws XQException {
		
		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		//checkAccess();
		super.getItem();
		return new BagriXQResultItem(type, value, this);
	}

	@Override
	public boolean isClosed() {
		
		return expression.isClosed() || super.isClosed();
	}
}
