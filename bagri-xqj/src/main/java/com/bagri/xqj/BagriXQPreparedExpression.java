package com.bagri.xqj;

import static com.bagri.common.util.CollectionUtils.copyIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;
import javax.xml.xquery.XQStaticContext;

import static javax.xml.xquery.XQSequenceType.*;

public class BagriXQPreparedExpression extends BagriXQDynamicContext implements	XQPreparedExpression {
	
	private String xquery;

	BagriXQPreparedExpression(XQStaticContext context) {
		super(context);
	}
	
	BagriXQPreparedExpression(BagriXQConnection connection) {
		super(connection);
	}

	BagriXQPreparedExpression(BagriXQConnection connection, XQStaticContext context) {
		super(connection, context);
	}
	
	@Override
	public void bindItem(QName varName, XQItem value) throws XQException {

		if (!getVarNames().contains(varName)) {
			throw new XQException("Unknown variable: " + varName);
		}
		super.bindItem(varName, value);
	}

	@Override
	public void bindSequence(QName varName, XQSequence value) throws XQException {
		if (!getVarNames().contains(varName)) {
			throw new XQException("Unknown variable: " + varName);
		}
		super.bindSequence(varName, value);
	}
	
	
	public String getXQuery() {
		return xquery;
	}
	
	public void setXQuery(String xquery) {
		this.xquery = xquery;
	}

	@Override
	public XQResultSequence executeQuery() throws XQException {

		if (isClosed()) {
			throw new XQException("Expression is closed");
		}
		// run it...
		Iterator result = connection.executeQuery(xquery, context); //this.getStaticContext());
		
		if (context.getScrollability() == XQConstants.SCROLLTYPE_SCROLLABLE) {
			return new ScrollableXQResultSequence(this, copyIterator(result));
		}
		return new IterableXQResultSequence(this, result);
	}

	@Override
	public QName[] getAllExternalVariables() throws XQException {

		if (isClosed()) {
			throw new XQException("Expression is closed");
		}
		return getVarNames().toArray(new QName[0]);
	}

	@Override
	public QName[] getAllUnboundExternalVariables() throws XQException {

		if (isClosed()) {
			throw new XQException("Expression is closed");
		}
		Set<QName> vars = getVarNames();
		Set<QName> bound = getBindings().keySet();
		List<QName> delta = new ArrayList<QName>(vars.size() - bound.size());
		for (QName name: vars) {
			if (!bound.contains(name)) {
				delta.add(name);
			}
		}
		return delta.toArray(new QName[0]);
	}

	@Override
	public XQSequenceType getStaticResultType() throws XQException {

		if (isClosed()) {
			throw new XQException("Expression is closed");
		}
		//
		return new BagriXQSequenceType(connection.createItemType(), OCC_ZERO_OR_MORE);
	}

	@Override
	public XQSequenceType getStaticVariableType(QName name) throws XQException {

		if (isClosed()) {
			throw new XQException("Expression is closed");
		}
		if (name == null) {
			throw new XQException("name is null");
		}
		if (getVarNames().contains(name)) {
			// where can I get var type??
			XQItemType type;
			XQItemAccessor acc = getBindings().get(name);
			if (acc != null) {
				type = acc.getItemType();
			} else {
				type = connection.createItemType();
			}
			return new BagriXQSequenceType(type, OCC_ZERO_OR_MORE);
		}
		throw new XQException("Static variable [" + name + "] does not exist");
	}

}
