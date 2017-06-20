package com.bagri.support.util;

import static com.bagri.core.Constants.*;
import static javax.xml.datatype.DatatypeConstants.*;
import static javax.xml.xquery.XQItemType.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQStaticContext;

import org.apache.xerces.impl.dv.util.Base64;
import org.apache.xerces.util.XMLChar;
import org.w3c.dom.Node;

import com.bagri.core.api.BagriException;

/**
 * set of XQJ static utilities 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XQUtils {
	
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
			case XQBASETYPE_DATE: return XMLUtils.newXMLCalendar(value); 
			case XQBASETYPE_DATETIME: return XMLUtils.newXMLCalendar(value);
			case XQBASETYPE_DAYTIMEDURATION: return XMLUtils.getXMLDuration(value, XQBASETYPE_DAYTIMEDURATION); 
			case XQBASETYPE_DECIMAL: return new BigDecimal(value);
			case XQBASETYPE_DOUBLE: return new Double(value);
			case XQBASETYPE_DURATION: return XMLUtils.getXMLDuration(value, XQBASETYPE_DURATION);
			case XQBASETYPE_ENTITIES: return value;
			case XQBASETYPE_ENTITY: return value;
			case XQBASETYPE_FLOAT: return new Float(value);
			case XQBASETYPE_GDAY: return XMLUtils.newXMLCalendar(value);
			case XQBASETYPE_GMONTH: return XMLUtils.newXMLCalendar(value);
			case XQBASETYPE_GMONTHDAY: return XMLUtils.newXMLCalendar(value);
			case XQBASETYPE_GYEAR: return XMLUtils.newXMLCalendar(value);
			case XQBASETYPE_GYEARMONTH: return XMLUtils.newXMLCalendar(value);
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
			case XQBASETYPE_TIME: return XMLUtils.newXMLCalendar(value);
			case XQBASETYPE_TOKEN: return value;
			case XQBASETYPE_UNSIGNED_BYTE: return new Byte(value);
			case XQBASETYPE_UNSIGNED_INT: return new Integer(value);
			case XQBASETYPE_UNSIGNED_LONG: return new Long(value);
			case XQBASETYPE_UNSIGNED_SHORT: return new Short(value);
			case XQBASETYPE_UNTYPED: return value;
			case XQBASETYPE_UNTYPEDATOMIC: return value;
			case XQBASETYPE_YEARMONTHDURATION: return XMLUtils.getXMLDuration(value, XQBASETYPE_YEARMONTHDURATION);
		}
		return null;
	}

	public static Object getAtomicValue(String typeName, String value) {
		int baseType = getBaseTypeForTypeName(typeName);
		return getAtomicValue(baseType, value);
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
			return getBaseTypeForTypeName(typeName.getLocalPart());
		}
		return XQBASETYPE_ANYTYPE;
	}

	public static int getBaseTypeForTypeName(String typeName) {
		switch (typeName) {
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
		
		int baseType = getBaseTypeForObject(value);
		return factory.createAtomicType(baseType, getTypeName(baseType), null);
	}
	
    /**
     * finds XQJ item type XQBASETYPE constant for the {@code value} specified
     * 
     * @param value the value to get item type from
     * @return XQBASETYPE constant item type 
     * @throws XQException in case of resolution error
     */
	public static int getBaseTypeForObject(Object value) throws XQException {
		
		if (value == null) {
			return XQBASETYPE_ANYTYPE;
		}
		
		if (value instanceof XQItem) {
			value = ((XQItem) value).getObject();
		}
		
		if (value instanceof String) {
			return XQBASETYPE_STRING;
		} else if (value instanceof Integer) {
			return XQBASETYPE_INT; 
		} else if (value instanceof Long) {
			return XQBASETYPE_LONG;
		} else if (value instanceof Double) {
			return XQBASETYPE_DOUBLE;
		} else if (value instanceof Boolean) {
			return XQBASETYPE_BOOLEAN;
		} else if (value instanceof Short) {
			return XQBASETYPE_SHORT;
		} else if (value instanceof Float) {
			return XQBASETYPE_FLOAT;
		} else if (value instanceof Byte) {
			return XQBASETYPE_BYTE;
		} else if (value instanceof byte[]) {
			return XQBASETYPE_BASE64BINARY;
		} else if (value instanceof java.math.BigDecimal) {
			return XQBASETYPE_DECIMAL;
		} else if (value instanceof java.math.BigInteger) {
			return XQBASETYPE_INTEGER;
		} else if (value instanceof javax.xml.datatype.Duration) {
			javax.xml.datatype.Duration d = (javax.xml.datatype.Duration) value;
			boolean setYM = d.isSet(YEARS) || d.isSet(MONTHS);
			boolean setDT = d.isSet(DAYS) || d.isSet(HOURS) || d.isSet(MINUTES) || d.isSet(SECONDS);
			if (setYM) {
				if (setDT) {
					return XQBASETYPE_DURATION;
				} else {
					return XQBASETYPE_YEARMONTHDURATION;
				}
			} else {
				return XQBASETYPE_DAYTIMEDURATION;
			}
		} else if (value instanceof javax.xml.datatype.XMLGregorianCalendar) {
			javax.xml.datatype.XMLGregorianCalendar c = (javax.xml.datatype.XMLGregorianCalendar) value;
			boolean setYear = c.getYear() != FIELD_UNDEFINED;
			boolean setMonth = c.getMonth() != FIELD_UNDEFINED;
			boolean setDay = c.getDay() != FIELD_UNDEFINED;
			boolean setHour = c.getHour() != FIELD_UNDEFINED;
			boolean setMinute = c.getMinute() != FIELD_UNDEFINED;
			boolean setSecond = c.getSecond() != FIELD_UNDEFINED;
			if (setYear) {
				if (setMonth) {
					if (setDay) {
						if (setHour || setMinute || setSecond) {
							return XQBASETYPE_DATETIME;
						} else {
							return XQBASETYPE_DATE;
						}
					} else {
						return XQBASETYPE_GYEARMONTH;
					}
				} else {
					return XQBASETYPE_GYEAR;
				}
			} else {
				if (setMonth) {
					if (setDay) {
						return XQBASETYPE_GMONTHDAY;
					} else {
						return XQBASETYPE_GMONTH;
					}
				} else {
					if (setDay) {
						return XQBASETYPE_GDAY;
					} else {
						if (setHour || setMinute || setSecond) {
							return XQBASETYPE_TIME;
						} else {
							throw new XQException("Unknown Calendar type: " + c);
						}
					}
				}
			}
		} else if (value instanceof javax.xml.namespace.QName) {
			return XQBASETYPE_QNAME;
		} else if (value instanceof org.w3c.dom.Node) { //??
			return XQBASETYPE_UNTYPED;
		} else if (value instanceof URI) {
			return XQBASETYPE_ANYURI;
		}
		
		Class<?> cls = value.getClass();
		if (cls.isEnum()) {
			return XQBASETYPE_STRING;
		}

		return XQBASETYPE_ANYATOMICTYPE; 
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
		switch (node.getNodeType()) {
			case Node.DOCUMENT_NODE:
				return factory.createDocumentType();
			case Node.DOCUMENT_FRAGMENT_NODE:
				return factory.createDocumentType();
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
		Throwable cause = null;
		String message = "";
		while (ex != null) {
			if (ex instanceof XQException) {
				return (XQException) ex;
			} else if (/*errorCode == 0 &&*/ ex instanceof BagriException) {
				// deeper is better!
				message += ex.getMessage() + "; ";
				errorCode = ((BagriException) ex).getErrorCode();
				cause = ex;
			}
			ex = ex.getCause();
		}
		XQException xqe = new XQException(message, String.valueOf(errorCode));
		xqe.initCause(cause);
		return xqe;
	}

	/**
	 * converts XQuery Static Context to set of Properties
	 * 
	 * @param ctx XQuery context
	 * @return java Properties
	 * @throws XQException in case of conversion error
	 */
    public static Properties context2Props(XQStaticContext ctx) throws XQException {
    	Properties result = new Properties();
    	result.put(pn_xqj_baseURI, ctx.getBaseURI());
    	result.setProperty(pn_xqj_bindingMode, String.valueOf(ctx.getBindingMode()));
    	result.setProperty(pn_xqj_boundarySpacePolicy, String.valueOf(ctx.getBoundarySpacePolicy()));
    	result.setProperty(pn_xqj_constructionMode, String.valueOf(ctx.getConstructionMode()));
    	//ctx.getContextItemStaticType()
    	result.setProperty(pn_xqj_copyNamespacesModeInherit, String.valueOf(ctx.getCopyNamespacesModeInherit()));
    	result.setProperty(pn_xqj_copyNamespacesModePreserve, String.valueOf(ctx.getCopyNamespacesModePreserve()));
    	result.setProperty(pn_xqj_defaultCollationUri, ctx.getDefaultCollation());
    	result.setProperty(pn_xqj_defaultElementTypeNamespace, ctx.getDefaultElementTypeNamespace());
    	result.setProperty(pn_xqj_defaultFunctionNamespace, ctx.getDefaultFunctionNamespace());
    	if (ctx.getNamespacePrefixes().length > 0) {
    		StringBuffer namespaces = new StringBuffer();
    		for (String prefix: ctx.getNamespacePrefixes()) {
    			namespaces.append(prefix).append(":").append(ctx.getNamespaceURI(prefix));
    			namespaces.append(" ");
    		}
    		result.put(pn_xqj_defaultNamespaces, namespaces.toString());
    	}
    	result.setProperty(pn_xqj_defaultOrderForEmptySequences, String.valueOf(ctx.getDefaultOrderForEmptySequences()));
    	result.setProperty(pn_xqj_holdability, String.valueOf(ctx.getHoldability()));
    	result.setProperty(pn_xqj_orderingMode, String.valueOf(ctx.getOrderingMode()));
    	result.setProperty(pn_xqj_queryLanguageTypeAndVersion, String.valueOf(ctx.getQueryLanguageTypeAndVersion()));
    	result.setProperty(pn_xqj_queryTimeout, String.valueOf(ctx.getQueryTimeout()));
    	result.setProperty(pn_xqj_scrollability, String.valueOf(ctx.getScrollability()));
    	return result;
    }

	/**
	 * converts set of Properties to XQuery Static Context
	 * 
	 * @param props source Properties to convert
	 * @param ctx target XQuery context
	 * @throws XQException in case of conversion error
	 */
	public static void props2Context(Properties props, XQStaticContext ctx) throws XQException {
		String prop = props.getProperty(pn_xqj_baseURI);
		if (prop != null) {
			ctx.setBaseURI(prop);
		}
		prop = props.getProperty(pn_xqj_bindingMode);
		if (prop != null) {
			ctx.setBindingMode(Integer.valueOf(prop));
		}
		prop = props.getProperty(pn_xqj_boundarySpacePolicy);
		if (prop != null) {
			ctx.setBoundarySpacePolicy(Integer.valueOf(prop));
		}
		// pass it as URI!?
    	//ctx.setContextItemStaticType(...)
		prop = props.getProperty(pn_xqj_constructionMode);
		if (prop != null) {
			ctx.setConstructionMode(Integer.valueOf(prop));
		}
		prop = props.getProperty(pn_xqj_copyNamespacesModeInherit);
		if (prop != null) {
			ctx.setCopyNamespacesModeInherit(Integer.valueOf(prop));
		}
		prop = props.getProperty(pn_xqj_copyNamespacesModePreserve);
		if (prop != null) {
			ctx.setCopyNamespacesModePreserve(Integer.valueOf(prop));
		}
		prop = props.getProperty(pn_xqj_defaultCollationUri);
		if (prop != null) {
			ctx.setDefaultCollation(prop);
		}
		prop = props.getProperty(pn_xqj_defaultElementTypeNamespace);
		if (prop != null) {
			ctx.setDefaultElementTypeNamespace(prop);
		}
		prop = props.getProperty(pn_xqj_defaultFunctionNamespace);
		if (prop != null) {
			ctx.setDefaultFunctionNamespace(prop);
		}
		prop = props.getProperty(pn_xqj_defaultNamespaces);
		if (prop != null) {
			String[] nspaces = prop.split(" ");
			for (String ns: nspaces) {
				int pos = ns.indexOf(":");
				ctx.declareNamespace(ns.substring(0, pos), ns.substring(pos + 1));
			}
		}
		prop = props.getProperty(pn_xqj_defaultOrderForEmptySequences);
		if (prop != null) {
			ctx.setDefaultOrderForEmptySequences(Integer.valueOf(prop));
		}
		prop = props.getProperty(pn_xqj_holdability);
		if (prop != null) {
			ctx.setHoldability(Integer.valueOf(prop));
		}
		prop = props.getProperty(pn_xqj_orderingMode);
		if (prop != null) {
			ctx.setOrderingMode(Integer.valueOf(prop));
		}
		prop = props.getProperty(pn_xqj_queryLanguageTypeAndVersion);
		if (prop != null) {
			ctx.setQueryLanguageTypeAndVersion(Integer.valueOf(prop));
		}
		prop = props.getProperty(pn_xqj_queryTimeout);
		if (prop != null) {
			ctx.setQueryTimeout(Integer.valueOf(prop));
		}
		prop = props.getProperty(pn_xqj_scrollability);
		if (prop != null) {
			ctx.setScrollability(Integer.valueOf(prop));
		}
	}
	
	public static Map<String, Object> sequenceToMap(XQSequence sequence) throws XQException {
		Map<String, Object> result;
		synchronized (sequence) {
			if (sequence.isScrollable()) {
				result = new HashMap<>(sequence.count());
				sequence.beforeFirst();
			} else {
				result = new HashMap<>();
			}
			while (sequence.next()) {
				XQSequence pair = (XQSequence) sequence.getObject();
				pair.beforeFirst();
				if (pair.next()) {
					String key = pair.getAtomicValue();
					if (pair.next()) {
						Object value = pair.getObject();
						result.put(key, value);
					}
				}
			}
		}
       	return result;
	}
	
	public static XQSequence mapToSequence(XQDataFactory factory, Map<String, Object> map) throws XQException {

    	List<XQSequence> pairs = new ArrayList<>(); 
    	for (Map.Entry<String, Object> e: map.entrySet()) {
    		List<XQItemAccessor> pair = new ArrayList<>(2);
    		pair.add(factory.createItemFromString(e.getKey(), factory.createAtomicType(XQBASETYPE_STRING)));
    		pair.add(factory.createItemFromObject(e.getValue(), factory.createAtomicType(getBaseTypeForObject(e.getValue()))));
    		pairs.add(factory.createSequence(pair.iterator()));
    	}
    	return factory.createSequence(pairs.iterator());
	}
}
