package com.bagri.xqj;

import static com.bagri.xqj.BagriXQErrors.ex_expression_closed;

import java.util.ArrayList;
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

import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.XDMException;

import static javax.xml.xquery.XQSequenceType.*;
import static com.bagri.xquery.api.XQUtils.*;


public class BagriXQPreparedExpression extends BagriXQDynamicContext implements	XQPreparedExpression {
	
	private String xquery;
	private List<XQResultSequence> results = new ArrayList<>();

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
	
	@Override
	public void close() throws XQException {
		super.close();
		for (XQResultSequence sq: results) {
			sq.close();
		}
		results.clear();
	}
	
	public String getXQuery() {
		return xquery;
	}
	
	public void setXQuery(String xquery) {
		this.xquery = xquery;
	}

	@Override
	public XQResultSequence executeQuery() throws XQException {

		checkState(ex_expression_closed);
		ResultCursor result = connection.executeQuery(xquery, context);
		XQResultSequence sequence;
		if (context.getScrollability() == XQConstants.SCROLLTYPE_SCROLLABLE) {
			try {
				sequence = new ScrollableXQResultSequence(this, result.getList());
			} catch (XDMException ex) {
				throw getXQException(ex); 
			}
		} else {
			sequence = new IterableXQResultSequence(this, result); 
		}
		results.add(sequence);
		return sequence;
	}

	@Override
	public QName[] getAllExternalVariables() throws XQException {

		checkState(ex_expression_closed);
		return getVarNames().toArray(new QName[0]);
	}

	@Override
	public QName[] getAllUnboundExternalVariables() throws XQException {

		checkState(ex_expression_closed);
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

		checkState(ex_expression_closed);
		return new BagriXQSequenceType(connection.createItemType(), OCC_ZERO_OR_MORE);
	}

	@Override
	public XQSequenceType getStaticVariableType(QName name) throws XQException {

		checkState(ex_expression_closed);
		if (name == null) {
			throw new XQException("name is null");
		}
		if (getVarNames().contains(name)) {
			// where can I get var type??
			XQItemType type;
			XQItemAccessor acc = (XQItemAccessor) getBindings().get(name);
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
