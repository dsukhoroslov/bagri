package com.bagri.xquery.api;

import static com.bagri.xdm.common.Constants.xs_ns;
import static com.bagri.xdm.common.Constants.xs_prefix;
import static javax.xml.xquery.XQItemType.XQBASETYPE_ANYATOMICTYPE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_ANYSIMPLETYPE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_ANYTYPE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_ANYURI;
import static javax.xml.xquery.XQItemType.XQBASETYPE_BASE64BINARY;
import static javax.xml.xquery.XQItemType.XQBASETYPE_BOOLEAN;
import static javax.xml.xquery.XQItemType.XQBASETYPE_BYTE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DATE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DATETIME;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DAYTIMEDURATION;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DECIMAL;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DOUBLE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DURATION;
import static javax.xml.xquery.XQItemType.XQBASETYPE_ENTITIES;
import static javax.xml.xquery.XQItemType.XQBASETYPE_ENTITY;
import static javax.xml.xquery.XQItemType.XQBASETYPE_FLOAT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_GDAY;
import static javax.xml.xquery.XQItemType.XQBASETYPE_GMONTH;
import static javax.xml.xquery.XQItemType.XQBASETYPE_GMONTHDAY;
import static javax.xml.xquery.XQItemType.XQBASETYPE_GYEAR;
import static javax.xml.xquery.XQItemType.XQBASETYPE_GYEARMONTH;
import static javax.xml.xquery.XQItemType.XQBASETYPE_HEXBINARY;
import static javax.xml.xquery.XQItemType.XQBASETYPE_ID;
import static javax.xml.xquery.XQItemType.XQBASETYPE_IDREF;
import static javax.xml.xquery.XQItemType.XQBASETYPE_IDREFS;
import static javax.xml.xquery.XQItemType.XQBASETYPE_INT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_LANGUAGE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_LONG;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NAME;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NCNAME;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NEGATIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NMTOKEN;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NMTOKENS;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NONNEGATIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NONPOSITIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NORMALIZED_STRING;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NOTATION;
import static javax.xml.xquery.XQItemType.XQBASETYPE_POSITIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_QNAME;
import static javax.xml.xquery.XQItemType.XQBASETYPE_SHORT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_STRING;
import static javax.xml.xquery.XQItemType.XQBASETYPE_TIME;
import static javax.xml.xquery.XQItemType.XQBASETYPE_TOKEN;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_BYTE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_INT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_LONG;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_SHORT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNTYPED;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNTYPEDATOMIC;
import static javax.xml.xquery.XQItemType.XQBASETYPE_YEARMONTHDURATION;
import static javax.xml.xquery.XQItemType.XQITEMKIND_ATOMIC;
import static javax.xml.xquery.XQItemType.XQITEMKIND_ATTRIBUTE;
import static javax.xml.xquery.XQItemType.XQITEMKIND_DOCUMENT_ELEMENT;
import static javax.xml.xquery.XQItemType.XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT;
import static javax.xml.xquery.XQItemType.XQITEMKIND_ELEMENT;
import static javax.xml.xquery.XQItemType.XQITEMKIND_PI;
import static javax.xml.xquery.XQItemType.XQITEMKIND_SCHEMA_ATTRIBUTE;
import static javax.xml.xquery.XQItemType.XQITEMKIND_SCHEMA_ELEMENT;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import org.apache.xerces.impl.dv.util.Base64;
import org.apache.xerces.util.XMLChar;
import org.w3c.dom.Node;

import com.bagri.xdm.api.XDMException;

