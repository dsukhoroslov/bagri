package com.bagri.xqj;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQResultSequence;

public class ScrollableXQResultSequence extends ScrollableXQSequence implements	XQResultSequence {

	private BagriXQDynamicContext expression;
	
	ScrollableXQResultSequence(BagriXQDynamicContext expression) {
		super(expression.connection, expression.connection.getProcessor(), expression.connection.getResultList());
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
		
		return new BagriXQResultItem(this.getItemType(), this.getAtomicValue(), this);
	}

	@Override
	public boolean isClosed() {
		
		return expression.isClosed() || super.isClosed();
	}
	
}
