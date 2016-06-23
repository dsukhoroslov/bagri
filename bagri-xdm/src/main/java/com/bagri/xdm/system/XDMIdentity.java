package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/access", propOrder = {
		"id" 
})
public class XDMIdentity {

	@XmlAttribute
	@XmlID
	private String id;

	/**
	 * default constructor
	 */
	public XDMIdentity() {
		//
	}
	
	/**
	 * 
	 * @param id identity id
	 */
	public XDMIdentity(String id) {
		this.id = id;
	}
	
	/**
	 * 
	 * @return identity id
	 */
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id.hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XDMIdentity other = (XDMIdentity) obj;
		return id.equals(other.id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "XDMIdentity [" + id + "]";
	}
	
	
}
