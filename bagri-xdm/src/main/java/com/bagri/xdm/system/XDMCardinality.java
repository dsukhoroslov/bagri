package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Cardinality", namespace = "http://www.bagridb.com/xdm/system")
@XmlEnum
public enum XDMCardinality {
	
    @XmlEnumValue("one")
	one,
	
    @XmlEnumValue("one_or_more")
	one_or_more,

    @XmlEnumValue("zero_or_one")
	zero_or_one,

    @XmlEnumValue("zero_or_more")
	zero_or_more;

}
