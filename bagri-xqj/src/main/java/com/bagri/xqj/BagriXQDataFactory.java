package com.bagri.xqj;

import static com.bagri.common.util.CollectionUtils.copyIterator;
import static com.bagri.xqj.BagriXQErrors.ex_connection_closed;
import static com.bagri.xquery.api.XQUtils.getTypeName;
import static com.bagri.xquery.api.XQUtils.isAtomicType;
import static javax.xml.xquery.XQItemType.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;

import org.apache.xerces.util.XMLChar;
import org.w3c.dom.Node;

import com.bagri.common.util.XMLUtils;
import com.bagri.xquery.api.XQUtils;
import com.bagri.xquery.api.XQProcessor;

public class BagriXQDataFactory extends BagriXQCloseable implements XQDataFactory {

	private XQProcessor xqProcessor;
	
	public BagriXQDataFactory() {
		//
	}
    
	public XQProcessor getProcessor() {
		return this.xqProcessor;
	}
	
	public void setProcessor(XQProcessor xqProcessor) {
		// must be not null!
		this.xqProcessor = xqProcessor;
	}
    
	@Override
	public XQItemType createAtomicType(int baseType) throws XQException {

		return createAtomicType(baseType, getTypeName(baseType), null);
	}

	@Override
	public XQItemType createAtomicType(int baseType, QName typeName, URI schemaURI) throws XQException {
		
		checkState(ex_connection_closed);
		if (baseType == XQBASETYPE_UNTYPED || baseType == XQBASETYPE_ANYTYPE ||
			baseType == XQBASETYPE_IDREFS || baseType == XQBASETYPE_NMTOKENS ||
			baseType == XQBASETYPE_ENTITIES || baseType == XQBASETYPE_ANYSIMPLETYPE) {
			
			throw new XQException("Wrong base type: " + baseType); 
		}
		return new BagriXQItemType(baseType, XQITEMKIND_ATOMIC, null, typeName, false, schemaURI);
	}

	@Override
	public XQItemType createAttributeType(QName nodeName, int baseType)	throws XQException {
		
		return createAttributeType(nodeName, baseType, getTypeName(baseType), null);
	}

	@Override
	public XQItemType createAttributeType(QName nodeName, int baseType, QName typeName,	URI schemaURI) throws XQException {
		
		checkState(ex_connection_closed);
		if (baseType == XQBASETYPE_UNTYPED || baseType == XQBASETYPE_ANYTYPE) {
			throw new XQException("Wrong base type: " + baseType);
		}
		return new BagriXQItemType(baseType, XQITEMKIND_ATTRIBUTE, nodeName, typeName, false, schemaURI);
	}

	@Override
	public XQItemType createCommentType() throws XQException {
		
		checkState(ex_connection_closed);
		return new BagriXQItemType(XQBASETYPE_UNTYPED, XQITEMKIND_COMMENT, null, getTypeName(XQBASETYPE_UNTYPED), false, null);
	}

	@Override
	public XQItemType createDocumentElementType(XQItemType elementType)	throws XQException {
		
		checkState(ex_connection_closed);
		if (elementType == null) {
			throw new XQException("provided elementType is null");
		} 
		if (elementType.getItemKind() !=  XQITEMKIND_ELEMENT) {
			throw new XQException("provided elementType has wrong kind: " + elementType);
		}
		return new BagriXQItemType(elementType.getBaseType(), XQITEMKIND_DOCUMENT_ELEMENT, elementType.getNodeName(), 
				elementType.getTypeName(), false, null); 
	}

	@Override
	public XQItemType createDocumentSchemaElementType(XQItemType elementType) throws XQException {
		
		checkState(ex_connection_closed);
		return new BagriXQItemType(XQBASETYPE_ANYTYPE, XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT, elementType.getNodeName(), 
				elementType.getTypeName(), elementType.isElementNillable(), elementType.getSchemaURI());

	}

	@Override
	public XQItemType createDocumentType() throws XQException {
		
		checkState(ex_connection_closed);
		return new BagriXQItemType(XQBASETYPE_ANYTYPE, XQITEMKIND_DOCUMENT, null, null,	false, null);
	}

