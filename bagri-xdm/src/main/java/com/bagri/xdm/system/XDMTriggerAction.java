package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"scope",
		"action"
})
public class XDMTriggerAction {

	@XmlType(name = "Scope", namespace = "http://www.bagri.com/xdm/system")
	@XmlEnum
	public enum Scope {

	    @XmlEnumValue("before")
		before,

	    @XmlEnumValue("after")
		after
	}
	
	@XmlType(name = "Action", namespace = "http://www.bagri.com/xdm/system")
	@XmlEnum
	public enum Action {

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
	private Action action;
	
	@XmlAttribute(required = true)
	private Scope scope;
	
	public XDMTriggerAction() {
		// for JAXB
	}

	public XDMTriggerAction(Action action, Scope scope) {
		this.action = action;
		this.scope = scope;
	}

	public Action getAction() {
		return action;
	}

	public Scope getScope() {
		return scope;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 + action.ordinal();
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
		if (action != other.action) {
			return false;
		}
		if (scope != other.scope) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return scope + " " + action;
	}

	
}
