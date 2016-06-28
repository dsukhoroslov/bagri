package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents DataType's cardinality 
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlType(name = "Cardinality", namespace = "http://www.bagridb.com/xdm/system")
@XmlEnum
public enum Cardinality {
	
	/**
	 * one
	 */
    @XmlEnumValue("one")
	one,
	
	/**
	 * one or more
	 */
    @XmlEnumValue("one_or_more")
	one_or_more,

	/**
	 * zero or one
	 */
    @XmlEnumValue("zero_or_one")
	zero_or_one,

	/**
	 * zero or more
	 */
    @XmlEnumValue("zero_or_more")
	zero_or_more;

}