	@Override
	public XQItemType createElementType(QName nodeName, int baseType) throws XQException {
		
		return createElementType(nodeName, baseType, getTypeName(baseType), null, false);
	}

	@Override
	public XQItemType createElementType(QName nodeName, int baseType, QName typeName, URI schemaURI, boolean allowNil) throws XQException {
		
		checkState(ex_connection_closed);
		return new BagriXQItemType(baseType, XQITEMKIND_ELEMENT, nodeName, typeName, allowNil, schemaURI);
	}

	@Override
	public XQItem createItem(XQItem item) throws XQException {
		
		checkState(ex_connection_closed);
		if (item == null) {
			throw new XQException("Item is null");
		}
		if (item.isClosed()) {
			throw new XQException("Item is closed");
		}
		return new BagriXQItem(xqProcessor, item.getItemType(), item.getAtomicValue());
	}

	@Override
	public XQItem createItemFromAtomicValue(String value, XQItemType type) throws XQException {
		
		checkState(ex_connection_closed);
		if (type == null) {
			throw new XQException("value is null");
		}
		if (value == null) {
			throw new XQException("type is null");
		}
		if (!isAtomicType(type.getBaseType())) {
			throw new XQException("type is not atomic");
		}
		if (!((BagriXQItemType) type).isValueCompatible(value)) {
			throw new XQException("Value is not compatible");
		} 
		return new BagriXQItem(xqProcessor, type, value);
	}

	@Override
	public XQItem createItemFromBoolean(boolean value, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (type == null || type.getBaseType() == XQBASETYPE_BOOLEAN) {
			return new BagriXQItem(xqProcessor, new BagriXQItemType(XQBASETYPE_BOOLEAN, XQITEMKIND_ATOMIC, null, getTypeName(XQBASETYPE_BOOLEAN), false, null), value);
		} 
		throw new XQException("wrong boolean type: " + type + "(" + type.getBaseType() + ")");
	}

