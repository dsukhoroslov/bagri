package com.bagri.xqj;

import static com.bagri.xqj.BagriXQErrors.ex_sequence_closed;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQSequence;

import org.xml.sax.ContentHandler;

import com.bagri.common.util.XMLUtils;
import com.bagri.xquery.api.XQProcessor;

public abstract class BagriXQSequence extends BagriXQItemAccessor implements XQSequence {
	
	protected BagriXQDataFactory xqFactory;
	
	BagriXQSequence(BagriXQDataFactory xqFactory, XQProcessor xqProcessor) {
		super(xqProcessor);
		this.xqFactory = xqFactory;
	}
	
	@Override
	public XQItem getItem() throws XQException {
		
		checkState(ex_sequence_closed);
		return new BagriXQItem(getXQProcessor(), type, value);
	}

	@Override
	public XMLStreamReader getSequenceAsStream() throws XQException {

		checkState(ex_sequence_closed);
		try {
			return XMLUtils.stringToStream(getSequenceAsString(null));
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}
	
	@Override
	public String getSequenceAsString(Properties props) throws XQException {

		checkState(ex_sequence_closed);
        props = checkOutputProperties(props);
		return getXQProcessor().convertToString(this, props);
	}

	@Override
	public void writeSequence(OutputStream os, Properties props) throws XQException {

		checkState(ex_sequence_closed);
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
		
		checkState(ex_sequence_closed);
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

		checkState(ex_sequence_closed);
		if (saxhdlr == null) {
			throw new XQException("Provided ContextHandler is null");
		}

		try {
			XMLUtils.stringToResult(getSequenceAsString(null), new SAXResult(saxhdlr));
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public void writeSequenceToResult(Result result) throws XQException {
		
		checkState(ex_sequence_closed);
		if (result == null) {
			throw new XQException("Provided Result is null");
		}

		try {
			XMLUtils.stringToResult(getSequenceAsString(null), result);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}

}
