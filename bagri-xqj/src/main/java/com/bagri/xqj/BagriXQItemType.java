package com.bagri.xqj;

import java.net.URI;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequenceType;

import org.apache.axis.types.NCName;
import org.w3c.dom.Node;

public class BagriXQItemType extends BagriXQSequenceType implements XQItemType {

	private int baseType;
	private int kind;
	private QName nodeName;
	private QName typeName;
	private boolean nillable;
	private URI schemaURI;

	BagriXQItemType(int baseType, int kind, QName nodeName, QName typeName, boolean nillable, URI schemaURI) {

		super(null, OCC_EXACTLY_ONE);
		this.baseType = baseType;
		this.kind = kind;
		this.nodeName = nodeName;
		this.typeName = typeName;
		this.nillable = nillable;
		this.schemaURI = schemaURI;
	}
	
	@Override
	public XQItemType getItemType() {
		
		return this;
	}
	
	@Override
	public int getBaseType() throws XQException {
		
		if (BagriXQUtils.isBaseTypeSupported(kind)) {
			return baseType;
		}
		throw new XQException("getBaseType is not supported for this item kind: " + kind);
	}

	@Override
	public int getItemKind() {
		
		return kind;
	}

	@Override
	public QName getNodeName() throws XQException {
		
		if (BagriXQUtils.isNodeNameSupported(kind)) {
			
			if (nodeName != null && "*".equals(nodeName.getLocalPart())) { 
				return null;  // a wildcard..
			}
			return nodeName;
		}
		throw new XQException("getNodeName is not supported for this item kind: " + kind);
	}

	@Override
	public String getPIName() throws XQException {
		
		if (BagriXQUtils.isPINameSupported(kind)) {
			if (nodeName != null) {
				return nodeName.getLocalPart();
			}
			return null;
		}
		throw new XQException("getPIName is not supported for this item kind: " + kind);
	}

	@Override
	public URI getSchemaURI() {
		
		return schemaURI;
	}

	@Override
	public QName getTypeName() throws XQException {

		if (BagriXQUtils.isBaseTypeSupported(kind)) {
			return typeName;
		}
		throw new XQException("getTypeName is not supported for this item kind: " + kind);
	}

	@Override
	public boolean isAnonymousType() {
		// @TODO: wrong implementation!
		// Represents whether the item type is an anonymous type in the schema.
		//return false;
		return schemaURI != null && typeName == null;
	}

	@Override
	public boolean isElementNillable() {
		
		return nillable;
	}
	
	public boolean isNodeCompatible(org.w3c.dom.Node node) {
		short type = node.getNodeType();
		switch (kind) {
			case XQITEMKIND_DOCUMENT: return (type == Node.DOCUMENT_NODE || type == Node.DOCUMENT_FRAGMENT_NODE);
			case XQITEMKIND_DOCUMENT_ELEMENT: 
			case XQITEMKIND_ELEMENT: return (type == Node.ELEMENT_NODE);
			case XQITEMKIND_ATTRIBUTE: return (type == Node.ATTRIBUTE_NODE);
			case XQITEMKIND_COMMENT: return (type == Node.COMMENT_NODE);
			case XQITEMKIND_PI: return (type == Node.PROCESSING_INSTRUCTION_NODE);
			case XQITEMKIND_TEXT: return (type == Node.TEXT_NODE);
			default: return true;
		}
	}
	
	public boolean isValueCompatible(String value) {
		switch (baseType) {
			case XQBASETYPE_ANYTYPE: 
			case XQBASETYPE_ANYATOMICTYPE:
			case XQBASETYPE_STRING: return true;
			case XQBASETYPE_BOOLEAN: try {
				Boolean.parseBoolean(value);
				return true;
			} catch (Exception e) {
				break;
			}
			case XQBASETYPE_BYTE: try {
				Byte.parseByte(value);
				return true;
			} catch (Exception e) {
				break;
			} 
			case XQBASETYPE_DOUBLE: try {
				Double.parseDouble(value);
				return true;
			} catch (Exception e) {
				break;
			} 
			case XQBASETYPE_FLOAT: try {
				Float.parseFloat(value);
				return true;
			} catch (Exception e) {
				break;
			} 
			case XQBASETYPE_INT: try {
				Integer.parseInt(value);
				return true;
			} catch (Exception e) {
				break;
			} 
			case XQBASETYPE_INTEGER: try {
				new java.math.BigInteger(value);
				return true;
			} catch (Exception e) {
				break;
			} 
			case XQBASETYPE_DECIMAL: try {
				new java.math.BigDecimal(value);
				return true;
			} catch (Exception e) {
				break;
			} 
			case XQBASETYPE_LONG: try {
				Long.parseLong(value);
				return true;
			} catch (Exception e) {
				break;
			} 
			case XQBASETYPE_SHORT: try {
				Integer.parseInt(value);
				return true;
			} catch (Exception e) {
				break;
			} 
			case XQBASETYPE_NCNAME: if (NCName.isValid(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		
		int hashCode = this.getItemKind();
		if (this.getSchemaURI() != null) {
		    hashCode = 31*hashCode + this.getSchemaURI().hashCode();
		}
		if (BagriXQUtils.isBaseTypeSupported(kind)) {
			hashCode = 31*hashCode + baseType;
		}
		if (BagriXQUtils.isNodeNameSupported(kind) && nodeName != null) {
			hashCode = 31*hashCode + nodeName.hashCode();
		}
		if (BagriXQUtils.isBaseTypeSupported(kind)) {
			hashCode = 31*hashCode + typeName.hashCode();
		}
		try {
			if (BagriXQUtils.isPINameSupported(kind) && this.getPIName () != null) { 
				hashCode = 31*hashCode + this.getPIName().hashCode();
			}
		} catch (XQException ex) {
			// can't be this, actually...
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XQSequenceType)) {
			return false;
		}
		XQItemType other = (XQItemType) obj;
		if (this.getItemKind() != other.getItemKind()) {
			return false;
		}
		try {
			if (BagriXQUtils.isBaseTypeSupported(kind)) {
				if (this.getBaseType() != other.getBaseType()) {
					return false;
				}
			}
			if (BagriXQUtils.isNodeNameSupported(kind)) {
				if (nodeName == null) {
					if (other.getNodeName() != null) {
						return false;
					}
				} else {
					if (!nodeName.equals(other.getNodeName())) {
						return false;
					}
				}
			}
			if (schemaURI == null) {
				if (other.getSchemaURI() != null) {
					return false;
				}
			} else {
				if (!schemaURI.equals(other.getSchemaURI())) {
					return false;
				}
			}
			if (BagriXQUtils.isBaseTypeSupported(kind)) {
				if (typeName == null) {
					if (other.getTypeName() != null) {
						return false;
					}
				} else {
					if (!typeName.equals(other.getTypeName())) {
						return false;
					}
				}
			}
			if (isAnonymousType() != other.isAnonymousType()) {
				return false;
			}
			if (isElementNillable() != other.isElementNillable()) {
				return false;
			}
			if (BagriXQUtils.isPINameSupported(kind)) {
				if (this.getPIName() == null) {
					if (other.getPIName() != null) {
						return false;
					}
				} else {
					if (!this.getPIName().equals(other.getPIName())) {
						return false;
					}
				}
			}
			return true;
		} catch (Exception ex) {
			//
			return false;
		}
	}

	@Override
	public String toString() {
		return "BagriXQItemType [baseType=" + baseType + ", kind=" + kind
				+ ", nodeName=" + nodeName + ", typeName=" + typeName
				+ ", nillable=" + nillable + ", schemaURI=" + schemaURI + "]";
	}


}
