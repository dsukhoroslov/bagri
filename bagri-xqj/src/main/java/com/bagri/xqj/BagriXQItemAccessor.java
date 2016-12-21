package com.bagri.xqj;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQItemType;

import static com.bagri.support.util.PropUtils.getOutputProperties;
import static com.bagri.xqj.BagriXQErrors.ex_item_closed;
import static javax.xml.xquery.XQItemType.*;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.support.util.XMLUtils;

public abstract class BagriXQItemAccessor extends BagriXQCloseable implements XQItemAccessor {
	
	protected XQItemType type;
	protected Object value;
	protected boolean positioned = false;
	
	private XQProcessor xqProcessor;

	BagriXQItemAccessor(XQProcessor xqProcessor) {
		if (xqProcessor == null) {
			throw new NullPointerException("Got NULL processor at initialization!");
		}
		this.xqProcessor = xqProcessor;
	}
	
	protected void setCurrent(XQItemType type, Object value) {
		this.type = type;
		this.value = value;
		this.positioned = true;
	}
	
	XQProcessor getXQProcessor() {
		return xqProcessor;
	}
	
	@Override
	public boolean getBoolean() throws XQException {
		
		checkState(ex_item_closed);
		if (type == null) {
			throw new XQException("ItemType is null");
		}
		if (type.getBaseType() == XQBASETYPE_BOOLEAN) {
			return (Boolean) value;
		}
		throw new XQException("ItemType is not boolean");
	}
	
	protected Properties checkOutputProperties(Properties props) {
		Properties outProps;
        if (props == null) {
    		outProps = new Properties();
            outProps.setProperty(OutputKeys.INDENT, "yes");
            outProps.setProperty(OutputKeys.METHOD, "xml");
            outProps.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } else {
        	outProps = getOutputProperties(props);
        }
        return outProps;
	}
	
	private long convertDecimal(long min, long max, String typeName) throws XQException {

		switch (type.getBaseType()) {
			case XQBASETYPE_BYTE: {
				Byte b = (Byte) value;
				if (b.longValue() >= min && b.longValue() <= max) {
					return b.longValue();
				}
				break;
			}
			case XQBASETYPE_INT: 
			case XQBASETYPE_UNSIGNED_SHORT: {
				Integer i = (Integer) value;
				if (i.longValue() >= min && i.longValue() <= max) {
					return i.longValue();
				}
				break;
			}
			case XQBASETYPE_LONG: 
			case XQBASETYPE_UNSIGNED_INT: {
				Long l = (Long) value;
				if (l.longValue() >= min && l.longValue() <= max) {
					return l.longValue();
				}
				break;
			}
			case XQBASETYPE_SHORT: 
			case XQBASETYPE_UNSIGNED_BYTE: {
				Short s = (Short) value;
				if (s.longValue() >= min && s.longValue() <= max) {
					return s.longValue();
				}
				break;
			}
			case XQBASETYPE_INTEGER: 
			case XQBASETYPE_NEGATIVE_INTEGER: 
			case XQBASETYPE_NONNEGATIVE_INTEGER: 
			case XQBASETYPE_NONPOSITIVE_INTEGER: 
			case XQBASETYPE_POSITIVE_INTEGER: 
			case XQBASETYPE_UNSIGNED_LONG: {
				java.math.BigInteger i = (java.math.BigInteger) value;
				if (i.longValue() >= min && i.longValue() <= max) {
					return i.longValue();
				}
				break;
			}
			case XQBASETYPE_DECIMAL: {
				java.math.BigDecimal d = (java.math.BigDecimal) value;
				//if (d.longValue() >= min && d.longValue() <= max) {
				//	return d.longValue();
				//}
				try {
					return d.longValueExact();
				} catch (ArithmeticException e) {
					//
				}
				break;
			}
			//default: 
		}
		throw new XQException("ItemType is not " + typeName + "; the value is: " + value);
	}

	@Override
	public byte getByte() throws XQException {
		
		checkState(ex_item_closed);
		return (byte) convertDecimal(Byte.MIN_VALUE, Byte.MAX_VALUE, "byte");
	}

	@Override
	public double getDouble() throws XQException {
		
		checkState(ex_item_closed);
		if (type.getBaseType() == XQBASETYPE_DOUBLE) {
			return (Double) value;
		}
		if (type.getBaseType() == XQBASETYPE_FLOAT) {
			return ((Float) value).doubleValue();
		}
		throw new XQException("ItemType is not double");
	}