/**
 * set of XQJ static utilities 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XQUtils {
	
	private static DatatypeFactory dtFactory;
	static {
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException("Can not instantiate datatype factory");
		}
	}

	/**
	 * converts XQJ baseType to the corresponding {@link QName}. Return null if the provided type does not correspond to any XQJ type constant.
	 * 
	 * @param baseType one of XQJ base type constants 
	 * @return QName representation for the type specified
	 */
    public static QName getTypeName(int baseType) {
    	switch (baseType) {
    		case XQBASETYPE_ANYATOMICTYPE: return new QName(xs_ns, "anyAtomicType", xs_prefix);
    		case XQBASETYPE_ANYSIMPLETYPE: return new QName(xs_ns, "anySimpleType", xs_prefix);
    		case XQBASETYPE_ANYTYPE: return new QName(xs_ns, "anyType", xs_prefix);
    		case XQBASETYPE_ANYURI: return new QName(xs_ns, "anyURI", xs_prefix);
    		case XQBASETYPE_BASE64BINARY: return new QName(xs_ns, "base64Binary", xs_prefix);
    		case XQBASETYPE_BOOLEAN: return new QName(xs_ns, "boolean", xs_prefix);
    		case XQBASETYPE_BYTE: return new QName(xs_ns, "byte", xs_prefix);
    		case XQBASETYPE_DATE: return new QName(xs_ns, "date");
    		case XQBASETYPE_DATETIME: return new QName(xs_ns, "dateTime", xs_prefix);
    		case XQBASETYPE_DAYTIMEDURATION: return new QName(xs_ns, "dayTimeDuration", xs_prefix);
    		case XQBASETYPE_DECIMAL: return new QName(xs_ns, "decimal", xs_prefix);
    		case XQBASETYPE_DOUBLE: return new QName(xs_ns, "double", xs_prefix);
    		case XQBASETYPE_DURATION: return new QName(xs_ns, "duration", xs_prefix);
    		case XQBASETYPE_ENTITIES: return new QName(xs_ns, "ENTITIES", xs_prefix);
    		case XQBASETYPE_ENTITY: return new QName(xs_ns, "ENTITY", xs_prefix);
    		case XQBASETYPE_FLOAT: return new QName(xs_ns, "float", xs_prefix);
    		case XQBASETYPE_GDAY: return new QName(xs_ns, "gDay", xs_prefix);
    		case XQBASETYPE_GMONTH: return new QName(xs_ns, "gMonth", xs_prefix);
    		case XQBASETYPE_GMONTHDAY: return new QName(xs_ns, "gMonthDay", xs_prefix);
    		case XQBASETYPE_GYEAR: return new QName(xs_ns, "gYear", xs_prefix);
    		case XQBASETYPE_GYEARMONTH: return new QName(xs_ns, "gYearMonth", xs_prefix);
    		case XQBASETYPE_HEXBINARY: return new QName(xs_ns, "hexBinary", xs_prefix);
    		case XQBASETYPE_ID: return new QName(xs_ns, "ID", xs_prefix);
    		case XQBASETYPE_IDREF: return new QName(xs_ns, "IDREF", xs_prefix);
    		case XQBASETYPE_IDREFS: return new QName(xs_ns, "IDREFS", xs_prefix);
    		case XQBASETYPE_INT: return new QName(xs_ns, "int", xs_prefix);
    		case XQBASETYPE_INTEGER: return new QName(xs_ns, "integer", xs_prefix);
    		case XQBASETYPE_LANGUAGE: return new QName(xs_ns, "language", xs_prefix);
    		case XQBASETYPE_LONG: return new QName(xs_ns, "long", xs_prefix);
    		case XQBASETYPE_NAME: return new QName(xs_ns, "Name", xs_prefix);
    		case XQBASETYPE_NCNAME: return new QName(xs_ns, "NCName", xs_prefix);
    		case XQBASETYPE_NEGATIVE_INTEGER: return new QName(xs_ns, "negativeInteger", xs_prefix);
    		case XQBASETYPE_NMTOKEN: return new QName(xs_ns, "NMTOKEN", xs_prefix);
    		case XQBASETYPE_NMTOKENS: return new QName(xs_ns, "NMTOKENS", xs_prefix);
    		case XQBASETYPE_NONNEGATIVE_INTEGER: return new QName(xs_ns, "nonNegativeInteger", xs_prefix);
    		case XQBASETYPE_NONPOSITIVE_INTEGER: return new QName(xs_ns, "nonPositiveInteger", xs_prefix);
    		case XQBASETYPE_NORMALIZED_STRING: return new QName(xs_ns, "normalizedString", xs_prefix);
    		case XQBASETYPE_NOTATION: return new QName(xs_ns, "NOTATION", xs_prefix);
    		case XQBASETYPE_POSITIVE_INTEGER: return new QName(xs_ns, "positiveInteger", xs_prefix);
    		case XQBASETYPE_QNAME: return new QName(xs_ns, "QName", xs_prefix);
    		case XQBASETYPE_SHORT: return new QName(xs_ns, "short", xs_prefix);
    		case XQBASETYPE_STRING: return new QName(xs_ns, "string", xs_prefix);
    		case XQBASETYPE_TIME: return new QName(xs_ns, "time", xs_prefix);
    		case XQBASETYPE_TOKEN: return new QName(xs_ns, "token", xs_prefix);
    		case XQBASETYPE_UNSIGNED_BYTE: return new QName(xs_ns, "unsignedByte", xs_prefix);
    		case XQBASETYPE_UNSIGNED_INT: return new QName(xs_ns, "unsignedInt", xs_prefix);
    		case XQBASETYPE_UNSIGNED_LONG: return new QName(xs_ns, "unsignedLong", xs_prefix);
    		case XQBASETYPE_UNSIGNED_SHORT: return new QName(xs_ns, "unsignedShort", xs_prefix);
    		case XQBASETYPE_UNTYPED: return new QName(xs_ns, "untyped", xs_prefix);
    		case XQBASETYPE_UNTYPEDATOMIC: return new QName(xs_ns, "untypedAtomic", xs_prefix);
    		case XQBASETYPE_YEARMONTHDURATION: return new QName(xs_ns, "yearMonthDuration", xs_prefix);
    	}
    	return null;
    }
    
    /**
     * converts String value to its Object representation. The resulting class constructed from the type provided.  
     * 
     * @param baseType one of XQJ base type constants
     * @param value String representation of the value
     * @return Object representation of the value
     */
	public static Object getAtomicValue(int baseType, String value) {
    	switch (baseType) {
			case XQBASETYPE_ANYATOMICTYPE: return value;
			case XQBASETYPE_ANYSIMPLETYPE: return value;
			case XQBASETYPE_ANYTYPE: return value;
			case XQBASETYPE_ANYURI: return URI.create(value);
			case XQBASETYPE_BASE64BINARY: return Base64.encode(value.getBytes());
			case XQBASETYPE_BOOLEAN: return new Boolean(value);
			case XQBASETYPE_BYTE: return new Byte(value);
			case XQBASETYPE_DATE: return dtFactory.newXMLGregorianCalendar(value);
			case XQBASETYPE_DATETIME: return dtFactory.newXMLGregorianCalendar(value);
			case XQBASETYPE_DAYTIMEDURATION: return dtFactory.newDurationDayTime(value); 
			case XQBASETYPE_DECIMAL: return new BigDecimal(value);
			case XQBASETYPE_DOUBLE: return new Double(value);
			case XQBASETYPE_DURATION: return dtFactory.newDuration(value); 
			case XQBASETYPE_ENTITIES: return value;
			case XQBASETYPE_ENTITY: return value;
			case XQBASETYPE_FLOAT: return new Float(value);
			case XQBASETYPE_GDAY: return dtFactory.newXMLGregorianCalendar(value);
			case XQBASETYPE_GMONTH: return dtFactory.newXMLGregorianCalendar(value);
			case XQBASETYPE_GMONTHDAY: return dtFactory.newXMLGregorianCalendar(value);
			case XQBASETYPE_GYEAR: return dtFactory.newXMLGregorianCalendar(value);
			case XQBASETYPE_GYEARMONTH: return dtFactory.newXMLGregorianCalendar(value);
			case XQBASETYPE_HEXBINARY: return Base64.encode(value.getBytes());
			case XQBASETYPE_ID: return value;
			case XQBASETYPE_IDREF: return value;
			case XQBASETYPE_IDREFS: return value;
			case XQBASETYPE_INT: return new Integer(value);
			case XQBASETYPE_INTEGER: return new BigInteger(value);
			case XQBASETYPE_LANGUAGE: return value;
			case XQBASETYPE_LONG: return new Long(value);
			case XQBASETYPE_NAME: return value;
			case XQBASETYPE_NCNAME: return value;
			case XQBASETYPE_NEGATIVE_INTEGER: return new BigInteger(value);
			case XQBASETYPE_NMTOKEN: return value;
			case XQBASETYPE_NMTOKENS: return value;
			case XQBASETYPE_NONNEGATIVE_INTEGER: return new BigInteger(value);
			case XQBASETYPE_NONPOSITIVE_INTEGER: return new BigInteger(value);
			case XQBASETYPE_NORMALIZED_STRING: return value;
			case XQBASETYPE_NOTATION: return value;
			case XQBASETYPE_POSITIVE_INTEGER: return new BigInteger(value);
			case XQBASETYPE_QNAME: return new QName(value);
			case XQBASETYPE_SHORT: return new Short(value);
			case XQBASETYPE_STRING: return value;
			case XQBASETYPE_TIME: return dtFactory.newXMLGregorianCalendar(value);
			case XQBASETYPE_TOKEN: return value;
			case XQBASETYPE_UNSIGNED_BYTE: return new Byte(value);
			case XQBASETYPE_UNSIGNED_INT: return new Integer(value);
			case XQBASETYPE_UNSIGNED_LONG: return new Long(value);
			case XQBASETYPE_UNSIGNED_SHORT: return new Short(value);
			case XQBASETYPE_UNTYPED: return value;
			case XQBASETYPE_UNTYPEDATOMIC: return value;
			case XQBASETYPE_YEARMONTHDURATION: return dtFactory.newDurationYearMonth(value);
		}
		return null;
	}

	/**
	 * converts {@link QName} to the corresponding XQJ baseType. 
	 * Return XQBASETYPE_STRING if the provided typeName's local name is not recognized.
	 * Return XQBASETYPE_ANYTYPE if the provided typeName is not from standard xs: namespace.
	 * 
	 * @param typeName the QName type representation 
	 * @return one of XQJ base type constants
	 */
	public static int getBaseTypeForTypeName(QName typeName) {
		if (xs_ns.equals(typeName.getNamespaceURI())) {
			switch (typeName.getLocalPart()) {
				case "anyAtomicType": return XQBASETYPE_ANYATOMICTYPE;
				case "anySimpleType": return XQBASETYPE_ANYSIMPLETYPE;
				case "anyType": return XQBASETYPE_ANYTYPE;
				case "anyURI": return XQBASETYPE_ANYURI;
				case "base64Binary": return XQBASETYPE_BASE64BINARY;
				case "boolean": return XQBASETYPE_BOOLEAN;
				case "byte": return XQBASETYPE_BYTE;
				case "date": return XQBASETYPE_DATE;
				case "dateTime": return XQBASETYPE_DATETIME; 
	    		case "dayTimeDuration": return XQBASETYPE_DAYTIMEDURATION;
	    		case "decimal": return XQBASETYPE_DECIMAL;
	    		case "double": return XQBASETYPE_DOUBLE;
	    		case "duration": return XQBASETYPE_DURATION;
	    		case "ENTITIES": return XQBASETYPE_ENTITIES;
	    		case "ENTITY": return XQBASETYPE_ENTITY;
	    		case "float": return XQBASETYPE_FLOAT;
	    		case "gDay": return XQBASETYPE_GDAY;
	    		case "gMonth": return XQBASETYPE_GMONTH;
	    		case "gMonthDay": return XQBASETYPE_GMONTHDAY;
	    		case "gYear": return XQBASETYPE_GYEAR;
	    		case "gYearMonth": return XQBASETYPE_GYEARMONTH;
	    		case "hexBinary": return XQBASETYPE_HEXBINARY;
	    		case "ID": return XQBASETYPE_ID;
	    		case "IDREF": return XQBASETYPE_IDREF;
	    		case "IDREFS": return XQBASETYPE_IDREFS;
	    		case "int": return XQBASETYPE_INT;
	    		case "integer": return XQBASETYPE_INTEGER;
	    		case "language": return XQBASETYPE_LANGUAGE;
	    		case "long": return XQBASETYPE_LONG;
	    		case "Name": return XQBASETYPE_NAME;
	    		case "NCName": return XQBASETYPE_NCNAME;
	    		case "negativeInteger": return XQBASETYPE_NEGATIVE_INTEGER;
	    		case "NMTOKEN": return XQBASETYPE_NMTOKEN;
	    		case "NMTOKENS": return XQBASETYPE_NMTOKENS;
	    		case "nonNegativeInteger": return XQBASETYPE_NONNEGATIVE_INTEGER;
	    		case "nonPositiveInteger": return XQBASETYPE_NONPOSITIVE_INTEGER;
	    		case "normalizedString": return XQBASETYPE_NORMALIZED_STRING;
	    		case "NOTATION": return XQBASETYPE_NOTATION;
	    		case "positiveInteger": return XQBASETYPE_POSITIVE_INTEGER;
	    		case "QName": return XQBASETYPE_QNAME;
	    		case "short": return XQBASETYPE_SHORT;
	    		case "string": return XQBASETYPE_STRING;
	    		case "time": return XQBASETYPE_TIME;
	    		case "token": return XQBASETYPE_TOKEN;
	    		case "unsignedByte": return XQBASETYPE_UNSIGNED_BYTE;
	    		case "unsignedInt": return XQBASETYPE_UNSIGNED_INT;
	    		case "unsignedLong": return XQBASETYPE_UNSIGNED_LONG;
	    		case "unsignedShort": return XQBASETYPE_UNSIGNED_SHORT;
	    		case "untyped": return XQBASETYPE_UNTYPED;
	    		case "untypedAtomic": return XQBASETYPE_UNTYPEDATOMIC;
	    		case "yearMonthDuration": return XQBASETYPE_YEARMONTHDURATION;
			}
			return XQBASETYPE_STRING;
		}
		return XQBASETYPE_ANYTYPE;
	}
    
	/**
	 * checks if the provided XQJ base type constant corresponds to XQJ atomic type or not 
	 * 
	 * @param type one of XQJ base type constants 
	 * @return true if the {@code type} corresponds to XQJ atomic type, false otherwise
	 */
    public static boolean isAtomicType(int type) {
    	return type >= XQBASETYPE_ANYATOMICTYPE && type <= XQBASETYPE_ENTITY; 
    }

    /**
     * checks if XQJ base type feature is supported by the data kind provided 
     * 
     * @param kind one of XQJ data kind constants
     * @return true if base type supported for this data kind, false otherwise
     */
	public static boolean isBaseTypeSupported(int kind) {
		return kind == XQITEMKIND_DOCUMENT_ELEMENT || kind ==  XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT 
			|| kind ==  XQITEMKIND_ELEMENT || kind == XQITEMKIND_SCHEMA_ELEMENT 
			|| kind == XQITEMKIND_ATTRIBUTE || kind == XQITEMKIND_SCHEMA_ATTRIBUTE 
			|| kind == XQITEMKIND_ATOMIC;
	}
	
	/**
	 * checks if the node name feature is supported by the data kind provided
	 * 
	 * @param kind one of XQJ data kind constants
	 * @return true if node name is supported, false otherwise
	 */
	public static boolean isNodeNameSupported(int kind) {
		return kind == XQITEMKIND_DOCUMENT_ELEMENT || kind ==  XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT 
				|| kind ==  XQITEMKIND_ELEMENT || kind == XQITEMKIND_SCHEMA_ELEMENT 
				|| kind == XQITEMKIND_ATTRIBUTE || kind == XQITEMKIND_SCHEMA_ATTRIBUTE; 
	}

	/**
	 * checks if the processing instructions name feature is supported by the data kind provided
	 * 
	 * @param kind one of XQJ data kind constants
	 * @return true if processing instruction name is supported, false otherwise
	 */
	public static boolean isPINameSupported(int kind) {
		return kind == XQITEMKIND_PI;
	}

	/**
	 * checks if the provided XQJ base type constant is assignable from String
	 * 
	 * @param baseType one of XQJ base type constants 
	 * @return true if the value of the type is compatible with String, false otherwise
	 */
    public static boolean isStringTypeCompatible(int baseType) {
    	return baseType == XQBASETYPE_ANYATOMICTYPE || baseType == XQBASETYPE_ANYSIMPLETYPE
    			|| baseType == XQBASETYPE_ANYTYPE || baseType == XQBASETYPE_ENTITIES
    			|| baseType == XQBASETYPE_ENTITY || baseType == XQBASETYPE_ID
    			|| baseType == XQBASETYPE_IDREF || baseType == XQBASETYPE_IDREF
    			|| baseType == XQBASETYPE_LANGUAGE || baseType == XQBASETYPE_NAME
    			|| baseType == XQBASETYPE_NCNAME || baseType == XQBASETYPE_NMTOKEN
    			|| baseType == XQBASETYPE_NMTOKENS || baseType ==  XQBASETYPE_NORMALIZED_STRING
    			|| baseType == XQBASETYPE_NOTATION || baseType == XQBASETYPE_STRING
    			|| baseType == XQBASETYPE_TOKEN || baseType == XQBASETYPE_UNTYPED
    			|| baseType == XQBASETYPE_UNTYPEDATOMIC;
    }
    
	/**
	 * checks if the provided XQJ base type constant is compatible with provided {@code value}
	 * 
	 * @param baseType one of XQJ base type constants
	 * @param value the Object value representation
	 * @return true if the type is compatible with the value, false otherwise
	 */
    public static boolean isTypeValueCompatible(int baseType, Object value) {
    	String sval = value.toString();
    	switch (baseType) {
    		case XQBASETYPE_ANYATOMICTYPE: 
    		case XQBASETYPE_ANYSIMPLETYPE: 
    		case XQBASETYPE_ANYTYPE: return true; 
    		case XQBASETYPE_ANYURI: if (value instanceof URI) {
    				return true;
    			} else if (value instanceof String) {
    				try {
    					URI.create(sval);
    					return true;
    				} catch (Exception ex) {
    					//
    				}
    			}
    			return false;
    		case XQBASETYPE_BASE64BINARY: return true; //? 
    		case XQBASETYPE_BOOLEAN: if (value instanceof Boolean) {
    				return true;
    			} else if (value instanceof String) {
    				try {
    					Boolean.parseBoolean(sval);
    					return true;
    				} catch (Exception e) {
    					//
    				}
    			}
    			return false;
    		case XQBASETYPE_BYTE: 
    		case XQBASETYPE_UNSIGNED_BYTE: 
    			if (value instanceof Byte) {
    				return true;
    			}
    			try {
    				Byte.parseByte(sval);
    				return true;
    			} catch (Exception e) {
    			}     
    			return false;
    		case XQBASETYPE_DECIMAL: if (value instanceof BigDecimal) {
    				return true;
    			}
    			try {
    				new BigDecimal(sval);
    				return true;
    			} catch (Exception e) {
    			}
    			return false;
    		case XQBASETYPE_DOUBLE: if (value instanceof Double) {
    				return true;
    			}
    			try {
    				Double.parseDouble(sval);
    				return true;
    			} catch (Exception e) {
    			} 
    			return false;
    		case XQBASETYPE_DURATION: 
    		case XQBASETYPE_DAYTIMEDURATION: 
    		case XQBASETYPE_YEARMONTHDURATION: if (value instanceof Duration) {
    				return true;
    			}
    			return false;
    		case XQBASETYPE_ENTITIES: 
    		case XQBASETYPE_ENTITY: return true;
    		case XQBASETYPE_FLOAT: if (value instanceof Float) {
    				return true;
    			}
    			try {
    				Float.parseFloat(sval);
    				return true;
    			} catch (Exception e) {
    			}
    			return false;
    		case XQBASETYPE_DATE: 
    		case XQBASETYPE_DATETIME: 
    		case XQBASETYPE_TIME: 
    		case XQBASETYPE_GDAY: 
    		case XQBASETYPE_GMONTH: 
    		case XQBASETYPE_GMONTHDAY: 
    		case XQBASETYPE_GYEAR: 
    		case XQBASETYPE_GYEARMONTH: if (value instanceof XMLGregorianCalendar) {
    				return true;
    			}
    			return false;
    		case XQBASETYPE_HEXBINARY: 
    		case XQBASETYPE_ID: 
    		case XQBASETYPE_IDREF:
    		case XQBASETYPE_IDREFS: return true;
    		case XQBASETYPE_INT: 
    		case XQBASETYPE_UNSIGNED_INT: 
    			if (value instanceof Integer) {
    				return true;
    			}
    			try {
    				Integer.parseInt(sval);
    				return true;
    			} catch (Exception e) {
    				//
    			}     
    			return false;
    		case XQBASETYPE_INTEGER: 
    		case XQBASETYPE_NEGATIVE_INTEGER: 
    		case XQBASETYPE_NONNEGATIVE_INTEGER: 
    		case XQBASETYPE_NONPOSITIVE_INTEGER: 
    		case XQBASETYPE_POSITIVE_INTEGER: 
    			if (value instanceof BigInteger) {
    				return true;
    			}
    			try {
    				new BigInteger(sval);
    				return true;
    			} catch (Exception e) {
    			}
    			return false;
    		case XQBASETYPE_LANGUAGE: return true; //?
    		case XQBASETYPE_LONG: 
    		case XQBASETYPE_UNSIGNED_LONG: if (value instanceof Long) {
    				return true;
    			}
    			try {
    				Long.parseLong(sval);
    				return true;
    			} catch (Exception e) {
    			}
    			return false;
    		case XQBASETYPE_NAME: return true;
    		case XQBASETYPE_NCNAME:  
    			if (XMLChar.isValidNCName(sval)) {
    				return true;
    			}
    			return false;
    		case XQBASETYPE_NMTOKEN: 
    		case XQBASETYPE_NMTOKENS:
    		case XQBASETYPE_NORMALIZED_STRING: 
    		case XQBASETYPE_NOTATION: 
    		case XQBASETYPE_QNAME: return true; 
    		case XQBASETYPE_SHORT: 
    		case XQBASETYPE_UNSIGNED_SHORT: if (value instanceof Short) {
    				return true;
    			}
    			try {
    				Integer.parseInt(sval);
    				return true;
    			} catch (Exception e) {
    			}
    			return false;
    		case XQBASETYPE_STRING: return true;
    		case XQBASETYPE_TOKEN:
    		case XQBASETYPE_UNTYPED: 
    		case XQBASETYPE_UNTYPEDATOMIC: return true;
    	}
    	return false;
    }
	
    /**
     * constructs XQJ item type for the {@code value} specified
     * 
     * @param factory the XQJ data factory to produce XQ item type
     * @param value the value to get item type from
     * @return XQ item type 
     * @throws XQException in case of construction error
     */
	public static XQItemType getTypeForObject(XQDataFactory factory, Object value) throws XQException {
		
		if (value instanceof org.w3c.dom.Node) {
			return getTypeForNode(factory, (org.w3c.dom.Node) value);
		}
		
		int baseType = XQBASETYPE_ANYATOMICTYPE; //XQBASETYPE_ANYTYPE;
		if (value instanceof XQItem) {
			value = ((XQItem) value).getObject();
		}
		
		if (value instanceof Integer) {
			baseType = XQBASETYPE_INT; 
		} else if (value instanceof Long) {
			baseType = XQBASETYPE_LONG;
		} else if (value instanceof Double) {
			baseType = XQBASETYPE_DOUBLE;
		} else if (value instanceof Boolean) {
			baseType = XQBASETYPE_BOOLEAN;
		} else if (value instanceof Short) {
			baseType = XQBASETYPE_SHORT;
		} else if (value instanceof Float) {
			baseType = XQBASETYPE_FLOAT;
		} else if (value instanceof Byte) {
			baseType = XQBASETYPE_BYTE;
		} else if (value instanceof String) {
			baseType = XQBASETYPE_STRING;
		} else if (value instanceof java.math.BigDecimal) {
			baseType = XQBASETYPE_DECIMAL;
		} else if (value instanceof java.math.BigInteger) {
			baseType = XQBASETYPE_INTEGER;
		} else if (value instanceof javax.xml.datatype.Duration) {
			javax.xml.datatype.Duration d = (javax.xml.datatype.Duration) value;
			boolean setYM = d.isSet(DatatypeConstants.YEARS) || d.isSet(DatatypeConstants.MONTHS);
			boolean setDT = d.isSet(DatatypeConstants.DAYS) || d.isSet(DatatypeConstants.HOURS) ||
					d.isSet(DatatypeConstants.MINUTES) || d.isSet(DatatypeConstants.SECONDS);
			if (setYM) {
				if (setDT) {
					baseType = XQBASETYPE_DURATION;
				} else {
					baseType = XQBASETYPE_YEARMONTHDURATION;
				}
			} else {
				baseType = XQBASETYPE_DAYTIMEDURATION;
			}
		} else if (value instanceof javax.xml.datatype.XMLGregorianCalendar) {
			javax.xml.datatype.XMLGregorianCalendar c = (javax.xml.datatype.XMLGregorianCalendar) value;
			boolean setYear = c.getYear() != DatatypeConstants.FIELD_UNDEFINED;
			boolean setMonth = c.getMonth() != DatatypeConstants.FIELD_UNDEFINED;
			boolean setDay = c.getDay() != DatatypeConstants.FIELD_UNDEFINED;
			boolean setHour = c.getHour() != DatatypeConstants.FIELD_UNDEFINED;
			boolean setMinute = c.getMinute() != DatatypeConstants.FIELD_UNDEFINED;
			boolean setSecond = c.getSecond() != DatatypeConstants.FIELD_UNDEFINED;
			if (setYear) {
				if (setMonth) {
					if (setDay) {
						if (setHour || setMinute || setSecond) {
							baseType = XQBASETYPE_DATETIME;
						} else {
							baseType = XQBASETYPE_DATE;
						}
					} else {
						baseType = XQBASETYPE_GYEARMONTH;
					}
				} else {
					baseType = XQBASETYPE_GYEAR;
				}
			} else {
				if (setMonth) {
					if (setDay) {
						baseType = XQBASETYPE_GMONTHDAY;
					} else {
						baseType = XQBASETYPE_GMONTH;
					}
				} else {
					if (setDay) {
						baseType = XQBASETYPE_GDAY;
					} else {
						if (setHour || setMinute || setSecond) {
							baseType = XQBASETYPE_TIME;
						} else {
							throw new XQException("Unknown Calendar type: " + c);
						}
					}
				}
			}
		} else if (value instanceof javax.xml.namespace.QName) {
			baseType = XQBASETYPE_QNAME;
		}

		return factory.createAtomicType(baseType, getTypeName(baseType), null);
	}

	/**
	 * converts {@link GregorianCalendar} to the corresponding {@link XMLGregorianCalendar} instance 
	 * 
	 * @param gc the initial GregorianCalendar instance
	 * @param cType one of XQJ base type constants 
	 * @return XML gregorian calendar instance
	 */
	public static XMLGregorianCalendar getXMLCalendar(GregorianCalendar gc, int cType) { 
    	switch (cType) {
    		case XQBASETYPE_DATE:
    			return dtFactory.newXMLGregorianCalendarDate(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH) + 1, 
    					gc.get(Calendar.DAY_OF_MONTH), gc.get(Calendar.ZONE_OFFSET)); 
    		case XQBASETYPE_GDAY: 
    			return dtFactory.newXMLGregorianCalendarDate(DatatypeConstants.FIELD_UNDEFINED, 
    					DatatypeConstants.FIELD_UNDEFINED, gc.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED); 
    		case XQBASETYPE_GMONTH:  
    			return dtFactory.newXMLGregorianCalendarDate(DatatypeConstants.FIELD_UNDEFINED, 
    					gc.get(Calendar.MONTH) + 1, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED); 
    		case XQBASETYPE_GMONTHDAY:  
    			return dtFactory.newXMLGregorianCalendarDate(DatatypeConstants.FIELD_UNDEFINED, 
    					gc.get(Calendar.MONTH) + 1, gc.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED); 
    		case XQBASETYPE_GYEAR:  
    			return dtFactory.newXMLGregorianCalendarDate(gc.get(Calendar.YEAR), 
    					DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED); 
    		case XQBASETYPE_GYEARMONTH: 
    			return dtFactory.newXMLGregorianCalendarDate(gc.get(Calendar.YEAR), 
    					gc.get(Calendar.MONTH) + 1, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED); 
    		case XQBASETYPE_TIME:
    			return dtFactory.newXMLGregorianCalendarTime(gc.get(Calendar.HOUR), gc.get(Calendar.MINUTE), 
    					gc.get(Calendar.SECOND), gc.get(Calendar.MILLISECOND), gc.get(Calendar.ZONE_OFFSET)); 
    		//default: //XQBASETYPE_DATETIME 
    	}
    	return dtFactory.newXMLGregorianCalendar(gc);
    }
	
	/**
	 * converts String representation of duration to its XML equivalent.
	 * Returns null if the type provided does not correspond to any XML duration types.
	 * 
	 * @param duration the String duration representation
	 * @param dType one of XQJ base type constants 
	 * @return XML {@link Duration} instance or null
	 */
    public static Duration getXMLDuration(String duration, int dType) { 
    	switch (dType) {
			case XQBASETYPE_DURATION: return dtFactory.newDuration(duration); 
			case XQBASETYPE_DAYTIMEDURATION: return dtFactory.newDurationDayTime(duration); 
			case XQBASETYPE_YEARMONTHDURATION: return dtFactory.newDurationYearMonth(duration);
    	}
    	return null;
    }
	
    /**
     * constructs XQJ item type for the w3c {@link Node} instance provided
     * 
     * @param factory the XQJ data factory to produce XQ item type
     * @param node w3c XML Node instance
     * @return XQ item type
     * @throws XQException in case of processing error
     */
	public static XQItemType getTypeForNode(XQDataFactory factory, org.w3c.dom.Node node) throws XQException {
		//new URI(node.getBaseURI()));
		switch (node.getNodeType()) {
			case Node.DOCUMENT_NODE:
				return factory.createDocumentType();
			case Node.DOCUMENT_FRAGMENT_NODE:
				return factory.createDocumentType();
				//return factory.createDocumentElementType(
				//		factory.createElementType(new QName(node.getNodeName()), XQItemType.XQBASETYPE_ANYTYPE));
			case Node.ELEMENT_NODE: 
				return factory.createElementType(new QName(node.getNodeName()), XQItemType.XQBASETYPE_ANYTYPE);
			case Node.ATTRIBUTE_NODE:
				return factory.createAttributeType(new QName(node.getNodeName()), XQItemType.XQBASETYPE_ANYSIMPLETYPE);
			case Node.COMMENT_NODE:
				return factory.createCommentType();
			case Node.PROCESSING_INSTRUCTION_NODE:
				return factory.createProcessingInstructionType(null);
			case  Node.TEXT_NODE:
				return factory.createTextType();
			default: 
				return factory.createNodeType();
		}
	}

	/**
	 * a utility method to extract XQ exception information from the error stack provided 
	 * 
	 * @param ex the full error chain
	 * @return XQ exception  
	 */
	public static XQException getXQException(Throwable ex) {

		int errorCode = 0;
		Throwable init = ex;
		while (ex != null) {
			if (ex instanceof XQException) {
				return (XQException) ex;
			} else if (errorCode == 0 && ex instanceof XDMException) {
				errorCode = ((XDMException) ex).getErrorCode();
			}
			ex = ex.getCause();
		}
		XQException xqe = new XQException(init.getMessage() == null ? "null" : init.getMessage(), String.valueOf(errorCode));
		xqe.initCause(ex);
		return xqe;
	}

}