	@Override
	public XQItem createItemFromByte(byte value, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (type == null) {
			return new BagriXQItem(xqProcessor, new BagriXQItemType(XQBASETYPE_BYTE, XQITEMKIND_ATOMIC, null, getTypeName(XQBASETYPE_BYTE), false, null), value);
		}

		switch (type.getBaseType()) {
			case XQBASETYPE_BYTE: 
			case XQBASETYPE_SHORT: 
			case XQBASETYPE_INT: 
	    	case XQBASETYPE_LONG: return new BagriXQItem(xqProcessor, type, value);
			case XQBASETYPE_DECIMAL: return new BagriXQItem(xqProcessor, type, new java.math.BigDecimal(value));

			case XQBASETYPE_INTEGER: return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
			case XQBASETYPE_NEGATIVE_INTEGER: if (value < 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_NONNEGATIVE_INTEGER: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_NONPOSITIVE_INTEGER: if (value <= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_POSITIVE_INTEGER: if (value > 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break; 
			
			case XQBASETYPE_UNSIGNED_BYTE: if (value >= 0) { 
					return new BagriXQItem(xqProcessor, type, value);
				}
				break;
			case XQBASETYPE_UNSIGNED_INT: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, value); 
				}
				break;
			case XQBASETYPE_UNSIGNED_LONG: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_UNSIGNED_SHORT: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, value);
				}
		}
		throw new XQException("wrong byte type: " + type + "(" + type.getBaseType() + ")");
	}

	@Override
	public XQItem createItemFromDocument(XMLStreamReader value, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (value == null) {
			throw new XQException("StreamReader is null");
		}

		String content;
		try {
			content = XMLUtils.sourceToString(new StAXSource(value), null);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		return createItemFromDocument(content, null, type);
	}

	@Override
	public XQItem createItemFromDocument(Source value, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (value == null) {
			throw new XQException("Source is null");
		}
		
		String content;
		try {
			content = XMLUtils.sourceToString(value, null);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		return createItemFromDocument(content, null, type);
	}

	@Override
	public XQItem createItemFromDocument(String value, String baseURI, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (value == null) {
			throw new XQException("value is null");
		}
		
		// do not delete this line. it'll throw exception
		// in case when value contains wrong XML
		try {
			XMLUtils.textToDocument(value);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}

		if (type == null) {
			return new BagriXQItem(xqProcessor, createDocumentElementType(createElementType(null, XQBASETYPE_UNTYPED)), 
					value); 
		} else {
			int kind = type.getItemKind(); 
			if (kind == XQITEMKIND_DOCUMENT || kind == XQITEMKIND_DOCUMENT_ELEMENT 
					|| kind == XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT) {
				return new BagriXQItem(xqProcessor, type, value);
			}
		}

		throw new XQException("wrong document type: " + type); 
	}

	@Override
	public XQItem createItemFromDocument(Reader value, String baseURI, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (value == null) {
			throw new XQException("value is null");
		}
		
		String content;
		try {
			content = XMLUtils.textToString(value);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		return createItemFromDocument(content, baseURI, type);
	}

	@Override
	public XQItem createItemFromDocument(InputStream value, String baseURI, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (value == null) {
			throw new XQException("value is null");
		}
		
		String content;
		try {
			content = XMLUtils.textToString(value);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		return createItemFromDocument(content, baseURI, type);
	}

	@Override
	public XQItem createItemFromDouble(double value, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (type == null || type.getBaseType() == XQBASETYPE_DOUBLE) {
			return new BagriXQItem(xqProcessor, new BagriXQItemType(XQBASETYPE_DOUBLE, XQITEMKIND_ATOMIC, null, getTypeName(XQBASETYPE_DOUBLE), false, null), value);
		} 
		throw new XQException("wrong double type: " + type + "(" + type.getBaseType() + ")");
	}

	@Override
	public XQItem createItemFromFloat(float value, XQItemType type)	throws XQException {

		checkState(ex_connection_closed);
		if (type == null || type.getBaseType() == XQBASETYPE_FLOAT) {
			return new BagriXQItem(xqProcessor, new BagriXQItemType(XQBASETYPE_FLOAT, XQITEMKIND_ATOMIC, null, getTypeName(XQBASETYPE_FLOAT), false, null), value);
		} 
		throw new XQException("wrong float type: " + type + "(" + type.getBaseType() + ")");
	}

	@Override
	public XQItem createItemFromInt(int value, XQItemType type)	throws XQException {

		checkState(ex_connection_closed);
		if (type == null) {
			return new BagriXQItem(xqProcessor, new BagriXQItemType(XQBASETYPE_INT, XQITEMKIND_ATOMIC, null, getTypeName(XQBASETYPE_INT), false, null), value);
		}

		Integer intVal = new Integer(value);
		switch (type.getBaseType()) {
			case XQBASETYPE_BYTE: if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
					return new BagriXQItem(xqProcessor, type, intVal.byteValue());
				}
				break;
			case XQBASETYPE_SHORT: if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
					return new BagriXQItem(xqProcessor, type, intVal.shortValue());
				}
				break;
			case XQBASETYPE_INT: return new BagriXQItem(xqProcessor, type, intVal);
	    	case XQBASETYPE_LONG: return new BagriXQItem(xqProcessor, type, new Long(value));
			case XQBASETYPE_DECIMAL: return new BagriXQItem(xqProcessor, type, new java.math.BigDecimal(value));

			case XQBASETYPE_INTEGER: return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
			case XQBASETYPE_NEGATIVE_INTEGER: if (value < 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_NONNEGATIVE_INTEGER: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_NONPOSITIVE_INTEGER: if (value <= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_POSITIVE_INTEGER: if (value > 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break; 
			
			case XQBASETYPE_UNSIGNED_BYTE: if (value >= 0 && value <= Short.MAX_VALUE) { 
					return new BagriXQItem(xqProcessor, type, intVal.shortValue());
				}
				break;
			case XQBASETYPE_UNSIGNED_INT: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, value); 
				}
				break;
			case XQBASETYPE_UNSIGNED_LONG: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_UNSIGNED_SHORT: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, value);
				}
		}
		throw new XQException("wrong int type: " + type + "(" + type.getBaseType() + ")");
	}

	@Override
	public XQItem createItemFromLong(long value, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (type == null) {
			return new BagriXQItem(xqProcessor, new BagriXQItemType(XQBASETYPE_LONG, XQITEMKIND_ATOMIC, null, getTypeName(XQBASETYPE_LONG), false, null), value);
		} 
		
		Long longVal = new Long(value);
		switch (type.getBaseType()) {
			case XQBASETYPE_BYTE: if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
					return new BagriXQItem(xqProcessor, type, longVal.byteValue());
				}
				break;
			case XQBASETYPE_SHORT: if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
					return new BagriXQItem(xqProcessor, type, longVal.shortValue());
				}
				break;
			case XQBASETYPE_INT: if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
					return new BagriXQItem(xqProcessor, type, longVal.intValue());
				}
				break;
	    	case XQBASETYPE_LONG: return new BagriXQItem(xqProcessor, type, longVal);
			case XQBASETYPE_DECIMAL: return new BagriXQItem(xqProcessor, type, new java.math.BigDecimal(value));

			case XQBASETYPE_INTEGER: return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(longVal.toString()));
			case XQBASETYPE_NEGATIVE_INTEGER: if (value < 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(longVal.toString()));
				}
				break;
			case XQBASETYPE_NONNEGATIVE_INTEGER: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(longVal.toString()));
				}
				break;
			case XQBASETYPE_NONPOSITIVE_INTEGER: if (value <= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(longVal.toString()));
				}
				break;
			case XQBASETYPE_POSITIVE_INTEGER: if (value > 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(longVal.toString()));
				}
				break; 
			
			case XQBASETYPE_UNSIGNED_BYTE: if (value >= 0 && value <= Short.MAX_VALUE) { 
					return new BagriXQItem(xqProcessor, type, longVal.shortValue());
				}
				break;
			case XQBASETYPE_UNSIGNED_INT: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, longVal); 
				}
				break;
			case XQBASETYPE_UNSIGNED_LONG: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(longVal.toString()));
				}
				break;
			case XQBASETYPE_UNSIGNED_SHORT: if (value >= 0 && value <= Integer.MAX_VALUE) {
					return new BagriXQItem(xqProcessor, type, longVal.intValue());
				}
		}
		throw new XQException("wrong long type: " + type + "(" + type.getBaseType() + ")");
	}

	@Override
	public XQItem createItemFromNode(Node value, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (value == null) {
			throw new XQException("Node value is null");
		}
		if (type == null) {
			return new BagriXQItem(xqProcessor, createNodeType(), value); 
		} else { 
			if (!((BagriXQItemType) type).isNodeCompatible(value)) {
				throw new XQException("Node type and value are not compatible");
			} 
			return new BagriXQItem(xqProcessor, type, value); 
		}
	}

	@Override
	public XQItem createItemFromObject(Object value, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (value == null) {
			throw new XQException("value is null");
		}
		if (type == null) {
			type = XQUtils.getTypeForObject(this, value); 
		} else {
			if (!XQUtils.isTypeValueCompatible(type.getBaseType(), value)) {
				throw new XQException("Value is not compatible");
			} 
		}
		return new BagriXQItem(xqProcessor, type, value);
	}

	@Override
	public XQItem createItemFromShort(short value, XQItemType type)	throws XQException {

		checkState(ex_connection_closed);
		if (type == null) {
			return new BagriXQItem(xqProcessor, new BagriXQItemType(XQBASETYPE_SHORT, XQITEMKIND_ATOMIC, null, getTypeName(XQBASETYPE_SHORT), false, null), value);
		}

		Short shortVal = new Short(value);
		switch (type.getBaseType()) {
			case XQBASETYPE_BYTE: if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
					return new BagriXQItem(xqProcessor, type, shortVal.byteValue());
				}
				break;
			case XQBASETYPE_SHORT: return new BagriXQItem(xqProcessor, type, value); 
			case XQBASETYPE_INT: return new BagriXQItem(xqProcessor, type, new Integer(value));
	    	case XQBASETYPE_LONG: return new BagriXQItem(xqProcessor, type, new Long(value));
			case XQBASETYPE_DECIMAL: return new BagriXQItem(xqProcessor, type, new java.math.BigDecimal(value));

			case XQBASETYPE_INTEGER: return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
			case XQBASETYPE_NEGATIVE_INTEGER: if (value < 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_NONNEGATIVE_INTEGER: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_NONPOSITIVE_INTEGER: if (value <= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_POSITIVE_INTEGER: if (value > 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break; 
			
			case XQBASETYPE_UNSIGNED_BYTE: if (value >= 0) { 
					return new BagriXQItem(xqProcessor, type, value);
				}
				break;
			case XQBASETYPE_UNSIGNED_INT: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, new Integer(value)); 
				}
				break;
			case XQBASETYPE_UNSIGNED_LONG: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, new java.math.BigInteger(String.valueOf(value)));
				}
				break;
			case XQBASETYPE_UNSIGNED_SHORT: if (value >= 0) {
					return new BagriXQItem(xqProcessor, type, new Integer(value));
				}
		}
		throw new XQException("wrong short type: " + type + "(" + type.getBaseType() + ")");
	}

	@Override
	public XQItem createItemFromString(String value, XQItemType type) throws XQException {

		checkState(ex_connection_closed);
		if (value == null) {
			throw new XQException("value is null");
		}
		if (type == null) {
			type = createAtomicType(XQBASETYPE_STRING);
		}
		switch (type.getBaseType()) {
			case XQBASETYPE_ANYURI: 
			case XQBASETYPE_NOTATION:  
			case XQBASETYPE_STRING: 
			case XQBASETYPE_UNTYPEDATOMIC: 
				return new BagriXQItem(xqProcessor, type, value);
				
			case XQBASETYPE_ID: if (XMLChar.isValidNCName(value)) {
					return new BagriXQItem(xqProcessor, type, value);
				}
				break;
			case XQBASETYPE_NAME: if (XMLChar.isValidName(value)) {
					return new BagriXQItem(xqProcessor, type, value);
				}
				break;
			case XQBASETYPE_ENTITY: 
			case XQBASETYPE_ENTITIES: 
			case XQBASETYPE_IDREF: 
			case XQBASETYPE_NCNAME: if (XMLChar.isValidNCName(value)) {
					return new BagriXQItem(xqProcessor, type, value);
				}
				//throw new XQException("can't convert string \"" + value + "\" to type " + type);
				break;
			case XQBASETYPE_NMTOKEN: if (XMLChar.isValidNmtoken(value)) {
					return new BagriXQItem(xqProcessor, type, value);
				}
				break;
			case XQBASETYPE_NORMALIZED_STRING: if (isValidNormalizedString(value)) {
					return new BagriXQItem(xqProcessor, type, value);
				}
				break;
			case XQBASETYPE_TOKEN: if (isValidToken(value)) {
					return new BagriXQItem(xqProcessor, type, value);
				}
				break;
		}
		throw new XQException("wrong string value: " + value + " for type: " + type);
	}

	/**
	 * implementation taken from org.apache.axis.types.NormalizedString class
	 * 
	 * @param value
	 * @return
	 */
    private boolean isValidNormalizedString(String value)  {
        int scan;

        for (scan = 0; scan < value.length(); scan++) {
            char cDigit = value.charAt(scan);
            switch (cDigit) {
                case 0x09:
                case 0x0A:
                case 0x0D:
                    return false;
                default:
                    break;
            }
        }
        return true;
    }

    /**
	 * implementation taken from org.apache.axis.types.Token class
     * 
     * @param value
     * @return
     */
    private boolean isValidToken(String value) {
        int scan;
        // check to see if we have a string to review
        if (  (value == null) || (value.length() == 0)  )
            return true;
            
        // no leading space
        if (value.charAt(0) == 0x20)
            return false;

        // no trail space
        if (value.charAt(value.length() - 1) == 0x20)
            return false;

        for (scan=0; scan < value.length(); scan++) {
            char cDigit = value.charAt(scan);
            switch (cDigit) {
                case 0x09:
                case 0x0A:
                    return false;
                case 0x20:
                   // no doublspace
                    if (scan+1 < value.length())
                        if (value.charAt(scan + 1) == 0x20) {
                            return false;
                        }
                default:
                    break;
            }
        }
        return true;
    }
	
	@Override
	public XQItemType createItemType() throws XQException {

		checkState(ex_connection_closed);
		return new BagriXQItemType(XQBASETYPE_ANYTYPE, XQITEMKIND_ITEM, null, getTypeName(XQBASETYPE_ANYTYPE), false, null);
	}

	@Override
	public XQItemType createNodeType() throws XQException {
		
		checkState(ex_connection_closed);
		return new BagriXQItemType(XQBASETYPE_ANYTYPE, XQITEMKIND_NODE, null, getTypeName(XQBASETYPE_UNTYPED), false, null);
	}

	@Override
	public XQItemType createProcessingInstructionType(String piTarget) throws XQException {
		
		checkState(ex_connection_closed);
		QName nodeName = null;
		if (piTarget != null) {
			nodeName = new QName(piTarget);
		}
		return new BagriXQItemType(XQBASETYPE_ANYTYPE, XQITEMKIND_PI, nodeName, getTypeName(XQBASETYPE_UNTYPED), false, null);
	}

	@Override
	public XQItemType createSchemaAttributeType(QName nodeName, int baseType, URI schemaURI) throws XQException {
		
		checkState(ex_connection_closed);
		return new BagriXQItemType(baseType, XQITEMKIND_SCHEMA_ATTRIBUTE, nodeName, getTypeName(XQBASETYPE_ANYTYPE), false, schemaURI);
	}

	@Override
	public XQItemType createSchemaElementType(QName nodeName, int baseType, URI schemaURI) throws XQException {
		
		checkState(ex_connection_closed);
		return new BagriXQItemType(baseType, XQITEMKIND_SCHEMA_ELEMENT, nodeName, getTypeName(XQBASETYPE_UNTYPED), false, schemaURI);
	}
	
	@Override
	public XQSequence createSequence(XQSequence sqc) throws XQException {
		
		checkState(ex_connection_closed);
		if (sqc == null) {
			throw new XQException("Sequence is null");
		}
		
		return new ScrollableXQSequence(this, xqProcessor, getList(sqc));
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public XQSequence createSequence(Iterator itr) throws XQException {

		checkState(ex_connection_closed);
		if (itr == null) {
			throw new XQException("Iterator is null");
		}

		// shouldn't we check processor props to know type of sequence?
		return new ScrollableXQSequence(this, xqProcessor, copyIterator(itr));
		//return new IterableXQSequence(this, xqProcessor, itr);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getList(XQSequence xqc) throws XQException {
		ArrayList list = new ArrayList();
		boolean hasNext = xqc.isOnItem();
		if (!hasNext) {
			hasNext = xqc.next();
		}
		while (hasNext) {
			list.add(xqc.getItem());
			hasNext = xqc.next();
		}
		return list;
	}
	
	@Override
	public XQSequenceType createSequenceType(XQItemType type, int occurence) throws XQException {
		
		checkState(ex_connection_closed);
		if (type == null) {
			if (occurence == XQSequenceType.OCC_EMPTY) {
				return new BagriXQSequenceType(type, occurence);
			}
			throw new XQException("Occurence must be OCC_EMPTY");
		} else {
			if (occurence == XQSequenceType.OCC_EMPTY) {
				throw new XQException("Occurence must be not OCC_EMPTY");
			}
			if (occurence == XQSequenceType.OCC_ZERO_OR_ONE || occurence == XQSequenceType.OCC_EXACTLY_ONE
					|| occurence == XQSequenceType.OCC_ZERO_OR_MORE || occurence == XQSequenceType.OCC_ONE_OR_MORE) {
				return new BagriXQSequenceType(type, occurence);
			}
			throw new XQException("Wrong occurence value: " + occurence);
		}
	}

	@Override
	public XQItemType createTextType() throws XQException {

		checkState(ex_connection_closed);
		return new BagriXQItemType(XQBASETYPE_UNTYPED, XQITEMKIND_TEXT, null, getTypeName(XQBASETYPE_UNTYPED), false, null);
	}

}