	@Override
	public float getFloat() throws XQException {
		
		checkState(ex_item_closed);
		if (type.getBaseType() == XQBASETYPE_FLOAT) {
			return (Float) value;
		}
		if (type.getBaseType() == XQBASETYPE_DOUBLE) {
			return ((Double) value).floatValue();
		}
		throw new XQException("ItemType is not float");
	}

	@Override
	public int getInt() throws XQException {
		
		checkState(ex_item_closed);
		return (int) convertDecimal(Integer.MIN_VALUE, Integer.MAX_VALUE, "int");
	}

	@Override
	public XQItemType getItemType() throws XQException {
		
		if (closed) {
			throw new XQException(ex_item_closed);
		}
		if (!positioned) {
			throw new XQException("not positioned on the Item");
		}
		return type;
	}

	@Override
	public String getAtomicValue() throws XQException {

		return getItemAsString(null);
	}

	@Override
	public long getLong() throws XQException {

		checkState(ex_item_closed);
		return (long) convertDecimal(Long.MIN_VALUE, Long.MAX_VALUE, "long");
	}

	@Override
	public Node getNode() throws XQException {

		checkState(ex_item_closed);
		switch (type.getItemKind()) {
			case XQITEMKIND_ATTRIBUTE: 
			case XQITEMKIND_SCHEMA_ATTRIBUTE: return (org.w3c.dom.Attr) value;
			case XQITEMKIND_COMMENT: return (org.w3c.dom.Comment) value;
			case XQITEMKIND_DOCUMENT: return (org.w3c.dom.Document) value;
			case XQITEMKIND_ELEMENT: 
			case XQITEMKIND_DOCUMENT_ELEMENT: 
			case XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT:
			case XQITEMKIND_SCHEMA_ELEMENT: return (org.w3c.dom.Element) value;
			case XQITEMKIND_PI: return (org.w3c.dom.ProcessingInstruction) value;
			case XQITEMKIND_TEXT: return (org.w3c.dom.Text) value;
			default: 
				throw new XQException("ItemType is not Node: " + value.getClass().getName());
		}
	}

	@Override
	public URI getNodeUri() throws XQException {
		
		Node node = getNode();
		try {
			String base = node.getBaseURI();
			if (base == null) {
				base = "";
			}
			return new URI(base);
		} catch (URISyntaxException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public Object getObject() throws XQException {
		
		if (closed) {
			throw new XQException(ex_item_closed);
		}
		return value;
	}

	@Override
	public XMLStreamReader getItemAsStream() throws XQException {
		
		if (closed) {
			throw new XQException(ex_item_closed);
		}
		try {
			return XMLUtils.stringToStream(getItemAsString(null));
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public String getItemAsString(Properties props) throws XQException {
		
		if (closed) {
			throw new XQException(ex_item_closed);
		}
		if (value == null) {
			throw new XQException("Value is not accessible");
		}
		props = checkOutputProperties(props);
		return xqProcessor.convertToString(value, props);
	}

	@Override
	public short getShort() throws XQException {
		
		checkState(ex_item_closed);
		return (short) convertDecimal(Short.MIN_VALUE, Short.MAX_VALUE, "short");
	}

	@Override
	public boolean instanceOf(XQItemType type) throws XQException {
		
		checkState(ex_item_closed);
		if (!positioned) {
			throw new XQException("not positioned on the Item");
		}
		if (type == null) {
			throw new XQException("Provided type is null");
		}
		return this.type.equals(type);
	}

	@Override
	public void writeItem(OutputStream os, Properties props) throws XQException {

		checkState(ex_item_closed);
		if (os == null) {
			throw new XQException("Provided OutputStream is null");
		}

		String result = getItemAsString(props);
		try {
			os.write(result.getBytes());
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public void writeItem(Writer ow, Properties props) throws XQException {

		checkState(ex_item_closed);
		if (ow == null) {
			throw new XQException("Provided Writer is null");
		}
		
		try {
			ow.write(getItemAsString(props));
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public void writeItemToSAX(ContentHandler saxhdlr) throws XQException {

		checkState(ex_item_closed);
		if (saxhdlr == null) {
			throw new XQException("Provided ContextHandler is null");
		}
		
		try {
			XMLUtils.stringToResult(getItemAsString(null), new SAXResult(saxhdlr));
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public void writeItemToResult(Result result) throws XQException {

		checkState(ex_item_closed);
		if (result == null) {
			throw new XQException("Provided Result is null");
		}
		
		try {
			XMLUtils.stringToResult(getItemAsString(null), result);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}
	

}
