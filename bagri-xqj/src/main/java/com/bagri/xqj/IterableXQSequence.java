package com.bagri.xqj;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import com.bagri.xquery.api.XQProcessor;

public class IterableXQSequence extends BagriXQSequence {
	
	private Iterator iterator;
	private boolean accessed;
	
	@SuppressWarnings("rawtypes")
	IterableXQSequence(BagriXQDataFactory xqFactory, XQProcessor  xqProcessor, Iterator iterator) {
		super(xqFactory, xqProcessor);
		this.iterator = iterator;
		accessed = false;
	}
	
	protected void checkAccess(boolean checkPosition) throws XQException {
		if (checkPosition && !positioned) {
			throw new XQException("Not positioned on an Item");
		}
		if (accessed) {
			throw new XQException("Item has been already accessed");
		}
		//accessed = true;
	}

	@Override
	public boolean absolute(int itempos) throws XQException {

		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public void afterLast() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public void beforeFirst() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public int count() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public boolean first() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public boolean getBoolean() throws XQException {
		
		checkAccess(true);
		boolean result = super.getBoolean();
		accessed = true;
		return result;
	}

	@Override
	public byte getByte() throws XQException {
		
		checkAccess(true);
		byte result = super.getByte();
		accessed = true;
		return result;
	}

	@Override
	public double getDouble() throws XQException {
		
		checkAccess(true);
		double result = super.getDouble();
		accessed = true;
		return result;
	}

	@Override
	public float getFloat() throws XQException {
		
		checkAccess(true);
		float result = super.getFloat();
		accessed = true;
		return result;
	}

	@Override
	public int getInt() throws XQException {
		
		checkAccess(true);
		int result = super.getInt();
		accessed = true;
		return result;
	}

	@Override
	public String getAtomicValue() throws XQException {
		
		checkAccess(true);
		String result = super.getAtomicValue();
		accessed = true;
		return result;
	}

	@Override
	public long getLong() throws XQException {

		checkAccess(true);
		long result = super.getLong();
		accessed = true;
		return result;
	}

	@Override
	public Node getNode() throws XQException {

		checkAccess(true);
		Node result = super.getNode();
		accessed = true;
		return result;
	}

	@Override
	public URI getNodeUri() throws XQException {

		boolean tmp = accessed;
		accessed = false;
		checkAccess(true);
		URI result = super.getNodeUri();
		accessed = tmp;
		return result;
	}

	@Override
	public Object getObject() throws XQException {
		
		checkAccess(true);
		Object result = super.getObject();
		accessed = true;
		return result;
	}

	//@Override
	//Iterator getIterator() throws XQException {
		
	//	if (isClosed()) {
	//		throw new XQException("Sequence is closed");
	//	}
	//	return iterator;
	//}

	@Override
	public XQItem getItem() throws XQException {
		
		checkAccess(true);
		XQItem result = super.getItem();
		accessed = true;
		return result;
	}

	@Override
	public XMLStreamReader getItemAsStream() throws XQException {

		checkAccess(true);
		XMLStreamReader result = super.getItemAsStream();
		accessed = true;
		return result;
	}

	@Override
	public String getItemAsString(Properties props) throws XQException {

		checkAccess(true);
		String result = super.getItemAsString(props);
		accessed = true;
		return result;
	}

	@Override
	public short getShort() throws XQException {
		
		checkAccess(true);
		short result = super.getShort();
		accessed = true;
		return result;
	}
	
	@Override
	public int getPosition() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public XMLStreamReader getSequenceAsStream() throws XQException {

		//positioned = true;
		checkAccess(false);
		XMLStreamReader result = super.getSequenceAsStream();
		accessed = true;
		return result;
	}

	@Override
	public String getSequenceAsString(Properties props) throws XQException {

		//positioned = true;
		checkAccess(false);
		String result = super.getSequenceAsString(props);
		accessed = true;
		return result;
	}

	@Override
	public boolean isAfterLast() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public boolean isBeforeFirst() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public boolean isFirst() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public boolean isLast() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public boolean isOnItem() throws XQException {
		
		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		return positioned; 
	}

	@Override
	public boolean isScrollable() throws XQException {
		
		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		return false;
	}

	@Override
	public boolean last() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public boolean next() throws XQException {
		
		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		if (iterator.hasNext()) {
			Object current = iterator.next();
			if (current instanceof BagriXQItem) {
				setCurrent(((BagriXQItem) current).type, ((BagriXQItem) current).value);
			} else if (current instanceof XQItem) {
				setCurrent(((XQItem) current).getItemType(), ((XQItem) current).getObject());
			} else {
				setCurrent(BagriXQUtils.getTypeForObject(xqFactory, current), current);
			}
			accessed = false;
			return true;
		}
		positioned = false;
		return false;
	}

	@Override
	public boolean previous() throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public boolean relative(int itempos) throws XQException {
		
		throw new XQException("Sequence is not scrollable");
	}

	@Override
	public void writeItem(OutputStream os, Properties props) throws XQException {

		//accessed = false;
		checkAccess(true);
		super.writeItem(os, props);
		accessed = true;
	}

	@Override
	public void writeItem(Writer ow, Properties props) throws XQException {

		//accessed = false;
		checkAccess(true);
		super.writeItem(ow, props);
		accessed = true;
	}

	@Override
	public void writeItemToSAX(ContentHandler saxhdlr) throws XQException {

		checkAccess(true);
		super.writeItemToSAX(saxhdlr);
		accessed = true;
	}

	@Override
	public void writeItemToResult(Result result) throws XQException {

		checkAccess(true);
		super.writeItemToResult(result);
		accessed = true;
	}
	
	@Override
	public void writeSequence(OutputStream os, Properties props) throws XQException {

		//positioned = true;
		checkAccess(false);
		super.writeSequence(os, props);
		accessed = true;
	}

	@Override
	public void writeSequence(Writer ow, Properties props) throws XQException {
		
		//positioned = true;
		checkAccess(false);
		super.writeSequence(ow, props);
		accessed = true;
	}

	@Override
	public void writeSequenceToSAX(ContentHandler saxhdlr) throws XQException {

		//positioned = true;
		checkAccess(false);
		super.writeSequenceToSAX(saxhdlr);
		accessed = true;
	}

	@Override
	public void writeSequenceToResult(Result result) throws XQException {
		
		//positioned = true;
		checkAccess(false);
		super.writeSequenceToResult(result);
		accessed = true;
	}
	
}
