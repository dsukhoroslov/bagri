package com.bagri.xqj;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;

import org.xml.sax.ContentHandler;

import com.bagri.xquery.api.XQProcessor;

public abstract class BagriXQSequence extends BagriXQItemAccessor implements XQSequence /*, XQResultSequence*/ {
	
	//protected BagriXQConnection connection;
	protected BagriXQDataFactory xqFactory;
	
	//BagriXQSequence(BagriXQConnection connection) {
	//	super();
	//	this.connection = connection;
	//}
	
	//BagriXQSequence(BagriXQConnection connection) {
	//	super(connection.getProcessor());
	//	this.connection = connection;
	//}

	BagriXQSequence(BagriXQDataFactory xqFactory, XQProcessor xqProcessor) {
		super(xqProcessor);
		this.xqFactory = xqFactory;
	}

	@Override
	public XQItem getItem() throws XQException {
		
		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		return new BagriXQItem(getXQProcessor(), type, value);
	}

	@Override
	public XMLStreamReader getSequenceAsStream() throws XQException {

		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		
		return BagriXQUtils.stringToStream(getSequenceAsString(null));
	}
	
	@Override
	public String getSequenceAsString(Properties props) throws XQException {

		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
        
		StringBuffer buff = new StringBuffer();
		//buff.append("<s>");
		
		boolean hasNext = isOnItem();
		if (!hasNext) {
			hasNext = next();
		}
		while (hasNext) {
			buff.append(this.getItemAsString(props));
			if (next()) {
				buff.append(" ");
			} else {
				hasNext = false;
			}
		}
		
		//buff.append("</s>");
        return buff.toString();
	}

	@Override
	public void writeSequence(OutputStream os, Properties props) throws XQException {

		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		if (os == null) {
			throw new XQException("Provided OutputStream is null");
		}

		String result = getSequenceAsString(props);
		try {
			os.write(result.getBytes());
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public void writeSequence(Writer ow, Properties props) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		if (ow == null) {
			throw new XQException("Provided Writer is null");
		}

		try {
			ow.write(getSequenceAsString(props));
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public void writeSequenceToSAX(ContentHandler saxhdlr) throws XQException {

		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		if (saxhdlr == null) {
			throw new XQException("Provided ContextHandler is null");
		}

		BagriXQUtils.stringToResult(getSequenceAsString(null), new SAXResult(saxhdlr));
	}

	@Override
	public void writeSequenceToResult(Result result) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Sequence is closed");
		}
		if (result == null) {
			throw new XQException("Provided Result is null");
		}

		BagriXQUtils.stringToResult(getSequenceAsString(null), result);
	}

	
}
