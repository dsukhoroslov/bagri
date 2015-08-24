package com.bagri.xqj;

import static com.bagri.xdm.common.XDMConstants.xs_ns;
import static com.bagri.xdm.common.XDMConstants.xs_prefix;
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

import org.apache.xerces.util.XMLChar;
import org.w3c.dom.Node;

import com.bagri.xdm.api.XDMException;

public class BagriXQUtils {
	
	private static DatatypeFactory dtFactory;
	static {
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException("Can not instantiate datatype factory");
		}
	}

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
    
    public static boolean isAtomicType(int type) {
    	return type >= XQBASETYPE_ANYATOMICTYPE && type <= XQBASETYPE_ENTITY; 
    }

	public static boolean isBaseTypeSupported(int kind) {
		return kind == XQITEMKIND_DOCUMENT_ELEMENT || kind ==  XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT 
			|| kind ==  XQITEMKIND_ELEMENT || kind == XQITEMKIND_SCHEMA_ELEMENT 
			|| kind == XQITEMKIND_ATTRIBUTE || kind == XQITEMKIND_SCHEMA_ATTRIBUTE 
			|| kind == XQITEMKIND_ATOMIC;
	}
	
	public static boolean isNodeNameSupported(int kind) {
		return kind == XQITEMKIND_DOCUMENT_ELEMENT || kind ==  XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT 
			|| kind ==  XQITEMKIND_ELEMENT || kind == XQITEMKIND_SCHEMA_ELEMENT 
			|| kind == XQITEMKIND_ATTRIBUTE || kind == XQITEMKIND_SCHEMA_ATTRIBUTE; 
	}
	
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
	
	public static boolean isPINameSupported(int kind) {
		return kind == XQITEMKIND_PI;
	}

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

    
	public static XMLGregorianCalendar getXMLCalendar(GregorianCalendar gc, int cType) { 
    	switch (cType) {
    		case XQItemType.XQBASETYPE_DATE:
    			return dtFactory.newXMLGregorianCalendarDate(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH) + 1, 
    					gc.get(Calendar.DAY_OF_MONTH), gc.get(Calendar.ZONE_OFFSET)); 
    		case XQItemType.XQBASETYPE_GDAY: 
    			return dtFactory.newXMLGregorianCalendarDate(DatatypeConstants.FIELD_UNDEFINED, 
    					DatatypeConstants.FIELD_UNDEFINED, gc.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED); 
    		case XQItemType.XQBASETYPE_GMONTH:  
    			return dtFactory.newXMLGregorianCalendarDate(DatatypeConstants.FIELD_UNDEFINED, 
    					gc.get(Calendar.MONTH) + 1, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED); 
    		case XQItemType.XQBASETYPE_GMONTHDAY:  
    			return dtFactory.newXMLGregorianCalendarDate(DatatypeConstants.FIELD_UNDEFINED, 
    					gc.get(Calendar.MONTH) + 1, gc.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED); 
    		case XQItemType.XQBASETYPE_GYEAR:  
    			return dtFactory.newXMLGregorianCalendarDate(gc.get(Calendar.YEAR), 
    					DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED); 
    		case XQItemType.XQBASETYPE_GYEARMONTH: 
    			return dtFactory.newXMLGregorianCalendarDate(gc.get(Calendar.YEAR), 
    					gc.get(Calendar.MONTH) + 1, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED); 
    		case XQItemType.XQBASETYPE_TIME:
    			return dtFactory.newXMLGregorianCalendarTime(gc.get(Calendar.HOUR), gc.get(Calendar.MINUTE), 
    					gc.get(Calendar.SECOND), gc.get(Calendar.MILLISECOND), gc.get(Calendar.ZONE_OFFSET)); 
    		//default: //XQItemType.XQBASETYPE_DATETIME 
    	}
    	return dtFactory.newXMLGregorianCalendar(gc);
    }
	
    public static Duration getXMLDuration(String duration, int dType) { 
    	switch (dType) {
			case XQBASETYPE_DURATION: return dtFactory.newDuration(duration); 
			case XQBASETYPE_DAYTIMEDURATION: return dtFactory.newDurationDayTime(duration); 
			case XQBASETYPE_YEARMONTHDURATION: return dtFactory.newDurationYearMonth(duration);
    	}
    	return null;
    }
	
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

	public static void throwXQException(Exception ex) throws XQException {
		XQException xqe = new XQException(ex.getMessage());
		xqe.initCause(ex);
		throw xqe;
	}

	public static void throwXQException(XDMException ex) throws XQException {
		XQException xqe = new XQException(ex.getMessage(), ex.getVendorCode());
		// not sure we have to do this..
		xqe.initCause(ex);
		throw xqe;
	}
}
