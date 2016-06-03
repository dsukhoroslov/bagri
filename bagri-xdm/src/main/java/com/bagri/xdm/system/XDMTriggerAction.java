package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"order",
		"scope"
})
public class XDMTriggerAction {

	@XmlType(name = "Order", namespace = "http://www.bagridb.com/xdm/system")
	@XmlEnum
	public enum Order {

	    @XmlEnumValue("before")
		before,

	    @XmlEnumValue("after")
		after
	}
	
	@XmlType(name = "Scope", namespace = "http://www.bagridb.com/xdm/system")
	@XmlEnum
	public enum Scope {

	    @XmlEnumValue("insert")
		insert,

	    @XmlEnumValue("update")
		update,

	    @XmlEnumValue("delete")
		delete,

	    @XmlEnumValue("begin")
		begin,

		@XmlEnumValue("commit")
		commit,
		
	    @XmlEnumValue("rollback")
		rollback
		
	}
	
	@XmlAttribute(required = true)
	private Scope scope;
	
	@XmlAttribute(required = true)
	private Order order;
	
	public XDMTriggerAction() {
		// for JAXB
	}

	public XDMTriggerAction(Order order, Scope scope) {
		this.order = order;
		this.scope = scope;
	}

	public Order getOrder() {
		return order;
	}

	public Scope getScope() {
		return scope;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 + order.ordinal();
		result = 31 * result + scope.ordinal();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		XDMTriggerAction other = (XDMTriggerAction) obj;
		if (order != other.order) {
			return false;
		}
		if (scope != other.scope) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return order + " " + scope;
	}

	
}
