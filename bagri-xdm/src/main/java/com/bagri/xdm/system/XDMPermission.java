/**
 * 
 */
package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Denis Sukhoroslov
 *
 */
@XmlType(name = "Permission", namespace = "http://www.bagri.com/xdm/access")
@XmlEnum
public enum XDMPermission {

    @XmlEnumValue("readonly")
	readonly,

    @XmlEnumValue("readwrite")
	readwrite,
	
    @XmlEnumValue("execute")
	execute
	
}
