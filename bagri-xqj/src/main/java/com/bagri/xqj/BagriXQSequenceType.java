package com.bagri.xqj;

import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequenceType;

public class BagriXQSequenceType implements XQSequenceType {
	
	private XQItemType type;
	private int occurence;
	
	BagriXQSequenceType(XQItemType type, int occurence) {
		this.type = type;
		this.occurence = occurence;
	}

	@Override
	public XQItemType getItemType() {
		
		return type;
	}

	@Override
	public int getItemOccurrence() {
		
		return occurence;
	}

	@Override
	public int hashCode() {
		
		if (occurence == OCC_EMPTY) {
		    return 1; 
		} 
		return occurence*31 + type.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof XQSequenceType)) {
			return false;
		}
		
		XQSequenceType other = (XQSequenceType) obj;
		if (occurence != other.getItemOccurrence()) {
			return false;
		}
		if (occurence == OCC_EMPTY) {
			return true;
		}
		// here types can't be null
		return type.equals(other.getItemType());
	}

	@Override
	public String toString() {
		return "XQSequenceType [type=" + type + ", occurence=" + occurence + "]";
	}

}
