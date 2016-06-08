package com.bagri.xqj;

import static com.bagri.xqj.BagriXQErrors.ex_expression_closed;

import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.xquery.XQDynamicContext;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQStaticContext;

import org.w3c.dom.Node;

public class BagriXQDynamicContext extends BagriXQCloseable implements XQDynamicContext {
	
	protected XQStaticContext context;
	protected BagriXQConnection connection;
	private TimeZone timeZone;
	private Set<QName> varNames = new HashSet<QName>();
	private Map<QName, Object> bindings = new HashMap<QName, Object>();

	BagriXQDynamicContext() {
		//
        timeZone = new GregorianCalendar().getTimeZone();
	}
	
	BagriXQDynamicContext(XQStaticContext context) {
		this();
		this.context = context;
	}

	BagriXQDynamicContext(BagriXQConnection connection) {
		this();
		this.connection = connection;
		try {
			this.context = connection.getStaticContext();
		} catch (XQException ex) {
			connection = null;
		}
	}

	BagriXQDynamicContext(BagriXQConnection connection, XQStaticContext context) {
		this();
		this.connection = connection;
		this.context = context;
	}

	public void cancel() throws XQException {
		
		checkState(ex_expression_closed);
		connection.cancel();
	}

	public boolean isClosed() {
		
		if (closed) {
			return true;
		}
		if (connection != null) {
			return connection.isClosed();
		}
		return false; 
	}

	public void close() throws XQException {
		
		// close expression when it is created by DataFactory
		for (QName varName: bindings.keySet()) {
			connection.unbindVariable(varName);
		}
		bindings.clear(); 
		closed = true;
	}

	
	@Override
	public void bindAtomicValue(QName varName, String value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromAtomicValue(value, type));
	}

	@Override
	public void bindString(QName varName, String value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromString(value, type));
	}
	
	protected void bindXQItemAccessor(QName varName, BagriXQItemAccessor value) throws XQException {
		
		checkState(ex_expression_closed);
		if (varName == null) {
			throw new XQException("varName is null");
		}
		if (value.isClosed()) {
			throw new XQException("Item is closed");
		}
		bindings.put(varName, value); //?
		connection.bindVariable(varName, value); //value.getObject()
	}

	@Override
	public void bindItem(QName varName, XQItem value) throws XQException {

		bindXQItemAccessor(varName, (BagriXQItemAccessor) value);
	}

	@Override
	public void bindSequence(QName varName, XQSequence value) throws XQException {

		bindXQItemAccessor(varName, (BagriXQItemAccessor) value);
	}

	@Override
	public void bindObject(QName varName, Object value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromObject(value, type));
	}

	@Override
	public void bindBoolean(QName varName, boolean value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromBoolean(value, type));
	}

	@Override
	public void bindByte(QName varName, byte value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromByte(value, type));
	}

	@Override
	public void bindDocument(QName varName, XMLStreamReader value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromDocument(value, type));
	}

	@Override
	public void bindDocument(QName varName, Source source, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromDocument(source, type));
	}

	@Override
	public void bindDocument(QName varName, String value, String baseURI, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromDocument(value, baseURI, type));
	}

	@Override
	public void bindDocument(QName varName, Reader value, String baseURI, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromDocument(value, baseURI, type));
	}

	@Override
	public void bindDocument(QName varName, InputStream value, String baseURI, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromDocument(value, baseURI, type));
	}

	@Override
	public void bindDouble(QName varName, double value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromDouble(value, type));
	}

	@Override
	public void bindFloat(QName varName, float value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromFloat(value, type));
	}

	@Override
	public void bindInt(QName varName, int value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromInt(value, type));
	}

	@Override
	public void bindLong(QName varName, long value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromLong(value, type));
	}

	@Override
	public void bindNode(QName varName, Node value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromNode(value, type));
	}

	@Override
	public void bindShort(QName varName, short value, XQItemType type) throws XQException {

		bindItem(varName, connection.createItemFromShort(value, type));
	}
	
	protected Map<QName, Object> getBindings() {
		return bindings; 
	}

	@Override
	public TimeZone getImplicitTimeZone() throws XQException {
		
		checkState(ex_expression_closed);
		return timeZone;
	}

	public XQStaticContext getStaticContext() throws XQException {
		
		checkState(ex_expression_closed);
		return new BagriXQStaticContext(context);
	}
	
	@Override
	public void setImplicitTimeZone(TimeZone implicitTimeZone) throws XQException {
		
		checkState(ex_expression_closed);
		this.timeZone = implicitTimeZone;
		// TODO: propagate timezone to the underlying XQuery processor
        //GregorianCalendar now = new GregorianCalendar(implicitTimeZone);
        //try {
        //    getDynamicContext().setCurrentDateTime(new DateTimeValue(now, true));
        //} catch (XPathException e) {
        //    throw new XQException(e.getMessage());
        //}
	}
	
	protected Set<QName> getVarNames() {
		return varNames;
	}
	
	void setVarNames(Collection<QName> qNames) {
		//
		varNames.addAll(qNames);
	}
	
	
}
